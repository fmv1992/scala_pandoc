package fmv1992.scala_pandoc

import org.scalatest._

class TestRLFarsi extends FunSuite {

  val farsiWordThis: String = "این"
  val farsiWordHouse: String = "خانه"

  test("Basic test فارسی functionality test.") {
    val farsiStringJson: ujson.Value = Example.jsonFarsi03("blocks")(0)("c")(0)
    assert(RLFarsi.hasFarsiChar(farsiStringJson))
    assert(!RLFarsi.farsiCharSet.contains('%'))

    RLFarsi.farsiToRTL(Example.jsonFarsi03)
    // ???: There is a lack of tests for this functionality.
    // ???: Test 'Quoting only contiguous فارسی characters.'.
  }

  test("Integration test.", VerboseTest) {
    val processed = RLFarsi
      .entryPoint(Example.jsonFarsi03.render(0).lines.toSeq)
      .mkString("\n")
    assert(processed.contains(s"\\rl{${farsiWordThis}}"))
  }

}

//  Run this in vim:
//
// vim source: 1,$-10s/=>/⇒/ge
// vim source: iabbrev uj ujson.Value
//
// vim: set filetype=scala fileformat=unix foldmarker={,} nowrap tabstop=2 softtabstop=2 spell spelllang=en:
