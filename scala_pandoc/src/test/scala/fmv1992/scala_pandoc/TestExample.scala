package fmv1992.scala_pandoc

import org.scalatest._

class TestExample extends FunSuite {

  val x1 = ujson.read(""" {"t":"Str","c":"abcde"} """)
  val x2 = ujson.read("""{"t":"Str","c":"abcde"}""")

  test("Test ujson comparison.") {
    assert(x1 == x2)
  }

  test("Test basic functions.") {

    assert(Pandoc.isUIterable(x1))
    assert(!Pandoc.isUIterable(x1("t")))

    assert(Pandoc.isUIterable(Example.json01("blocks")))
    assert(Pandoc.isUIterable(Example.json01("blocks")(0)))
    assert(Pandoc.isPTypePara(Example.json01("blocks")(0)))
    assert(Pandoc.isPTypeStr(Example.json01("blocks")(0)("c")(0)))

  }

  test("Test changing strings to uppercase.") {

    val textParagraph = Example.json01("blocks")(0)("c")(0)
    val modABCDE =
      PandocConverter.immutableSetString(textParagraph)(x ⇒ "abcde")
    assert(modABCDE === ujson.read(""" {"t":"Str","c":"abcde"} """))
    // Assert that modification was not inplace.
    assert(modABCDE != textParagraph)
    val modUpper =
      PandocConverter.immutableSetString(textParagraph)(x ⇒ x.toUpperCase)
    assert(modUpper === ujson.read(""" {"t":"Str","c":"PARAGRAPH"} """))

  }

  test("Test recursive recursiveMapIfTrue.") {
    val copyJson01 = ujson.copy(Example.json01)
    assert(
      Example.json01 == Pandoc.recursiveMapIfTrue(Example.json01)(x ⇒ true)(
        identity _
      )
    )
    assert(
      Example.json02 == Pandoc.recursiveMapIfTrue(Example.json02)(x ⇒ true)(
        identity _
      )
    )

    val changeContentstoCONTENTS = ((x: ujson.Value) ⇒ {
      x("c") = "CONTENTS"
      x
    })
    assert(
      Pandoc.recursiveMapIfTrue(Example.json01)(Pandoc.isPTypeStr)(
        changeContentstoCONTENTS
      )
        == ujson.read(Example.json01changed)
    )
    assert(copyJson01 == Example.json01)
  }

  test("Test map.") {
    val copyJson01 = ujson.copy(Example.json01)
    assert(
      Example.json01 == Pandoc.recursiveMapIfTrue(Example.json01)(x ⇒ true)(
        identity _
      )
    )
    assert(
      Example.json02 == Pandoc.recursiveMapIfTrue(Example.json02)(x ⇒ true)(
        identity _
      )
    )

    val changeContentstoCONTENTS = ((x: ujson.Value) ⇒ {
      x("c") = "CONTENTS"
      x
    })
    assert(
      Pandoc.recursiveMapIfTrue(Example.json01)(Pandoc.isPTypeStr)(
        changeContentstoCONTENTS
      )
        == ujson.read(Example.json01changed)
    )
    assert(copyJson01 == Example.json01)
  }

  test("Test flatMap.") {
    assert(
      Pandoc.expandArray(ujson.Arr(x1))((a ⇒ List(a, a)))
        == ujson.read("[" + x1.toString + "," + x1.toString + "]")
    )
  }

  test("Test PandocAttributes parsing.") {

    val x = Example.codeblock01("blocks")(0)("c")(0)
    val cb01Attributes = PandocAttributes(
      "mycode",
      List("bash", "numberLines", "attra", "attrb", "attrc"),
      Map(("pipe", "sh"), ("startFrom", "100"), ("k1", "v1"), ("k2", "v2"))
    )
    assert(PandocAttributes(x) == cb01Attributes)
    assert(x == cb01Attributes.toUJson)
  }

  test("Test PandocCode identity.") {
    val x = Example.codeblock01
    assert(x("blocks")(0) == PandocCode(x("blocks")(0)).toUJson)
  }

}
