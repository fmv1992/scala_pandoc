// https://www.scala-sbt.org/1.0/docs/Howto-Project-Metadata.html

import java.io.File

coverageMinimum := 85
coverageFailOnMinimum := true
coverageExcludedPackages := "<empty>;.*ReplaceVariables.*"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

// From: https://stackoverflow.com/a/21738753/5544140
// show runtime:fullClasspath
// """
// If I type this in the sbt shell:
//
// inspect run
// I see, among other output:
//
// [info] Dependencies:
// [info]  runtime:fullClasspath
// So then if I type:
//
// show runtime:fullClasspath
// I get output like:
//
//  List(
//    Attributed(/Users/tisue/Dropbox/repos/euler/target/scala-2.10/classes),
//    Attributed(/Users/tisue/.sbt/boot/scala-2.10.3/lib/scala-library.jar))
// """

lazy val scalatest = "org.scalatest" %% "scalatest" % "3.0.5"
lazy val ujson = "com.lihaoyi" %% "ujson" % "0.7.1"
lazy val fmv1992UtilitiesCli = "io.github.fmv1992" %% "cli" % "1.9.3"
lazy val fmv1992UtilitiesUtil = "io.github.fmv1992" %% "util" % "1.9.3"
lazy val fmv1992ScalaCli = "io.github.fmv1992" %% "scala_cli_parser" % "0.2.0"

name := "scala_pandoc"

test in assembly := {}

lazy val commonSettings = Seq(
  organization := "fmv1992",
  licenses += "GPLv2" -> url("https://www.gnu.org/licenses/gpl-2.0.html"),
  version := IO
    .readLines(new File("./src/main/resources/version"))
    .mkString(""),
  scalaVersion := "2.12.8",
  pollInterval := scala.concurrent.duration.FiniteDuration(500L, "ms"),
  maxErrors := 10,
  resourceDirectory in Compile := file(".") / "./src/main/resources",
  resourceDirectory in Runtime := file(".") / "./src/main/resources",
  test in assembly := {},
  assemblyMergeStrategy in assembly := {
    case "version" => MergeStrategy.first
    case x => {
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
    }
  },
  // This final part makes test artifacts being only importable by the test files
  // libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  //                                                                   ↑↑↑↑↑
  // Removed on commit 'cd9d482' to enable 'trait ScalaInitiativesTest' define
  // 'namedTest'.
  libraryDependencies ++= Seq(
    scalatest,
    ujson,
    fmv1992UtilitiesCli,
    fmv1992ScalaCli,
    fmv1992UtilitiesUtil
  ),
  scalacOptions ++= (Seq("-feature", "-deprecation", "-Xfatal-warnings")
    ++ sys.env.get("SCALAC_OPTS").getOrElse("").split(" ").toSeq)
)

lazy val fmv1992 = (project in file("."))
  .settings(commonSettings)
  .settings(assemblyJarName in assembly := "scala_pandoc.jar")

// vim: set filetype=sbt fileformat=unix nowrap spell:
