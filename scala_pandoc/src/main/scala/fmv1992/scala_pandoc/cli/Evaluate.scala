package fmv1992.scala_pandoc

// ???: Allow pipes to be used in `pipe=""`.

import sys.process.Process
import sys.process.ProcessBuilder
import sys.process.ProcessLogger
import java.io.File

/** Object for main action of unwrapping and explaining code. */
object Evaluate extends PandocScalaMain {

  // ???: Allow this to be specified via CLI or env var.
  val evaluateMark = "pipe"
  val expandMark = "joiner"
  val serialScalaMark = "s"

  lazy val shell = sys.env.get("SHELL").getOrElse("bash")

  def entryPoint(in: Seq[String]): Seq[String] = {
    val text = in.mkString("\n")
    val expanded = Pandoc.recursiveMapUJToUJIfTrue(ujson.read(text))(
      Pandoc.isUArray
    )(x ⇒ Pandoc.expandArray(x)(expandMarked))
    val expandedAndEvaluated = Pandoc.recursiveMapUJToUJIfTrue(expanded)(
      Pandoc.isUObject
    )(evaluateMarked)
    val res = expandedAndEvaluated.toString.split("\n")
    res
  }

  def expandMarked(j: ujson.Value): Seq[ujson.Value] = {
    val res: Seq[ujson.Value] =
      if (Pandoc.isPTypeCodeBlock(j) || Pandoc.isPTypeCode(j)) {
        val cb = PandocCode(j)
        if (cb.attr.hasKey(expandMark)) {
          val joinerText: String = cb.attr.kvp(expandMark)
          val joinerJSON: ujson.Value =
            PandocJsonParsing.pandocParseMarkdownToUJson(joinerText)(0)

          // Prepare attributes for expanded expression.
          val noExpansionAttr = cb.attr.removeKey(expandMark)
          val noEAndEvaluationAttr = noExpansionAttr.removeKey(evaluateMark)
          val noEEAndIdentifierAttr = PandocAttributes(
            "",
            noEAndEvaluationAttr.classes,
            noEAndEvaluationAttr.kvp
          )

          Seq(
            // Put code as is without expansion and evaluation.
            PandocCode(
              noEEAndIdentifierAttr,
              cb.content,
              cb.pandocType
            ).toUJson,
            // Put a joiner paragraph..
            joinerJSON,
            // Remove expansion mark.
            PandocCode(noExpansionAttr, cb.content, cb.pandocType).toUJson
          )

        } else {
          Seq(j)
        }
      } else {
        Seq(j)
      }
    res
  }

  def evaluateMarked(j: ujson.Value): ujson.Value = {
    evaluateIndependentCode(j)
  }

  def evaluateSequentialCode(j: ujson.Value): ujson.Value = {
    ???
  }

  def evaluateIndependentCode(j: ujson.Value): ujson.Value = {
    val res = if (Pandoc.isPTypeCodeBlock(j) || Pandoc.isPTypeCode(j)) {
      val cb = PandocCode(j)
      if (cb.attr.hasKey(evaluateMark)) {
        val runCode: String = cb.content
        val systemC: String = cb.attr.kvp("pipe")
        val ce: CodeEvaluation =
          new CodeEvaluation(Process(Seq(shell, "-c", systemC)), runCode)
        if (ce.returnCode != 0) {
          ce.reportError
          throw new Exception()
        }
        PandocCode(
          cb.attr.removeKey(evaluateMark),
          ce.stdout.mkString,
          cb.pandocType
        ).toUJson
      } else {
        j
      }
    } else {
      j
    }
    res
  }

  // Regarding evaluateSeq.
  lazy val stringBetweenStatements = "|" + ("‡" * 79) + "|"
  private lazy val stringBetweenStatementsRegex =
    stringBetweenStatements.flatMap("[" + _ + "]")

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

// ???: Cannot be a case class because of lazy evaluation.
class CodeEvaluation(p: ⇒ ProcessBuilder, val code: String) {

  def reportError = {
    Console.err.println(
      Seq(
        "Code:",
        "---",
        code,
        "---",
        s"Had a return code of ${returnCode} and stderr:",
        "---",
        stderr,
        "---"
      ).mkString("\n")
    )
  }

  private val stdoutSB = new StringBuilder
  private val stderrSB = new StringBuilder

  private val codeAsBAIS = PandocUtilities.stringToBAIS(code)
  private val logger: ProcessLogger =
    ProcessLogger(x ⇒ stdoutSB.append(x + "\n"), x ⇒ stderrSB.append(x + "\n"))
  private val proc = p #< codeAsBAIS

  val returnCode: Int = proc.!(logger)

  val stdout = stdoutSB.mkString.dropRight(1)
  val stderr = stderrSB.mkString.dropRight(1)

}
