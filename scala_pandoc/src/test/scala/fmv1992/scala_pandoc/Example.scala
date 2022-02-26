package fmv1992.scala_pandoc

import java.io.File

/** Load example files. */
object Example {

  lazy val gitRoot = {
    var gr = new java.io.File(System.getProperty("user.dir"))
    while (!gr.list.contains(".git")) {
      gr = new java.io.File(gr.getParent)
    }
    gr
  }

  lazy val string01 = scala.io.Source
    .fromFile(
      new java.io.File(gitRoot, "./tmp/example_01.json")
    )
    .mkString("\n")
  lazy val json01 = ujson.read(string01)

  lazy val string01changed = scala.io.Source
    .fromFile(
      new java.io.File(gitRoot, "./tmp/example_01_changed.json")
    )
    .mkString("\n")
  lazy val json01changed = ujson.read(string01changed)

  lazy val string02 = scala.io.Source
    .fromFile(
      new java.io.File(gitRoot, "./tmp/example_02.json")
    )
    .mkString("\n")
  lazy val json02 = ujson.read(string02)

  lazy val string03 = scala.io.Source
    .fromFile(
      new java.io.File(
        gitRoot,
        "./tmp/example_03_code_block.json"
      )
    )
    .mkString("\n")
  lazy val codeblock01 = ujson.read(string03)

  lazy val farsi01 = scala.io.Source
    .fromFile(
      new java.io.File(
        gitRoot,
        "./tmp/example_farsi_01_with_curly_expressions.json"
      )
    )
    .mkString("\n")
  lazy val jsonFarsi01 = ujson.read(farsi01)

  lazy val farsi02 = scala.io.Source
    .fromFile(
      new java.io.File(
        gitRoot,
        "./tmp/example_farsi_02_mixed_farsi.json"
      )
    )
    .mkString("\n")
  lazy val jsonFarsi02 = ujson.read(farsi02)

  lazy val farsi03 = scala.io.Source
    .fromFile(
      new java.io.File(
        gitRoot,
        "./tmp/example_farsi_03.json"
      )
    )
    .mkString("\n")
  lazy val jsonFarsi03 = ujson.read(farsi03)

  lazy val evaluate01 = scala.io.Source
    .fromFile(
      new java.io.File(
        gitRoot,
        "tmp/example_01_evaluate.json"
      )
    )
    .mkString("\n")
  lazy val jsonEvaluate01 = ujson.read(evaluate01)

  lazy val evaluate02 = scala.io.Source
    .fromFile(
      new java.io.File(
        gitRoot,
        "tmp/example_02_evaluate.json"
      )
    )
    .mkString("\n")
  lazy val jsonEvaluate02 = ujson.read(evaluate02)

  lazy val evaluate03 = scala.io.Source
    .fromFile(
      new java.io.File(
        gitRoot,
        "tmp/example_03_evaluate.json"
      )
    )
    .mkString("\n")
  lazy val jsonEvaluate03 = ujson.read(evaluate03)

  lazy val embed01 = scala.io.Source
    .fromFile(
      new java.io.File(
        gitRoot,
        "tmp/example_01_embed.json"
      )
    )
    .mkString("\n")
  lazy val jsonEmbed01 = ujson.read(evaluate01)

  lazy val evaluate04 = scala.io.Source
    .fromFile(
      new java.io.File(
        gitRoot,
        "tmp/example_04_evaluate.json"
      )
    )
    .mkString("\n")
  lazy val jsonEvaluate04 = ujson.read(evaluate04)

  lazy val evaluate05 = scala.io.Source
    .fromFile(
      new java.io.File(
        gitRoot,
        "tmp/example_05_evaluate.json"
      )
    )
    .mkString("\n")
  lazy val jsonEvaluate05 = ujson.read(evaluate05)

  lazy val evaluate06 = scala.io.Source
    .fromFile(
      new java.io.File(
        gitRoot,
        "tmp/example_06_evaluate.json"
      )
    )
    .mkString("\n")
  lazy val jsonEvaluate06 = ujson.read(evaluate06)

  lazy val evaluate07 = scala.io.Source
    .fromFile(
      new java.io.File(
        gitRoot,
        "tmp/example_07_from_fom_has_error.json"
      )
    )
    .mkString("\n")
  lazy val jsonEvaluate07 = ujson.read(evaluate07)

  lazy val evaluate08 = scala.io.Source
    .fromFile(
      new java.io.File(
        gitRoot,
        "tmp/example_08_multiline_prints.json"
      )
    )
    .mkString("\n")
  lazy val jsonEvaluate08 = ujson.read(evaluate08)

  lazy val evaluate09 = scala.io.Source
    .fromFile(
      new java.io.File(
        gitRoot,
        "tmp/example_09_from_fom.json"
      )
    )
    .mkString("\n")
  lazy val jsonEvaluate09 = ujson.read(evaluate09)

  lazy val expand01 = scala.io.Source
    .fromFile(
      new java.io.File(
        gitRoot,
        "tmp/example_expand_01.json"
      )
    )
    .mkString("\n")
  lazy val jsonExpand01 = ujson.read(expand01)

  lazy val embed02 = scala.io.Source
    .fromFile(
      new java.io.File(
        gitRoot,
        "tmp/example_02_embed.json"
      )
    )
    .mkString("\n")
  lazy val jsonEmbed02 = ujson.read(evaluate02)

  lazy val replaceVariables01 = scala.io.Source
    .fromFile(
      new java.io.File(
        gitRoot,
        "tmp/example_replace_variables_01.json"
      )
    )
    .mkString("\n")
  lazy val jsonreplaceVariables01 = ujson.read(replaceVariables01)

  // ???: Reading files twice.
  lazy val allJsonsFilesIncludingInvalid =
    new File(gitRoot, "./tmp/")
      .listFiles(_.getPath.endsWith(".json"))
  lazy val allJsonsFiles = allJsonsFilesIncludingInvalid.filter(x =>
    !scala.io.Source
      .fromFile(x)
      .mkString("\n")
      .contains("scala_pandoc_test_error_mark")
  )
  lazy val allJsons: List[ujson.Value] =
    allJsonsFiles
      .map(x => ujson.read(scala.io.Source.fromFile(x).mkString("\n")))
      .toList

  allJsonsFilesIncludingInvalid.foreach(x => require(x.exists, x))

}
