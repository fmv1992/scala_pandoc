// https://hackage.haskell.org/package/pandoc-types-1.19/docs/Text-Pandoc-Definition.html#t:Attr

package fmv1992.scala_pandoc

trait PandocElement {

  def toUJson: ujson.Value

}

// https://hackage.haskell.org/package/pandoc-types-1.19/docs/Text-Pandoc-Definition.html#t:Attr
//   Attributes: identifier, classes, key-value pairs
case class PandocAttributes(
    identifier: String,
    classes: List[String],
    kvp: Map[String, String]
) {

  def removeClass(c: String): PandocAttributes = {
    require(classes.contains(c))
    val newL: List[String] = classes.diff(Seq(c))
    PandocAttributes(identifier, newL, kvp)
  }

  def addClass(c: String): PandocAttributes = {
    PandocAttributes(identifier, classes :+ c, kvp)
  }

  def removeKey(c: String): PandocAttributes = {
    require(kvp.contains(c))
    val newKVP = kvp - c
    PandocAttributes(identifier, classes, newKVP)
  }

  def addKey(c: String): PandocAttributes = {
    require(kvp.contains(c))
    val newKVP = kvp - c
    PandocAttributes(identifier, classes, newKVP)
  }

  def hasClass(c: String): Boolean = this.classes.contains(c)

  def hasKey(c: String): Boolean = this.kvp.contains(c)

  def toUJson: ujson.Value = {
    (ujson.Arr(identifier).arr
      ++ ujson.Arr(classes).arr
      ++ ujson.Arr(kvp.toList.map(x ⇒ List(x._1, x._2))).arr)
  }

}

object PandocAttributes {

  def apply(j: ujson.Value): PandocAttributes = {
    val identifier: String = j(0).str
    // val metadata: List[ujson.Value] = j(1).arr.toList
    val classes: List[String] = j(1).arr.map(_.str).toList
    val kvp: Map[String, String] = Map(
      j(2).arr
        .map((x: ujson.Value) ⇒ x.arr.map(_.str))
        .map(x ⇒ (x(0), x(1))): _*
    )
    PandocAttributes(identifier, classes, kvp)
  }

}

// https://hackage.haskell.org/package/pandoc-types-1.19/docs/Text-Pandoc-Definition.html:
// CodeBlock Attr String
// After: `08934d1` This used to be PandocCode. At any rate conversion of
// "inline code" and paragraphs code seem to be equal in terms of pandoc2 json.
case class PandocCode(
    attr: PandocAttributes,
    content: String,
    pandocType: String
) extends PandocElement {

  def toUJson: ujson.Value = {
    val c: ujson.Value = ujson.Arr(attr.toUJson).arr ++ ujson.Arr(content).arr
    val l: scala.collection.mutable.LinkedHashMap[String, ujson.Value] = {
      scala.collection.mutable
        .LinkedHashMap(List(("t", ujson.Str(pandocType)), ("c", c)): _*)
    }
    ujson.Obj(l)
  }

  def changeContent(newContent: String): PandocCode = {
    PandocCode(attr, newContent, pandocType)
  }

}

object PandocCode {

  def apply(j: ujson.Value): PandocCode = {
    val pa: PandocAttributes = PandocAttributes(j("c")(0))
    val code: String = j("c")(1).str
    val codeType: String = j("t").str
    PandocCode(pa, code, codeType)
  }

}

case class PandocStr(content: String) extends PandocElement {

  def toUJson: ujson.Value = ???

}

object PandocStr {

  def apply(j: ujson.Value): PandocStr = {
    val string: String = j("c").str
    PandocStr(string)
  }

}

case class RawInline(inlineType: String, content: String)
    extends PandocElement {

  def toUJson: ujson.Value = {
    val c: ujson.Value = ujson.Arr(ujson.Str(inlineType)).arr ++ ujson
      .Arr(content)
      .arr
    val l: scala.collection.mutable.LinkedHashMap[String, ujson.Value] = {
      scala.collection.mutable
        .LinkedHashMap(List(("t", ujson.Str("RawInline")), ("c", c)): _*)
    }
    ujson.Obj(l)
  }

}

object RawInline {

  def apply(j: ujson.Value): RawInline = {
    ???
  }

}
