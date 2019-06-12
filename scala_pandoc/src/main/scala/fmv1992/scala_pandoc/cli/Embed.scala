package fmv1992.scala_pandoc

object Embed extends PandocScalaMain {

  // ???: Allow this to be specified via CLI or env var.
  val actionMark = "embed"

  def entryPoint(in: Seq[String]): Seq[String] = {
    val text = in.mkString("\n")
    val embedded =
      Pandoc.recursiveMapUJToUJIfTrue(ujson.read(text))(Pandoc.isUArray)(
        x ⇒ Pandoc.expandArray(x)(embedIfMarked)
      )
    val ret = embedded.toString.split("\n")
    ret
  }

  def recursiveEmbed(j: ujson.Value): ujson.Value = {
    Pandoc.recursiveMapUJToUJ(j)(
      (x: ujson.Value) ⇒ x match {
          case x: ujson.Arr ⇒ Pandoc.expandArray(x)(embedIfMarked)
          case _ ⇒ x
        }
    )
  }

  // ???: Use recursiveMapUJToUJIfTrue
  def embedIfMarked(j: ujson.Value): Seq[ujson.Value] = {
    val res = if (Pandoc.isPTypeGeneralCode(j)) {
      val cb = PandocCode(j)
      if (cb.attr.hasClass(actionMark)) {
        val res = PandocJsonParsing.pandocParseMarkdownToUJson(cb.content)
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
