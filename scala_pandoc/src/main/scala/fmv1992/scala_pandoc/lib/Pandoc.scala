package fmv1992.scala_pandoc

import sys.process.Process

import fmv1992.fmv1992_scala_utilities.util.Reader

object Pandoc {

  // From: https://en.wikipedia.org/wiki/JSON#Data_types,_syntax_and_example
  // 1. Number
  // 2. String
  // 3. Boolean
  // 4. Array
  // 5. Object
  // 6. Null

  // Primitives. --- {

  def recursiveMap[A](
      e: ujson.Value
  )(f: ujson.Value ⇒ A): A = {
    ???
  }

  def recursiveMapUJToUJ(
      e: ujson.Value
  )(f: ujson.Value ⇒ ujson.Value): ujson.Value = {

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
        case _: ujson.Obj ⇒ PandocUtilities.mapToUjsonObj(
            modddedeGo.obj.iterator.map(mapTupleToGo).toMap
          )
        case ujson.Null ⇒ modddedeGo
      }

    }

    go(e)

  }

  def expandArray(
      e: ujson.Value
  )(f: ujson.Value ⇒ Seq[ujson.Value]): ujson.Value = {
    val res = ujson.Arr(e.arr.flatMap(f))
    require(Pandoc.isUArray(res))
    res
  }

  // --- }

  // Utility functions. --- {

  def recursiveMapUJToUJIfTrue(
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

    recursiveMapUJToUJ(e)(ifModElseIdentity)

  }

  def protectOriginal[A](f: ujson.Value ⇒ A): ujson.Value ⇒ A = {
    f compose ujson.copy
  }

  // --- }

  // Pandoc and ujson type comparisons. --- {

  private def isPType(e: ujson.Value, typeName: String): Boolean = {
    lazy val isObj = isUObject(e)
    lazy val objTypeName = e.obj.get("t").map(_.str).getOrElse("NOTYPENAME")
    isObj && (objTypeName == typeName)
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

  private def UnsafeSetString(
      e: ujson.Value
  )(f: String ⇒ String): ujson.Value = {
    e("c") = f(e("c").str)
    e
  }

  def immutableSetString = Pandoc.protectOriginal(UnsafeSetString)

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

  def pandocParseMarkdownToUJson(s: String): ujson.Value = {
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
