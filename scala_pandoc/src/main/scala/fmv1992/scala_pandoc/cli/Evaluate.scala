package fmv1992.scala_pandoc

// ???: Allow pipes to be used in `pipe=""`.

import sys.process.Process

/** Object for main action of unwrapping and explaining code. */
object Evaluate {

  // ???: Allow this to be specified via CLI or env var.
  val actionMark = "pipe"

  /** Unwrap pandoc code blocks marked with `.unwrapExplain`.
    *
    * Unwrap using the following pattern: code → code paragraph code_result.
    * Where "paragraph" is an "explanation expression" between the results. For
    * example:
    *
    * -------------------------------------------------------------------------
    * ```{.unwrapExplain pipe="sh"}
    * whoami
    * ```
    * -------------------------------------------------------------------------
    *
    * Becomes transformed to:
    *
    * -------------------------------------------------------------------------
    * ```{pipe="sh"}
    * whoami
    * ```
    *
    * Gives:
    *
    * ```
    * muyser
    * ```
    * -------------------------------------------------------------------------
    */
  def entryPoint(in: Seq[String]): Seq[String] = {
    val text = in.mkString("\n")
    val res = recursiveExplain(ujson.read(text))
    val retVal = res.toString.split("\n")
    retVal
  }

  /** Unwrap marked code with `.unwrapExplain` mark. */
  def recursiveExplain(j: ujson.Value): ujson.Value = {
    // flatMap ujson.Arr.
    Pandoc.recursiveMap(
      j,
      (x: ujson.Value) ⇒ x match {
          case x: ujson.Arr ⇒ Pandoc.flatMap(x, explainIfMarked)
          case _ ⇒ x
        }
    )
  }

  /** Unwrap marked code by adding a proper code paragraph and code element in
    * its place.
    */
  def explainIfMarked[A <: ujson.Value](j: A): Seq[ujson.Value] = {
    val res = if (Pandoc.isPTypeCodeBlock(j) || Pandoc.isPTypeCode(j)) {
      val cb = PandocCode(j)
      if (cb.attr.hasKey(actionMark)) {
        val runCode: String = cb.content
        val systemC: String = cb.attr.kvp("pipe")
        val proc = Process(systemC)
        val procIn = proc #< PandocUtilities.stringToBAIS(runCode)
        // Console.err.println(PandocUtilities.stringToBAIS(runCode).toArray)
        Console.err.println("-" * 79)
        Console.err.println(proc)
        Console.err.println("-" * 79)
        Console.err.println(procIn)
        val lines = proc.lineStream.mkString("\n")
        Seq(
          PandocCode(
            cb.attr.removeKey(actionMark),
            lines,
            cb.pandocType
          ).toUJson
        )
      } else {
        Seq(j)
      }
    } else {
      Seq(j)
    }
    res
  }

}

//  Run this in vim:
//
// vim source: 1,$-10s/=>/⇒/ge
// vim source: iabbrev uj ujson.Value
//
// vim: set filetype=scala fileformat=unix foldmarker={,} nowrap tabstop=2 softtabstop=2 spell spelllang=en:
