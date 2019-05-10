package fmv1992.scala_pandoc

import fmv1992.fmv1992_scala_utilities.util.Reader

// import ujson._

object PandocUtilities {

  lazy val gitRoot = {
    var gr = new java.io.File(System.getProperty("user.dir"))
    while (!gr.list.contains(".git")) {
      gr = new java.io.File(gr.getParent)
    }
    gr
  }

  private lazy val joinerString = Reader
    .readLines(
      new java.io.File(gitRoot, "other/json/joiner_element.json")
    )
    .mkString
  lazy val joiner = ujson.read(joinerString)

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
