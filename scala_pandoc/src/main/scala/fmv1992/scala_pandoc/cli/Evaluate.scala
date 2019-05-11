package fmv1992.scala_pandoc

// ???: Allow pipes to be used in `pipe=""`.

import sys.process.Process
import sys.process.ProcessLogger

/** Object for main action of unwrapping and explaining code. */
object Evaluate {

  // ???: Allow this to be specified via CLI or env var.
  val evaluateMark = "pipe"
  val expandMark = "joiner"
  lazy val shell = sys.env.get("SHELL").getOrElse("bash")

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
    val expanded = recursiveEvaluate(ujson.read(text))(expandIfMarked)
    val expandedAndEvaluated = recursiveEvaluate(expanded)(evaluateIfMarked)
    val res = expandedAndEvaluated.toString.split("\n")
    res
  }

  /** Unwrap marked code with `.unwrapExplain` mark. */
  def recursiveEvaluate[A](
      j: ujson.Value
  )(f: ujson.Value ⇒ Seq[ujson.Value]): ujson.Value = {
    // flatMap ujson.Arr.
    Pandoc.recursiveMap(
      j,
      (x: ujson.Value) ⇒ x match {
          case x: ujson.Arr ⇒ Pandoc.flatMap(x, f)
          case _ ⇒ x
        }
    )
  }

  /** Unwrap marked code by adding a proper code paragraph and code element in
    * its place.
    */
  def expandIfMarked[A <: ujson.Value](j: A): Seq[ujson.Value] = {
    val res: Seq[ujson.Value] =
      if (Pandoc.isPTypeCodeBlock(j) || Pandoc.isPTypeCode(j)) {
        val cb = PandocCode(j)
        if (cb.attr.hasKey(expandMark)) {
          val joinerText: String = cb.attr.kvp(expandMark)
          val joinerJSON: ujson.Value =
            PandocJsonParsing.pandocParseStringToUJson(joinerText)(0)
          Seq(
            PandocCode(
              cb.attr.removeKey(expandMark).removeKey(evaluateMark),
              cb.content,
              cb.pandocType
            ).toUJson,
            joinerJSON,
            PandocCode(cb.attr.removeKey(expandMark), cb.content, cb.pandocType).toUJson
          )
        } else {
          Seq(j)
        }
      } else {
        Seq(j)
      }
    res
  }

  /** Unwrap marked code by adding a proper code paragraph and code element in
    * its place.
    */
  def evaluateIfMarked[A <: ujson.Value](j: A): Seq[ujson.Value] = {
    val res = if (Pandoc.isPTypeCodeBlock(j) || Pandoc.isPTypeCode(j)) {
      val cb = PandocCode(j)
      if (cb.attr.hasKey(evaluateMark)) {
        val runCode: String = cb.content
        val systemC: String = cb.attr.kvp("pipe")
        val stdout = new StringBuilder
        val stderr = new StringBuilder
        val logger: ProcessLogger =
          ProcessLogger(stdout append _, stderr append _)
        val proc = (Process(Seq(shell, "-c", systemC)) #< PandocUtilities
          .stringToBAIS(runCode))
        val retCode: Int = proc.!(logger)
        if (retCode != 0 || runCode.toString.contains("myu")) {
          Console.err.println(
            Seq(
              "Code:",
              "---",
              runCode,
              "---",
              s"Had a return code of ${retCode} and stderr:",
              "---",
              stderr,
              "---"
            ).mkString("\n")
          )
          throw new Exception()
        }
        Seq(
          PandocCode(
            cb.attr.removeKey(evaluateMark),
            stdout.toString,
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

  // /** Program requests data.
  // *  Giver gives data.
  // *  Program process data.
  // *  Program informs it is done processing.
  // *  Giver informs Storager that program has processed data.
  // *  Storager registers.
  // *  Storager informs Giver.
  // *  Giver gives data.
  // *
  // */
  // def evaluateSeq(code: Seq[String]): Seq[String] = {
  // val scalaProc = Process(Seq("bash", "-c", "scala"))
  // val printSmt = """{ println("-" * 79) }"""
  // val interTwinedList = code.flatMap(x ⇒ Seq(x, printSmt))
  // val suitableInput = PandocUtilities.stringToBAIS(interTwinedList.mkString("\n"))
  // val proc = scalaProc #< suitableInput
  // val res = proc.lineStream(ProcessLogger(line ⇒ ()))
  // res
  // }

}
//  Run this in vim:
//
// vim source: 1,$-10s/=>/⇒/ge
// vim source: iabbrev uj ujson.Value
//
// vim: set filetype=scala fileformat=unix foldmarker={,} nowrap tabstop=2 softtabstop=2 spell spelllang=en:
