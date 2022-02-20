package fmv1992.scala_pandoc

// ???: Allow pipes to be used in `pipe=""`.

import sys.process.Process
import sys.process.ProcessBuilder
import sys.process.ProcessLogger

/** Object for main action of unwrapping and explaining code. */
object Evaluate extends PandocScalaMain {

  // ???: Allow this to be specified via CLI or env var.
  val evaluateMark = "pipe"
  val expandMark = "joiner"
  val evaluateSequentialMark = "computationTreeId"
  lazy val evalStringSep = "ǁ" + ("‡" * 79) + "ǁ"
  // It does not work as expected.
  // private lazy val evalStringSepR = evalStringSepP.flatMap("[" + _ + "]")
  private lazy val evalStringSepR = evalStringSep
  private lazy val evalStringSepP = s""" ; { print("${evalStringSep}") } ; """

  lazy val shell = sys.env.get("SHELL").getOrElse("bash")

  def entryPoint(in: Seq[String]): Seq[String] = {
    val text = in.mkString("\n")
    val expanded = Pandoc.recursiveMapUJToUJIfTrue(ujson.read(text))(
      Pandoc.isUArray
    )(x => Pandoc.expandArray(x)(expandMarked))
    val expandedAndEvaluated = Pandoc.recursiveMapUJToUJIfTrue(expanded)(
      Pandoc.isUObject
    )(evaluateMarked)
    val res = expandedAndEvaluated.toString.split("\n")
    res
  }

  def removeEvaluationAndEvalSequentialMarks(
      codeBlock: PandocCode
  ): PandocCode = {
    val noEvaluationMark = if (codeBlock.attr.kvp.contains(evaluateMark)) {
      codeBlock.attr.removeKey(evaluateMark)
    } else {
      codeBlock.attr
    }

    val noEMAndNoEvaluationSequentialMark =
      if (codeBlock.attr.kvp.contains(evaluateSequentialMark)) {
        noEvaluationMark.removeKey(evaluateSequentialMark)
      } else {
        noEvaluationMark
      }
    PandocCode(
      noEMAndNoEvaluationSequentialMark,
      codeBlock.content,
      codeBlock.pandocType
    )
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
          val cleanedOfMarks = removeEvaluationAndEvalSequentialMarks(cb).attr
          val noEEAndIdentifierAttr =
            PandocAttributes("", cleanedOfMarks.classes, cleanedOfMarks.kvp)

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
    evaluateIndependentCode(evaluateSequentialCode(j))
  }

  // Non-atomic: it is context dependent.
  //
  // The algorithm descends the trees creating a map of computation ids -> code.
  // Then the bottom-most node evaluates the whole code. It then pops the last
  // element of the list and return its tail.
  def evaluateSequentialCode(j: ujson.Value): ujson.Value = {

    type MS = Map[String, List[String]]
    val emptyMS = Map.empty: MS

    def getSequentialCode(j: ujson.Value): MS = {
      val res = if (Pandoc.isPTypeGeneralCode(j)) {
        val cb = PandocCode(j)
        val innerRes = cb.attr.kvp
          .get(evaluateSequentialMark)
          .map(x => Map((x -> List(cb.content))))
          .getOrElse(emptyMS)
        innerRes
      } else {
        emptyMS
      }
      res
    }

    def mergeMS[A, B](
        a1: Map[A, List[B]],
        a2: Map[A, List[B]]
    ): Map[A, List[B]] = {
      val simpleIntersection = a1 ++ a2
      val sameKeys = a1.keySet & a2.keySet
      val updatedComplement =
        simpleIntersection ++ (sameKeys.map(x => (x, a1(x) ++ a2(x))))
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
    //
    //  Single responsibility:
    //  Get all
    def getAggComputationTreeById(
        goJ: ujson.Value,
        listOfCode: MS
    ): (ujson.Value, MS) = {

      val newSeqCode = getSequentialCode(goJ)
      val newListOfCode = newSeqCode

      val res = goJ match {
        case _: ujson.Num  => (goJ, newListOfCode)
        case _: ujson.Str  => (goJ, newListOfCode)
        case _: ujson.Bool => (goJ, newListOfCode)
        case _: ujson.Arr => {
          val mapped = goJ.arr.map(getAggComputationTreeById(_, newListOfCode))
          val (v, codes): (List[ujson.Value], List[MS]) = mapped.toList.unzip
          (goJ, codes.foldLeft(newListOfCode)(mergeMS(_, _)))
        }
        case _: ujson.Obj => {
          val mapped = goJ.obj.iterator.toList
            .map(_._2)
            .map(getAggComputationTreeById(_, newListOfCode))
          val (v, codes): (List[ujson.Value], List[MS]) = mapped.toList.unzip
          val foldedCode = codes.foldLeft(newListOfCode)(mergeMS(_, _))
          (goJ, foldedCode)
        }
        case ujson.Null => (goJ, newListOfCode)
      }

      res

    }

    def applyComputationTreeById(
        j: ujson.Value,
        results: Map[String, Seq[String]]
    ): (ujson.Value, Map[String, Seq[String]]) = {

      def go(
          goJ: ujson.Value,
          goResults: Map[String, Seq[String]]
      ): (ujson.Value, Map[String, Seq[String]]) = {

        val newSeqCode = getSequentialCode(goJ)
        val res = if (newSeqCode.isEmpty) {
          goJ
        } else {
          val cb = PandocCode(goJ)
          if (cb.attr.kvp.contains(evaluateSequentialMark)) {
            val computationID = cb.attr.kvp
              .get(evaluateSequentialMark)
              .getOrElse(throw new Exception())

            // Only change contents if they have an evaluate mark. Otherwise
            // leave code in document as is.
            val cbWithResult = if (cb.attr.kvp.contains(evaluateMark)) {
              cb.changeContent(
                goResults(computationID).headOption.getOrElse("")
              )
            } else {
              cb
            }

            val cbWithResultNoEval = PandocCode(
              removeEvaluationAndEvalSequentialMarks(cbWithResult).attr,
              cbWithResult.content,
              cbWithResult.pandocType
            )
            cbWithResultNoEval.toUJson
          } else {
            goJ
          }
        }
        val newComputationList = if (newSeqCode.isEmpty) {
          goResults
        } else {
          val cb = PandocCode(goJ)
          val computationID = cb.attr.kvp
            .get(evaluateSequentialMark)
            .getOrElse(throw new Exception())
          goResults.map(x =>
            if (x._1 == computationID) {
              (x._1, if (x._2.isEmpty) Nil else x._2.tail)
            } else x
          )
        }

        (res, newComputationList)

      }

      val newUJAndMap = j match {
        case _: ujson.Num  => go(j, results)
        case _: ujson.Str  => go(j, results)
        case _: ujson.Bool => go(j, results)
        case _: ujson.Arr => {
          // Incrementally consume the computation results.
          val replacedCodeWithItsEvaluation =
            j.arr.foldLeft((List.empty: List[ujson.Value], results))((t, x) => {
              val (l: List[ujson.Value], r: Map[String, Seq[String]]) = t
              val (newUJcode, newMS) = applyComputationTreeById(x, r)
              (newUJcode :: l, newMS)
            })
          val res = go(
            ujson.Arr(replacedCodeWithItsEvaluation._1.reverse: _*),
            replacedCodeWithItsEvaluation._2
          )
          res
        }
        case _: ujson.Obj => {
          // Incrementally consume the computation results.
          val replacedCodeWithItsEvaluation = j.obj.iterator.toList.foldLeft(
            (List.empty: List[(String, ujson.Value)], results)
          )((t, x) => {
            val (l: List[(String, ujson.Value)], r: Map[String, Seq[String]]) =
              t
            val (newUJcode, newMS) = applyComputationTreeById(x._2, r)
            ((x._1, newUJcode) :: l, newMS)
          })
          val res = go(
            PandocUtilities.mapToUjsonObj(
              replacedCodeWithItsEvaluation._1.toMap
            ),
            replacedCodeWithItsEvaluation._2
          )
          res
        }
        case ujson.Null => go(j, results)
      }

      newUJAndMap

    }

    val codeMap: MS = getAggComputationTreeById(j, emptyMS)._2
    val codeMapWithSep: MS =
      codeMap
        .mapValues(x => x.flatMap(y => List(y, evalStringSepP)).dropRight(1))
        .toMap
    val evalCode: Map[String, CodeEvaluation] =
      codeMapWithSep
        .mapValues(x =>
          evaluateSeq(
            PandocCode.makeScalaScript(x.mkString("\n"))
          )
        )
        .toMap
    val erroredProcesses = evalCode.values.filter(_.returnCode != 0)
    if (erroredProcesses.isEmpty) {
      Unit
    } else {
      erroredProcesses.foreach(_.reportError)
      throw new Exception()
    }
    // Stdout is split based on newline instead of other marker.
    // [EvalAndSubstsCorrect]
    val evalStdout: Map[String, Seq[String]] =
      evalCode.mapValues(x => x.stdout.split(evalStringSepR).toList).toMap

    // Check that input code and generated output have the same lenght.
    require(
      codeMap.keySet == evalStdout.keySet,
      List(
        codeMap.keys.toList.sorted.toString,
        "--",
        evalStdout.keys.toList.sorted.toString
      ).mkString("\n--\n")
    )
    codeMap.keys.foreach(x => {
      require(
        (codeMap(x).length == evalStdout(x).length) || evalStdout(x).isEmpty,
        List(x, codeMap(x), evalStdout(x)).mkString("\n--\n")
      )
    })

    val replacedCode: ujson.Value = applyComputationTreeById(j, evalStdout)._1
    replacedCode

  }

  // Independent: can be applied to every json and sub-json element.
  def evaluateIndependentCode(j: ujson.Value): ujson.Value = {
    val res = if (Pandoc.isPTypeGeneralCode(j)) {
      val cb = PandocCode(j)
      if (cb.attr.hasKey(evaluateMark)) {
        val ce: CodeEvaluation = evaluateSeq(cb)
        if (ce.returnCode != 0) {
          ce.reportError
          throw new Exception()
        }
        PandocCode(
          cb.attr.removeKey(evaluateMark),
          ce.stdout,
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

  def evaluateSeq(cb: PandocCode): CodeEvaluation = {

    val runCode: String = cb.content
    val systemC: String = cb.attr.kvp("pipe")
    val ce: CodeEvaluation =
      new CodeEvaluation(Process(Seq(shell, "-c", systemC)), runCode)

    ce

    // val tempFile =
    //   File.createTempFile("scala_pandoc_", System.nanoTime.toString)

    // val printSmt = s""" ; { print("${stringBetweenStatements}") } ; """

    // val interTwinedList = code.flatMap(x => Seq(x, printSmt))
    // val suitableInput = interTwinedList.mkString("\n")
    // reflect.io.File(tempFile).writeAll(suitableInput)

    // val scalaProc = Process(Seq("scala", tempFile.getCanonicalPath))
    // val res =
    //   scalaProc.lineStream.mkString("\n").split(stringBetweenStatementsRegex)
    // res.toSeq: Seq[String]
  }

}

// ???: Cannot be a case class because of lazy evaluation.
class CodeEvaluation(p: => ProcessBuilder, val code: String) {

  def reportError = {
    Console.err.println(
      this.toString
    )
  }

  private val stdoutSB = new StringBuilder
  private val stderrSB = new StringBuilder

  private val codeAsBAIS = PandocUtilities.stringToBAIS(code)
  private val logger: ProcessLogger =
    ProcessLogger(
      x => stdoutSB.append(x + "\n"),
      x => stderrSB.append(x + "\n")
    )
  private val proc = p #< codeAsBAIS

  val returnCode: Int = proc.!(logger)

  val stdout = stdoutSB.mkString.dropRight(1)
  val stderr = stderrSB.mkString.dropRight(1)

  override def toString: String = {
    def toIndentedBlock(x: String): String = {
      x.split("\n").map(x => "\t| " + x).mkString("\n")
    }
    Seq(
      "+" * 79,
      "Code Evaluation:",
      "ProcessBuilder:\n" + toIndentedBlock(p.toString),
      "Code:\n" + toIndentedBlock(code),
      "Stdout:\n" + toIndentedBlock(stdout),
      "Stderr:\n" + toIndentedBlock(stderr),
      "+" * 79
    ).mkString("\n")
  }

}
