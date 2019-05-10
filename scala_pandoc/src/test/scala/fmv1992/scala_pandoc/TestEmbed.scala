package fmv1992.scala_pandoc

import org.scalatest._

class TestEmbed extends FunSuite with TestConstants {

  test("Test embedding of code block.") {

    Pandoc
      .findFirst(Embed.entryPoint(Example.embed01.split("\n")).mkString("\n"))(
        x ⇒ Pandoc.isPTypeStr(x) || x.str.contains("print(i)")
      )
      .getOrElse(throw new Exception())

    // $ date -d '2019-01-01' '+%Y-%m-%d is a %A'
    // 2019-01-01 is a Tuesday
    val j1 =
      ujson.read(Evaluate.entryPoint(Example.embed02.split("\n")).mkString)
    val isTuesdayJSONInCode = Pandoc
      .findFirst(j1)(x ⇒ {
        Pandoc.isPTypeGeneralCode(x) && PandocCode(x).content
          .contains("Tuesday")
      })
      .getOrElse(throw new Exception())
    assert(isTuesdayJSONInCode("c").arr.last.str.contains("is a Tuesday"))

    val j2 = ujson.read(Embed.entryPoint(j1.render(0).split("\n")).mkString)
    Pandoc
      .findFirst(j2)(x ⇒ {
        Pandoc.isPTypeStr(x) && x("c").str.contains("Tuesday")
      })
      .getOrElse(throw new Exception())
    Pandoc
      .findFirst(j2)(
        x ⇒ Pandoc.isUObject(x) && x.obj
            .contains("t") && x.obj("t").str == "BulletList"
      )
      .getOrElse(throw new Exception())

  }

}

//  Run this in vim:
//
// vim source: 1,$-10s/=>/⇒/ge
// vim source: iabbrev uj ujson.Value
//
// vim: set filetype=scala fileformat=unix foldmarker={,} nowrap tabstop=2 softtabstop=2 spell spelllang=en:
