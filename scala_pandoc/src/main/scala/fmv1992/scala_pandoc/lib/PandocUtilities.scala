package fmv1992.scala_pandoc

object PandocUtilities {

  private def mapToLinkedHashMap[A, B](
      m: Map[A, B]
  ): scala.collection.mutable.LinkedHashMap[A, B] = {
    scala.collection.mutable.LinkedHashMap(m.toList: _*)
  }

  def mapToUjsonObj(m: Map[String, ujson.Value]): ujson.Value = {
    ujson.Obj(mapToLinkedHashMap(m))
  }

  def stringToBAIS(a: String): java.io.ByteArrayInputStream =
    new java.io.ByteArrayInputStream(a.getBytes)
}

//  Run this in vim:
//
// vim source: 1,$-10s/=>/⇒/ge
// vim source: iabbrev uj ujson.Value
//
// vim: set filetype=scala fileformat=unix foldmarker={,} nowrap tabstop=2 softtabstop=2 spell spelllang=en:
