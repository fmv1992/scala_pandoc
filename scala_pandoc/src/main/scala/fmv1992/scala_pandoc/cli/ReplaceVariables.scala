package fmv1992.scala_pandoc

object ReplaceVariables {

  // ???: [BugReplacementOfVariables]
  def entryPoint(in: Seq[String]): Seq[String] = {
    throw new Exception("Not working, really: See [BugReplacementOfVariables].")
    // Revert to commit `commfad88b8`.
  }

  def takeReplaceValuesFromMetaBlock(
      j: ujson.Value
  ): (ujson.Value, Map[String, Seq[ujson.Value]]) = {
    val metaVars = j("replace-variables")("c")
    require(Pandoc.isUArray(metaVars), metaVars)
    val mapped: List[(String, Seq[ujson.Value])] = metaVars.arr
      .map(x ⇒ {
        require(x("t").str == "MetaInlines")
        require(x("c")(2)("c").str == "=")
        val key: String = x("c")(0)("c").str
        val valueAsArr = x("c").arr.slice(4, x("c").arr.length)
        val value: Seq[ujson.Value] = Seq(valueAsArr: _*)
        (key, value)
      })
      .toList
    (
      PandocUtilities.mapToUjsonObj(j.obj.toMap - "replace-variables"),
      mapped.toMap
    )
  }

  def recursiveReplace(
      j: ujson.Value,
      r: Map[String, Seq[ujson.Value]]
  ): ujson.Value = {
    Pandoc.recursiveMap(j)(
      (x: ujson.Value) ⇒ x match {
          case x: ujson.Arr ⇒ Pandoc.expandArray(x)(
              y ⇒ if (Pandoc.isPTypeStr(y)) r.getOrElse(y("c").str, Seq(y))
                else Seq(y)
            )
          case _ ⇒ x
        }
    )

  }

}

//  Run this in vim:
//
// vim source: 1,$-10s/=>/⇒/ge
// vim source: iabbrev uj ujson.Value
//
// vim: set filetype=scala fileformat=unix foldmarker={,} nowrap tabstop=2 softtabstop=2 spell spelllang=en:
