package fmv1992.scala_pandoc

// import fmv1992.fmv1992_scala_utilities.cli.GNUParser
// import fmv1992.fmv1992_scala_utilities.util.TestUtility

// import java.util.Random

import org.scalatest._

// import ujson._

class TestEvaluate extends FunSuite with TestConstants {

  test("Test Evaluate.") {

    val cb01 = Evaluate.evaluateIfMarked(Example.codeblock01)(0)("blocks")(0)
    assert(Evaluate.evaluateIfMarked(cb01)(0)("c")(1).str == "hey")

    val cb02 = Evaluate.evaluateIfMarked(Example.jsonEvaluate01)(0)("blocks")(0)
    assert(
      Evaluate.evaluateIfMarked(cb02)(0)("c")(1).str
        == (0 until 9).map(_.toString).mkString("\n")
    )

    val cb04 = Evaluate.evaluateIfMarked(Example.jsonEvaluate01)(0)("blocks")(1)
    assert(
      Evaluate.evaluateIfMarked(cb04)(0)("c")(1).str
        == (0 until 9).map(_.toString).mkString("")
    )

    val emptySHA1sumCommand = "da39a3ee5e6b4b0d3255bfef95601890afd80709  -"
    val cb03 = Pandoc
      .findFirst(Example.jsonEvaluate02)(
        x ⇒ Pandoc.isPTypeGeneralCode(x)
            && PandocCode(x).content.contains("sha1sum")
            && PandocCode(x).attr.kvp.contains("pipe")
      )
      .getOrElse(throw new Exception())
    assert(
      Evaluate.evaluateIfMarked(cb03)(0)("c")(1).str == emptySHA1sumCommand
    )

  }

  test("Test Evaluate with pipes.") {

    val cb01 = Evaluate.evaluateIfMarked(Example.jsonEvaluate03)(0)("blocks")(0)
    val res = Evaluate.evaluateIfMarked(cb01)(0)("c")(1).str
    assert(res == "01x3x5x7x9")

  }

  test("Test Evaluate expansion.") {

    val expanded01 = Evaluate.expandIfMarked(Example.jsonExpand01("blocks")(0))
    val expandedAndEvaluated =
      Pandoc.flatMap(expanded01, Evaluate.evaluateIfMarked)
    Pandoc
      .findFirst(expandedAndEvaluated)(
        x ⇒ Pandoc.isPTypeGeneralCode(x) && PandocCode(x).content
            .startsWith("date")
      )
      .getOrElse(throw new Exception())
    Pandoc
      .findFirst(expandedAndEvaluated)(
        x ⇒ Pandoc.isPTypeGeneralCode(x) && PandocCode(x).content
            .startsWith("Sat May")
      )
      .getOrElse(throw new Exception())

  }

  ignore("Test evaluation error.") {

    assertThrows[Exception](
      Evaluate.evaluateIfMarked(Example.jsonEvaluate05("blocks")(0))
    )

    // The first block compiles normally.
    val block0 = Example.jsonEvaluate04("blocks")(0)
    val block1 = Example.jsonEvaluate04("blocks")(1)
    Evaluate.evaluateIfMarked(block0)
    // The second block only compiles if executed after the first.
    println(Evaluate.evaluateIfMarked(block1))
    assertThrows[Exception](
      Evaluate.evaluateIfMarked(block1)
    )
    // But their sequence does evaluate correctly.
    Evaluate.evaluateSeq(Seq(block0, block1).map(x ⇒ PandocCode(x).content))

  }

}

class TestEvaluateSerialCode extends FunSuite with TestConstants {

  test("Test serial evaluation of codes.") {
    val c1 = """
    |val a = 10
    |println(a)""".trim.stripMargin
    val c2 = """println(a + a)"""
    val s1 = Seq(c1, c2)
    assert(Evaluate.evaluateSeq(s1).mkString("\n") == "10\n20")
  }

}

//  Run this in vim:
//
// vim source: 1,$-10s/=>/⇒/ge
// vim source: iabbrev uj ujson.Value
//
// vim: set filetype=scala fileformat=unix foldmarker={,} nowrap tabstop=2 softtabstop=2 spell spelllang=en:
