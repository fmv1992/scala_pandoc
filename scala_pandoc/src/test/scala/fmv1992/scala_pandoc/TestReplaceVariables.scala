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

//  Run this in vim:
//
// vim source: 1,$-10s/=>/⇒/ge
// vim source: iabbrev uj ujson.Value
//
// vim: set filetype=scala fileformat=unix foldmarker={,} nowrap tabstop=2 softtabstop=2 spell spelllang=en:
