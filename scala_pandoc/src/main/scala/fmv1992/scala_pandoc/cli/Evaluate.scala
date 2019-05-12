package fmv1992.scala_pandoc

// ???: Allow pipes to be used in `pipe=""`.

import sys.process.Process
import sys.process.ProcessLogger
import java.io.File

/** Object for main action of unwrapping and explaining code. */
object Evaluate {

  // ???: Allow this to be specified via CLI or env var.
  val evaluateMark = "pipe"
  val expandMark = "joiner"

  lazy val shell = sys.env.get("SHELL").getOrElse("bash")

  // Regarding evaluateSeq.
  lazy val stringBetweenStatements = "|" + ("‡" * 79) + "|"
  private lazy val stringBetweenStatementsRegex =
    stringBetweenStatements.flatMap("[" + _ + "]")

  def entryPoint(in: Seq[String]): Seq[String] = {
    val text = in.mkString("\n")
    val expanded = Pandoc.recursiveMapIfTrue(ujson.read(text))(Pandoc.isUArray)(
      x ⇒ Pandoc.flatMap(x, expandIfMarked)
    )
    val expandedAndEvaluated = Pandoc.recursiveMapIfTrue(expanded)(
      Pandoc.isUArray
    )(x ⇒ Pandoc.flatMap(x, evaluateIfMarked))
    val res = expandedAndEvaluated.toString.split("\n")
    res
  }

  def expandIfMarked(j: ujson.Value): Seq[ujson.Value] = {
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

  def evaluateIfMarked(j: ujson.Value): Seq[ujson.Value] = {
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
        if (retCode != 0) {
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

  def evaluateSeq(code: Seq[String]): Seq[String] = {

    val tempFile =
      File.createTempFile("scala_pandoc_", System.nanoTime.toString)

    val printSmt = s""" ; { print("${stringBetweenStatements}") } ; """

    val interTwinedList = code.flatMap(x ⇒ Seq(x, printSmt))
    val suitableInput = interTwinedList.mkString("\n")
    reflect.io.File(tempFile).writeAll(suitableInput)

    val scalaProc = Process(Seq("scala", tempFile.getCanonicalPath))
    val res = scalaProc.lineStream.mkString.split(stringBetweenStatementsRegex)
    res.toSeq: Seq[String]
  }

}

//  Run this in vim:
//
// vim source: 1,$-10s/=>/⇒/ge
// vim source: iabbrev uj ujson.Value
//
// vim: set filetype=scala fileformat=unix foldmarker={,} nowrap tabstop=2 softtabstop=2 spell spelllang=en:
