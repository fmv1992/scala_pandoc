package fmv1992.scala_pandoc

import org.scalatest.funsuite.AnyFunSuite

class TestRLFarsi extends AnyFunSuite {

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
      .entryPoint(Example.jsonFarsi03.render(0).split("\n").toSeq)
      .mkString("\n")
    assert(processed.contains(s"\\rl{${farsiWordThis}}"))
  }

}
