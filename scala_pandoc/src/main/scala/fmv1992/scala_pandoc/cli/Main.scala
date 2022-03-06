package fmv1992.scala_pandoc

import fmv1992.fmv1992_scala_utilities.util.Reader
import fmv1992.fmv1992_scala_utilities.util.S
import fmv1992.scala_cli_parser.util.MainTestableConfBased
import fmv1992.scala_cli_parser.cli.Argument
import fmv1992.scala_cli_parser.cli.ArgumentCLI

// ???: Extend with MainInterface.
/** Entry point for program. */
object Main extends MainTestableConfBased {

  // ???: Grab those from somewhere else.
  //
  // ???: This whole `S` thing is palliative for lack of support of resources
  // in Scala Native. This deficiency tickles down to
  // `fmv1992.fmv1992_scala_utilities.util`.
  @inline override final val version =
    scala.io.Source
      .fromFile(S.putabspath("./src/main/resources/version"))
      .mkString
  lazy val programName = "scala_pandoc"
  @inline override final val CLIConfigContents =
    S.putfile("src/main/resources/scala_pandoc_cli_config.conf")

  def testableMain(args: Set[ArgumentCLI]) = {

    // ???: This so common parsing should be responsibility of `main`.
    val (
      inputArgs: Seq[fmv1992.scala_cli_parser.cli.ArgumentCLI],
      otherArgs: Seq[fmv1992.scala_cli_parser.cli.ArgumentCLI]
    ) =
      splitInputArgumentCLIFromOthers(args.toSeq)
    val inputString = readInputArgument(inputArgs)

    val res: Seq[String] =
      otherArgs.foldLeft(inputString)((lineSeq, x) => {
        if (x.name == "evaluate") {
          CodeEvaluatorImpl.entryPoint(lineSeq)
        } else if (x.name == "farsi-to-rtl") {
          RLFarsi.entryPoint(lineSeq)
        } else if (x.name == "embed") {
          Embed.entryPoint(lineSeq)
        } else if (x.name == "replace-variables") {
          ReplaceVariables.entryPoint(lineSeq)
        } else {
          println(s"'${x.name}' not a CLI option.")
          throw new Exception()
        }
      })

    res

  }

}
