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
  val evaluateSequentialMark = "computationTreeId"

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
      if (Pandoc.isPTypeGeneralCode(j)) {
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

  // Non-atomic: it is context dependent.
  //
  // The algorithm descends the trees creating a map of computation ids → code.
  // Then the bottom-most node evaluates the whole code. It then pops the last
  // element of the list and return its tail.
  def evaluateSequentialCode(j: ujson.Value): ujson.Value = {

    type MS = Map[String, List[String]]
    val emptyMS = Map.empty: MS

    def getSequentialCode(j: ujson.Value): MS = {
      val res = if (Pandoc.isPTypeGeneralCode(j)) {
        val cb = PandocCode(j)
        val innerRes = cb.attr.kvp.get(evaluateSequentialMark).map(
          x ⇒ Map((x → List(cb.content)))
        ).getOrElse(emptyMS)
        innerRes
      } else {
        emptyMS
      }
      res
    }

    def mergeMS[A, B](a1: Map[A, List[B]], a2: Map[A, List[B]]): Map[A, List[B]] = {
      val simpleIntersection = a1 ++ a2
      val sameKeys = a1.keySet & a2.keySet
      val updatedComplement = simpleIntersection ++ (
          sameKeys.map(x ⇒ (x, a1(x) ++ a2(x)))
      )
      updatedComplement
    }

    //  ___ ___ ___                                    _   _                      __
    // |__ \__ \__ \_   ___  ___ _ __   __ _ _ __ __ _| |_(_) ___  _ __     ___  / _|   ___ ___  _ __   ___ ___ _ __ _ __  ___
    //   / / / / / (_) / __|/ _ \ '_ \ / _` | '__/ _` | __| |/ _ \| '_ \   / _ \| |_   / __/ _ \| '_ \ / __/ _ \ '__| '_ \/ __|
    //  |_| |_| |_| _  \__ \  __/ |_) | (_| | | | (_| | |_| | (_) | | | | | (_) |  _| | (_| (_) | | | | (_|  __/ |  | | | \__ \
    //  (_) (_) (_)(_) |___/\___| .__/ \__,_|_|  \__,_|\__|_|\___/|_| |_|  \___/|_|    \___\___/|_| |_|\___\___|_|  |_| |_|___/
    //                          |_|
    //
    //  ???: Separation of concerns. Each function should do one thing and do
    //  it well.
    def go(
      goJ: ujson.Value,
      listOfCode: MS,
      listOfResults: MS): (ujson.Value, MS, MS) = {

        println("-" * 79)
        println(goJ)
        // println(listOfCode)

        val newSeqCode = getSequentialCode(goJ)
        val newListOfCode = newSeqCode
        // val newListOfCode = mergeMS(listOfCode, newSeqCode)
        // println(newListOfCode)

        val res = goJ match {
          case _: ujson.Num ⇒ (goJ, newListOfCode, listOfResults)
          case _: ujson.Str ⇒ (goJ, newListOfCode, listOfResults)
          case _: ujson.Bool ⇒ (goJ, newListOfCode, listOfResults)
          case _: ujson.Arr ⇒ {
            val mapped = goJ.arr.map(go(_, newListOfCode, listOfResults))
            val (v, codes, results): (List[ujson.Value], List[MS], List[MS]) = mapped.toList.unzip3
            (goJ,
              codes.foldLeft(newListOfCode)(mergeMS(_, _)),
              null)
          }
          case _: ujson.Obj ⇒ {
            val mapped = goJ.obj.iterator.toList.map(_._2).map(go(_, newListOfCode, listOfResults))
            val (v, codes, results): (List[ujson.Value], List[MS], List[MS]) = mapped.toList.unzip3
            val foldedCode = codes.foldLeft(newListOfCode)(mergeMS(_, _))
            (goJ,
              foldedCode,
            null)
          }
          case ujson.Null ⇒ (goJ, newListOfCode, listOfResults)
        }

        res

      }

      go(j, emptyMS, emptyMS)._1

  }

  // Atomic: can be applied to every json and sub-json element.
  def evaluateIndependentCode(j: ujson.Value): ujson.Value = {
    val res = if (Pandoc.isPTypeGeneralCode(j)) {
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
