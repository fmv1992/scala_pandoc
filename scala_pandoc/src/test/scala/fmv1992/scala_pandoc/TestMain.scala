package fmv1992.scala_pandoc

import fmv1992.fmv1992_scala_utilities.cli.GNUParser
// import fmv1992.fmv1992_scala_utilities.util.TestUtility

// import java.util.Random

import org.scalatest._

// import ujson._

trait TestConstants {

  val flags = List("--farsi-to-rtl", "--evaluate")

}

class TestMain extends FunSuite with TestConstants {

  test("Test entry point.") {

    // Test all jsons.
    val parser = GNUParser(Main.CLIConfigPath)
    Example.allJsonsFiles.foreach(
      jf ⇒ flags.foreach(f ⇒ {
          val cliContent = (f + " --input " + jf).split(" ").toList
          val parsed = parser.parse(cliContent)
          Main.testableMain(parsed)
        })
    )

    // ???: Re enable those.
    // // Test help and version.
    // Main.testableMain(parser.parse(List("--help")))
    // Main.testableMain(parser.parse(List("--version")))
    // Main.main(Array("--version"))

  }

  test("Test stdin and other branches to improve code coverage.") {

    // ???: Re enable.
    // assertThrows[Exception](Main.main(Array("--unexistent-cli-arg")))
    // val mockedStdin = Example.jsonFarsi03.toString.toList
    // TestUtility.mockStdin(Main.main(Array("--farsi-to-rtl")), mockedStdin)

  }

}

//  Run this in vim:
//
// vim source: 1,$-10s/=>/⇒/ge
// vim source: iabbrev uj ujson.Value
//
// vim: set filetype=scala fileformat=unix foldmarker={,} nowrap tabstop=2 softtabstop=2 spell spelllang=en:
