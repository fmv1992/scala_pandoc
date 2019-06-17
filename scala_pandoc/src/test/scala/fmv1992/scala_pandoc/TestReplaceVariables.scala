package fmv1992.scala_pandoc

import org.scalatest.Ignore

import org.scalatest._

@Ignore
class TestReplaceVariables extends FunSuite with TestScalaPandoc {

  test("Test replacement of variables.") {
    val replacedText =
      ReplaceVariables.entryPoint(List(Example.jsonreplaceVariables01.toString))
    val j01 = ujson.read(replacedText.mkString)
    assertThrows[Exception](
      findFirst(j01)(
        x ⇒ Pandoc.isPTypeStr(x) && x("c").str == "expensiveComputation."
      )
    )
  }

}
