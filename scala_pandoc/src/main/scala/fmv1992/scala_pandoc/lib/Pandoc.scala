package fmv1992.scala_pandoc

import sys.process.Process

// import ujson._
// import scala.collection.mutable.ArrayBuffer
import fmv1992.fmv1992_scala_utilities.util.Reader

object Pandoc {

  // From: https://en.wikipedia.org/wiki/JSON#Data_types,_syntax_and_example
  // 1. Number
  // 2. String
  // 3. Boolean
  // 4. Array
  // 5. Object
  // 6. Null

  // General functions. --- {

  def recursiveMapIfTrue(
      e: ujson.Value
  )(f: ujson.Value ⇒ Boolean)(g: ujson.Value ⇒ ujson.Value): ujson.Value = {

    // Unify f and g.
    def ifModElseIdentity(x: ujson.Value): ujson.Value = {
      if (f(x)) {
        protectOriginal(g)(x)
      } else {
        x
      }
    }

    recursiveMap(e, ifModElseIdentity)

  }

  // ???: Move this to a more appropriate place.
  def findFirst(
      e: ujson.Value
  )(f: ujson.Value ⇒ Boolean): Option[ujson.Value] = {

    val res: Option[ujson.Value] = if (f(e)) {
      Some(e)
    } else {
      e match {
        case _: ujson.Arr ⇒ if (e.arr.isEmpty) None
          else e.arr.map(x ⇒ findFirst(x)(f)).reduce(_.orElse(_))
        case _: ujson.Obj ⇒ if (e.obj.isEmpty) None
          else
            e.obj.values.map(x ⇒ findFirst(x)(f)).reduce(_.orElse(_))
        case _ ⇒ None
      }
    }

    res

  }

  def flatten(e: ujson.Value): IndexedSeq[ujson.Value] = {
    ???
  }

  def map(e: ujson.Value)(f: ujson.Value ⇒ ujson.Value): ujson.Value = {
    ???
  }

  def recursiveMap(
      e: ujson.Value,
      f: ujson.Value ⇒ ujson.Value
  ): ujson.Value = {

    def mapTupleToGo(x: Tuple2[String, ujson.Value]): (String, ujson.Value) = {
      (x._1, go(x._2))
    }

    def go(eGo: ujson.Value): ujson.Value = {

      val modddedeGo = f(eGo)
      eGo match {
        case _: ujson.Num ⇒ modddedeGo
        case _: ujson.Str ⇒ modddedeGo
        case _: ujson.Bool ⇒ modddedeGo
        case _: ujson.Arr ⇒ ujson.Arr(modddedeGo.arr.map(go))
        case _: ujson.Obj ⇒ ujson.Obj(
            scala.collection.mutable
              .LinkedHashMap(
                modddedeGo.obj.iterator.map(mapTupleToGo).toSeq: _*
              )
          )
        case ujson.Null ⇒ modddedeGo
      }

    }

    go(e)

  }

  def flatMap(e: ujson.Arr, f: ujson.Value ⇒ Seq[ujson.Value]): ujson.Arr = {
    val res = ujson.Arr(e.arr.flatMap(f))
    require(Pandoc.isUArray(res))
    res
  }

  // --- }

  def protectOriginal[A](f: ujson.Value ⇒ A): ujson.Value ⇒ A = {
    f compose ujson.copy
  }

  def fromString(s: String): ujson.Value = {
    // Add double quotes back.
    ujson.Str('"' + s + '"')
  }

  // ???: Deprecate. Acess with `.str`.
  def removeEnclosingQuotes(e: ujson.Value): String = {
    // Remove double quotes.
    e.toString.stripPrefix("\"").stripSuffix("\"")
  }

  // Pandoc and ujson type comparisons. --- {

  def isPType(e: ujson.Value, typeName: String): Boolean = {
    lazy val isObj = isUObject(e)
    lazy val objTypeName = removeEnclosingQuotes(
      e.obj.get("t").getOrElse("NOTYPENAME")
    )
    isObj && (removeEnclosingQuotes(objTypeName) == typeName)
  }

  def isPTypePara(e: ujson.Value): Boolean = {
    isPType(e, "Para")
  }

  def isPTypeStr(e: ujson.Value): Boolean = {
    isPType(e, "Str")
  }

  def isPTypeSpace(e: ujson.Value): Boolean = {
    isPType(e, "Space")
  }

  def isPTypeGeneralCode(e: ujson.Value): Boolean = {
    isPTypeCode(e) || isPTypeCodeBlock(e)
  }

  def isPTypeCode(e: ujson.Value): Boolean = {
    isPType(e, "Code")
  }

  def isPTypeCodeBlock(e: ujson.Value): Boolean = {
    isPType(e, "CodeBlock")
  }

  def isUArray(e: ujson.Value): Boolean = {
    e match {
      case _: ujson.Arr ⇒ true
      case _ ⇒ false
    }
  }

  def isUObject(e: ujson.Value): Boolean = {
    e match {
      case _: ujson.Obj ⇒ true
      case _ ⇒ false
    }
  }

  def isUStr(e: ujson.Value): Boolean = {
    e match {
      case _: ujson.Str ⇒ true
      case _ ⇒ false
    }
  }

  def isUIterable(e: ujson.Value): Boolean = {
    e match {
      case _: ujson.Arr ⇒ true
      case _: ujson.Obj ⇒ true
      case _: ujson.Bool ⇒ false
      case _: ujson.Num ⇒ false
      case _: ujson.Str ⇒ false
      case _: ujson.Value ⇒ false
      case _ ⇒ throw new Exception()
    }
  }

  // --- }

}

object PandocConverter {

  private def UnsafeStrToStr(
      e: ujson.Value
  )(f: String ⇒ String): ujson.Value = {
    //          Pandoc.fromString → Only needed in keys, not values because the
    //          former are quoted by pandoc.
    // e("c") = Pandoc.fromString(f(Pandoc.toString(e("c"))))
    e("c") = f(Pandoc.removeEnclosingQuotes(e("c")))
    e
  }

  def strToStr = Pandoc.protectOriginal(UnsafeStrToStr)

}

object PandocJsonParsing {

  lazy val pandoc1EmptyMessage = ujson.read(
    Reader
      .readLines("./src/main/resources/pandoc1_empty_json_message.json")
      .mkString("\n")
  )

  lazy val pandoc2EmptyMessage = ujson.read(
    Reader
      .readLines("./src/main/resources/pandoc2_empty_json_message.json")
      .mkString("\n")
  )

  def detectPandocMessageVersion(m: ujson.Value): Int = {
    if (Pandoc.isUArray(m) && m(0).obj.contains("unMeta")) 1
    else if (Pandoc.isUObject(m) && m.obj.contains("blocks")) {
      2
    } else {
      throw new Exception()
    }
  }

  def pandocParseStringToUJson(s: String): ujson.Value = {
    val proc = (Process("pandoc2 --from markdown --to json") #< PandocUtilities
      .stringToBAIS(s))
    val lines = proc.lineStream.mkString("\n")
    val j = ujson.read(lines.mkString)
    val res =
      if (detectPandocMessageVersion(j) == 1)
        j(1)
      else if (detectPandocMessageVersion(j) == 2) {
        j.obj("blocks")
      } else {
        throw new Exception()
      }
    // The returned value is an array of strings and spaces. Thus it should be
    // flatmapped in a json.
    res
  }

}

//  Run this in vim:
//
// vim source: 1,$-10s/=>/⇒/ge
// vim source: iabbrev uj ujson.Value
//
// vim: set filetype=scala fileformat=unix foldmarker={,} nowrap tabstop=2 softtabstop=2 spell spelllang=en:
