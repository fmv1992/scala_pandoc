package fmv1992.scala_pandoc

import org.scalatest.funsuite.AnyFunSuite

class TestEvaluate extends AnyFunSuite with TestScalaPandoc {

  test("Test Evaluate.") {

    val cb01 = Evaluate.evaluateMarked(Example.codeblock01("blocks")(0))
    assert(cb01("c")(1).str == "hey")

    val cb02 = Evaluate.evaluateMarked(Example.jsonEvaluate01("blocks")(0))
    assert(
      cb02("c")(1).str
        == (0 until 9).map(_.toString).mkString("\n")
    )

    val cb04 = Evaluate.evaluateMarked(Example.jsonEvaluate01("blocks")(1))
    assert(
      Evaluate.evaluateMarked(cb04)("c")(1).str
        == (0 until 9).map(_.toString).mkString("")
    )

    val emptySHA1sumCommand = "da39a3ee5e6b4b0d3255bfef95601890afd80709  -"
    val cb03 = findFirst(Example.jsonEvaluate02)(x =>
      Pandoc.isPTypeGeneralCode(x)
        && PandocCode(x).content.contains("sha1sum")
        && PandocCode(x).attr.kvp.contains("pipe")
    ).getOrElse(throw new Exception())
    assert(
      Evaluate.evaluateMarked(cb03)("c")(1).str == emptySHA1sumCommand
    )

  }

  test("Test Evaluate with pipes.") {

    val cb01 = Evaluate.evaluateMarked(Example.jsonEvaluate03("blocks")(0))
    val res = cb01("c")(1).str
    assert(res == "01x3x5x7x9")

  }

  ignore("Test Evaluate expansion.") {

    val expanded01 = Evaluate.expandMarked(Example.jsonExpand01("blocks")(0))
    val expandedAndEvaluated = Pandoc.recursiveMapUJToUJIfTrue(expanded01)(
      Pandoc.isPTypeGeneralCode
    )(Evaluate.evaluateMarked)
    findFirst(expandedAndEvaluated)(x =>
      Pandoc.isPTypeGeneralCode(x) && PandocCode(x).content
        .startsWith("date")
    ).getOrElse(throw new Exception())
    findFirst(expandedAndEvaluated)(x =>
      Pandoc.isPTypeGeneralCode(x) && PandocCode(x).content
        .startsWith("Sat May")
    ).getOrElse(throw new Exception())

  }

  test("Test evaluation error.", VerboseTest) {

    assertThrows[Exception](
      Evaluate.evaluateMarked(Example.jsonEvaluate05("blocks")(0))
    )

    assertThrows[Exception](
      Evaluate.evaluateMarked(Example.jsonEvaluate07)
    )

    // The first block compiles normally.
    val block0 = Example.jsonEvaluate04("blocks")(0)
    val block1 = Example.jsonEvaluate04("blocks")(1)
    Evaluate.evaluateMarked(block0)
    // The second block only compiles if executed after the first.
    assertThrows[Exception](
      Evaluate.evaluateMarked(block1)
    )
    // But their sequence does evaluate correctly.
    Evaluate.evaluateSeq(
      PandocCode.makeScalaScript(Seq(block0, block1).mkString("\n"))
    )

  }

  test("Test Evaluate multi line code.") {

    val ev = Evaluate.evaluateMarked(Example.jsonEvaluate08)
    findFirst(ev)(x =>
      Pandoc.isPTypeGeneralCode(x) && PandocCode(x).content
        .contains("10")
    ).getOrElse(throw new Exception())
    findFirst(ev)(x =>
      Pandoc.isPTypeGeneralCode(x) && PandocCode(x).content
        .contains("A")
    ).getOrElse(throw new Exception())
    findFirst(ev)(x =>
      Pandoc.isPTypeGeneralCode(x) && PandocCode(x).content
        .contains("12")
    ).getOrElse(throw new Exception())

  }

  test("Test evaluating and non evaluating code in the same context.") {
    val e1 = Evaluate.evaluateSequentialCode(Example.jsonEvaluate09)
    findFirst(e1)(x =>
      Pandoc.isPTypeGeneralCode(x) && PandocCode(x).content
        .startsWith("def exercise23")
    ).getOrElse(throw new Exception())
    findFirst(e1)(x =>
      Pandoc.isPTypeGeneralCode(x)
        && PandocCode(x).content.startsWith("Nyquist frequency: 11025.0 Hz")
        && PandocCode(x).content.endsWith("Frequency resolution: 21.5 Hz.")
    ).getOrElse(throw new Exception())
  }

}

class TestEvaluateSerialCode extends AnyFunSuite with TestScalaPandoc {

  test("Test serial evaluation of codes in convenient Seq[String].") {
    val c1 = """
    |val a = 10
    |println(a)""".trim.stripMargin
    val c2 = """println(a + a)"""
    val s1 = Seq(c1, c2)
    val ce = Evaluate.evaluateSeq(PandocCode.makeScalaScript(s1.mkString("\n")))
    assert(ce.stdout == "10\n20")
  }

  test("Test serial evaluation of codes in a whole file.") {
    val j1 = Example.jsonEvaluate04
    val e1 = Evaluate.evaluateSequentialCode(j1)
    findFirst(e1)(x =>
      Pandoc.isPTypeGeneralCode(x) && PandocCode(x).content
        .startsWith("10")
    ).getOrElse(throw new Exception())
    findFirst(e1)(x =>
      Pandoc.isPTypeGeneralCode(x) && PandocCode(x).content
        .endsWith("20")
    ).getOrElse(throw new Exception())
  }

  test("Test serial evaluation of codes in a complex file.") {
    val j1 = Example.jsonEvaluate04
    val e1 = Evaluate.evaluateSequentialCode(j1)
    e1
  }

}

class SingleTest extends AnyFunSuite with TestScalaPandoc {}
