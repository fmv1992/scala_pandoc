package fmv1992.scala_pandoc

import fmv1992.scala_cli_parser.GNUParser

import org.scalatest.Tag
import org.scalatest.funsuite.AnyFunSuite

object VerboseTest extends Tag("Verbose tests.")

trait TestScalaPandoc {

  val flags = List("--farsi-to-rtl", "--evaluate", "--embed")

  val parser = GNUParser(Main.CLIConfigContents)

  def findFirst(
      e: ujson.Value
  )(f: ujson.Value => Boolean): Option[ujson.Value] = {

    val res: Option[ujson.Value] = if (f(e)) {
      Some(e)
    } else {
      e match {
        case _: ujson.Arr =>
          if (e.arr.isEmpty) None
          else e.arr.map(x => findFirst(x)(f)).reduce(_.orElse(_))
        case _: ujson.Obj =>
          if (e.obj.isEmpty) None
          else
            e.obj.values.map(x => findFirst(x)(f)).reduce(_.orElse(_))
        case _ => None
      }
    }

    res

  }

}

class TestMain extends AnyFunSuite with TestScalaPandoc {

  test("Test entry point.") {

    // Test all jsons.
    Example.allJsonsFiles.foreach(jf =>
      flags.foreach(f => {
        val cliContent = (f + " --input " + jf).split(" ").toList
        val parsed = parser.parse(cliContent)
        Main.testableMain(parsed)
      })
    )

    // assertThrows[Exception](Main.testableMain(parser.parse(List("--unexistent-cli-arg"))))
    // assertThrows[Exception](Main.testableMain(parser.parse(List("--replace-variables"))))

    // Test help and version.
    // These are taken care of at the `main` level.
    Main.main(Array("--help"))
    Main.main(Array("--version"))

  }

}
