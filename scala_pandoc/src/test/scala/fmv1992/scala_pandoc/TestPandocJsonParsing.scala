package fmv1992.scala_pandoc

import org.scalatest._

class TestPandocJsonParsing extends FunSuite with TestScalaPandoc {

  test("Test PandocJsonParsing.") {
    assert(
      PandocJsonParsing
        .detectPandocMessageVersion(PandocJsonParsing.pandoc1EmptyMessage) == 1
    )
    assert(
      PandocJsonParsing
        .detectPandocMessageVersion(PandocJsonParsing.pandoc2EmptyMessage) == 2
    )
  }

  test("Test running single string through pandoc.") {
    assert(
      PandocJsonParsing.pandocParseMarkdownToUJson("CONTENTS CONTENTS") ==
        Example.json01changed("blocks")
    )
  }

}
