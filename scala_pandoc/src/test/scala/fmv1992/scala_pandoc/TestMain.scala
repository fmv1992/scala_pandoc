package fmv1992.scala_pandoc

import fmv1992.fmv1992_scala_utilities.cli.GNUParser

import org.scalatest._

object VerboseTest extends Tag("Verbose tests.")

trait TestConstants {

  val flags = List("--farsi-to-rtl", "--evaluate", "--embed")
  val parser = GNUParser(Main.CLIConfigPath)

}

class TestMain extends FunSuite with TestConstants {

  test("Test entry point.") {

    // Test all jsons.
    Example.allJsonsFiles.foreach(
      jf ⇒ flags.foreach(f ⇒ {
          val cliContent = (f + " --input " + jf).split(" ").toList
          val parsed = parser.parse(cliContent)
          Main.testableMain(parsed)
        })
    )

    // assertThrows[Exception](Main.testableMain(parser.parse(List("--unexistent-cli-arg"))))
    // assertThrows[Exception](Main.testableMain(parser.parse(List("--replace-variables"))))

    // Test help and version.
    // These are taken care of at the `main` level.
    Main.main(Array("--help"))
    Main.main(Array("--version"))

  }

}

//  Run this in vim:
//
// vim source: 1,$-10s/=>/⇒/ge
// vim source: iabbrev uj ujson.Value
//
// vim: set filetype=scala fileformat=unix foldmarker={,} nowrap tabstop=2 softtabstop=2 spell spelllang=en:
