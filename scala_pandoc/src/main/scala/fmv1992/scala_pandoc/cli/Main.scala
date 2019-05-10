package fmv1992.scala_pandoc

import fmv1992.fmv1992_scala_utilities.util.Reader
// import fmv1992.fmv1992_scala_utilities.cli.GNUParser
// import fmv1992.fmv1992_scala_utilities.cli.GNUArg
import fmv1992.fmv1992_scala_utilities.cli.CLIConfigTestableMain
import fmv1992.fmv1992_scala_utilities.cli.Argument

// import java.io.File

// import ujson._

// ???: Extend with MainInterface.
/** Entry point for program. */
object Main extends CLIConfigTestableMain {

  // ???: Grab those from somewhere else.
  lazy val programName = "scala_pandoc"
  lazy val CLIConfigPath = "src/main/resources/scala_pandoc_cli_config.conf"
  lazy val version =
    Reader.readLines("./src/main/resources/version").mkString("")

  // /** Parse CLI parameters and run main program.
  // *
  // * From: https://stackoverflow.com/questions/2315912/best-way-to-parse-command-line-parameters
  // */
  // def main(args: Array[String]): Unit = {
  // val res: Traversable[String] = testableMain(args, scala.io.Source.stdin)
  // res.foreach(println)
  // }

  def testableMain(args: Seq[Argument]): Seq[String] = {

    // ???: This so common parsing should be responsibility of `main`.
    val (inputArgs, otherArgs) = splitInputArgumentFromOthers(args)
    val inputString = readInputArgument(inputArgs)

    val res: Seq[String] =
      otherArgs.foldLeft(inputString)((lineSeq, x) ⇒ {
        // Console.err.println("\t\t" + lineSeq.mkString("\t\t\n"))
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

//  Run this in vim:
//
// vim source: 1,$-10s/=>/⇒/ge
// vim source: iabbrev uj ujson.Value
//
// vim: set filetype=scala fileformat=unix foldmarker={,} nowrap tabstop=2 softtabstop=2 spell spelllang=en:
