package fmv1992.scala_pandoc.cli
import fmv1992.scala_pandoc.PandocCode

trait CodeEvaluator {

  def evaluate(codeBlock: PandocCode): PandocCode

}
