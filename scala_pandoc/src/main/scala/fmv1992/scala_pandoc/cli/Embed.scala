package fmv1992.scala_pandoc

object Embed {

  // ???: Allow this to be specified via CLI or env var.
  val actionMark = "embed"

  def entryPoint(in: Seq[String]): Seq[String] = {
    val text = in.mkString("\n")
    // val embedded = recursiveEmbed()
    val embedded = Pandoc.recursiveMapIfTrue(ujson.read(text))(Pandoc.isUArray)(
      x ⇒ Pandoc.flatMap(x, embedIfMarked)
    )
    val ret = embedded.toString.split("\n")
    ret
  }

  def recursiveEmbed(j: ujson.Value): ujson.Value = {
    Pandoc.recursiveMap(
      j,
      (x: ujson.Value) ⇒ x match {
          case x: ujson.Arr ⇒ Pandoc.flatMap(x, embedIfMarked)
          case _ ⇒ x
        }
    )
  }

  // ???: Use recursiveMapIfTrue
  def embedIfMarked(j: ujson.Value): Seq[ujson.Value] = {
    val res = if (Pandoc.isPTypeCodeBlock(j) || Pandoc.isPTypeCode(j)) {
      val cb = PandocCode(j)
      if (cb.attr.hasClass(actionMark)) {
        val res = PandocJsonParsing.pandocParseStringToUJson(cb.content)
        require(Pandoc.isUArray(res), res)
        // Transform code block back to either paragraph or normal string.
        if (cb.pandocType == "CodeBlock") {

          Seq(res.arr: _*)

        } else {

          Seq((res(0)("c").arr): _*)

        }
      } else {
        Seq(j)
      }
    } else {
      Seq(j)
    }
    res
  }

}

//  Run this in vim:
//
// vim source: 1,$-10s/=>/⇒/ge
// vim source: iabbrev uj ujson.Value
//
// vim: set filetype=scala fileformat=unix foldmarker={,} nowrap tabstop=2 softtabstop=2 spell spelllang=en:
