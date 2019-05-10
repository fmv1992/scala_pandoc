package fmv1992.scala_pandoc

// import fmv1992.fmv1992_scala_utilities.cli.GNUParser
// import fmv1992.fmv1992_scala_utilities.util.TestUtility

// import java.util.Random

import org.scalatest._

// import ujson._

class TestEvaluate extends FunSuite with TestConstants {

  test("Test Evaluate.") {

    val cb01 = Evaluate.explainIfMarked(Example.codeblock01)(0)("blocks")(0)
    assert(Evaluate.explainIfMarked(cb01)(0)("c")(1).str == "hey")

    val cb02 = Evaluate.explainIfMarked(Example.jsonEvaluate01)(0)("blocks")(0)
    assert(
      Evaluate.explainIfMarked(cb02)(0)("c")(1).str
        == (0 until 3).map(_.toString).mkString("\n")
    )

    val emptySHA1sumCommand = "da39a3ee5e6b4b0d3255bfef95601890afd80709  -"
    val cb03 = Pandoc
      .findFirst(Example.jsonEvaluate02)(
        x ⇒ Pandoc.isPTypeGeneralCode(x)
            && PandocCode(x).content.contains("sha1sum")
            && PandocCode(x).attr.kvp.contains("pipe")
      )
      .getOrElse(throw new Exception())
    assert(Evaluate.explainIfMarked(cb03)(0)("c")(1).str == emptySHA1sumCommand)

  }

  test("Test Evaluate with pipes.") {

    val cb01 = Evaluate.explainIfMarked(Example.jsonEvaluate03)(0)("blocks")(0)
    val res = Evaluate.explainIfMarked(cb01)(0)("c")(1).str
    assert(res == "01x3x5x7x9")

  }

}

//  Run this in vim:
//
// vim source: 1,$-10s/=>/⇒/ge
// vim source: iabbrev uj ujson.Value
//
// vim: set filetype=scala fileformat=unix foldmarker={,} nowrap tabstop=2 softtabstop=2 spell spelllang=en:
