package fmv1992.scala_pandoc

// import fmv1992.fmv1992_scala_utilities.util.Reader
import fmv1992.fmv1992_scala_utilities.util.Utilities

// import ujson._

case class PandocFarsi(s: String) extends PandocElement {

  require(s.forall(RLFarsi.farsiCharSet.contains), s)

  def toUJson: ujson.Value = {
    val enclosedS = "\\rl{" + s + "}"
    // val enclosedS = encloseContiguousFarsiCharacters
    RawInline("tex", enclosedS).toUJson
  }

  def +(that: PandocFarsi): PandocFarsi = PandocFarsi(this.s + that.s)
}

object PandocFarsi {

  def apply(j: ujson.Value): PandocFarsi = {
    require(Pandoc.isPTypeStr(j), j)
    val s: String = j("c").str
    PandocFarsi(s)
  }

}

object RLFarsi {

  type E = Either[ujson.Value, PandocFarsi]

  // Main related functions. --- {

  // ???: (note01): Shipping for "pandoc 1.16.0.2": it has to be shipped as an
  // executable filter and be used as a `--filter` parameter as pandocs
  // arguments.
  //
  // ???: Modularize entry points; make them composable (thus do not return
  // Unit).
  def entryPoint(in: Seq[String]): Seq[String] = {
    val text = in.mkString("")
    val res = farsiToRTL(ujson.read(text))
    val retVal = res.toString.split("\n")
    retVal
  }

  def farsiToRTL(j: ujson.Value): ujson.Value = {
    Pandoc.recursiveMap(j)(
      (x: ujson.Value) ⇒ x match {
          case x: ujson.Arr ⇒ Pandoc.expandArray(x)(transformToEscapedRL)
          case _ ⇒ x
        }
    )
  }

  /** Aggregate sucession of فارسی block and spaces.
    *
    * ???: PandocFarsi may have other characters on it that may interfere with
    * `--to latex` (such as '%').
    *
    */
  def transformToEscapedRL(j: ujson.Value): Seq[ujson.Value] = {
    // Act on strings.
    if (Pandoc.isPTypeStr(j)) {
      if (hasFarsiChar(j)) {
        val s = j("c").str
        val isFarsiCharList: List[Boolean] =
          s.map(RLFarsi.farsiCharSet.contains(_)).toList
        val indexes = Utilities.getContiguousElementsIndexes(isFarsiCharList)
        indexes.map(x ⇒ {
          val content = s.slice(x._1, x._2)
          if (isFarsiString(s(x._1).toString)) {
            PandocFarsi(content).toUJson
          } else {
            PandocUtilities.mapToUjsonObj(
              Map("t" → ujson.Str("Str"), "c" → ujson.Str(content))
            )
          }
        })
      } else {
        Seq(j)
      }
    } else {
      Seq(j)
    }
  }

  // --- }

  // General helper functions and constants. --- {

  private def hasCharsInSet(str: String, set: Set[Char]): Boolean = {
    str.exists(set contains _)
  }

  private def isMadeOfCharsInCharset(str: String, set: Set[Char]): Boolean = {
    str.forall(set contains _)
  }

  def hasFarsiChar(j: ujson.Value): Boolean = {
    require(Pandoc.isPTypeStr(j), j)
    val content = j("c").str
    hasFarsiChar(content)
  }

  def hasFarsiChar(str: String): Boolean = {
    hasCharsInSet(str, farsiCharSet)
  }

  def isFarsiString(str: String): Boolean = {
    isMadeOfCharsInCharset(str, farsiCharSet)
  }

  // Farsi charset. --- {
  // https://stackoverflow.com/questions/10561590/regex-for-check-the-input-string-is-just-in-persian-language
  // https://stackoverflow.com/a/26464912/5544140
  val farsiCharSet: Set[Char] =
    ((0x0600 to 0x06FF).toSet ++ Set(0xFB8A, 0x067E, 0x0686, 0x06AF))
      .map(_.toChar)
  // --- }

  // --- }

  // Trasform mixed farsi fields into Sequence of PandocFarsi and Str. --- {
  // --- }

}

//  Run this in vim:
//
// vim source: 1,$-10s/=>/⇒/ge
// vim source: iabbrev uj ujson.Value
//
// vim: set filetype=scala fileformat=unix foldmarker={,} nowrap tabstop=2 softtabstop=2 spell spelllang=en:
