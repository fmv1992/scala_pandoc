package fmv1992.scala_pandoc

// import fmv1992.fmv1992_scala_utilities.cli.GNUParser
// import fmv1992.fmv1992_scala_utilities.util.TestUtility

// import java.util.Random

import org.scalatest._

// import ujson._

class TestPandocJsonParsing extends FunSuite with TestConstants {

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
      PandocJsonParsing.pandocParseStringToUJson("CONTENTS CONTENTS") ==
        Example.json01changed("blocks")
    )
  }

}

//  Run this in vim:
//
// vim source: 1,$-10s/=>/⇒/ge
// vim source: iabbrev uj ujson.Value
//
// vim: set filetype=scala fileformat=unix foldmarker={,} nowrap tabstop=2 softtabstop=2 spell spelllang=en:
