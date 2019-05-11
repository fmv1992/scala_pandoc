package fmv1992.scala_pandoc

// import ujson._

object PandocUtilities {

  lazy val gitRoot = {
    var gr = new java.io.File(System.getProperty("user.dir"))
    while (!gr.list.contains(".git")) {
      gr = new java.io.File(gr.getParent)
    }
    gr
  }

  private def mapToLinkedHashMap[A, B](
      m: Map[A, B]
  ): scala.collection.mutable.LinkedHashMap[A, B] = {
    scala.collection.mutable.LinkedHashMap(m.toList: _*)
  }

  // ???: Rollout this.
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
