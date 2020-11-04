package fmv1992.scala_pandoc

import fmv1992.fmv1992_scala_utilities.util.Reader
import fmv1992.fmv1992_scala_utilities.util.S
import fmv1992.scala_cli_parser.CLIConfigTestableMain
import fmv1992.scala_cli_parser.Argument

// ???: Extend with MainInterface.
/** Entry point for program. */
object Main extends CLIConfigTestableMain {

  // ???: Grab those from somewhere else.
  //
  // ???: This whole `S` thing is palliative for lack of support of resources
  // in Scala Native. This deficiency tickles down to
  // `fmv1992.fmv1992_scala_utilities.util`.
  @inline override final val version =
    Reader.readLines(S.putabspath("./src/main/resources/version")).mkString
  lazy val programName = "scala_pandoc"
  @inline override final val CLIConfigPath =
    S.putabspath("src/main/resources/scala_pandoc_cli_config.conf")

  def testableMain(args: Seq[Argument]): Seq[String] = {

    // ???: This so common parsing should be responsibility of `main`.
    val (inputArgs, otherArgs) = splitInputArgumentFromOthers(args)
    val inputString = readInputArgument(inputArgs)

    val res: Seq[String] =
      otherArgs.foldLeft(inputString)((lineSeq, x) ⇒ {
        if (x.longName == "evaluate") {
          Evaluate.entryPoint(lineSeq)
        } else if (x.longName == "farsi-to-rtl") {
          RLFarsi.entryPoint(lineSeq)
        } else if (x.longName == "embed") {
          Embed.entryPoint(lineSeq)
        } else if (x.longName == "replace-variables") {
          ReplaceVariables.entryPoint(lineSeq)
        } else {
          println(s"'${x.longName}' not a CLI option.")
          throw new Exception()
        }
      })

    res

  }

}
