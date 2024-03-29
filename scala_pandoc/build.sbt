// https://www.scala-sbt.org/1.0/docs/Howto-Project-Metadata.html

import java.io.File

coverageMinimum := 85
coverageFailOnMinimum := true
coverageExcludedPackages := "<empty>;.*ReplaceVariables.*"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
resolvers += Resolver.mavenLocal

lazy val scala213 = "2.13.8"

inThisBuild(
  List(
    // sbtPlugin := true,
    scalaVersion := scala213,
    scalaBinaryVersion := scala213
    // scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.4.3",
    // https://index.scala-lang.org/ohze/scala-rewrites/scala-rewrites/0.1.10-sd?target=_2.13
    // semanticdbEnabled := true,
    // semanticdbOptions += "-P:semanticdb:synthetics:on", // make sure to add this
    // semanticdbVersion := scalafixSemanticdb.revision,
    // libraryDependencies += "org.scalameta" % s"semanticdb-scalac-core_${scala213}" % scalafixSemanticdb.revision,
    // scalafixScalaBinaryVersion := scala213,
    // fork in Test := false,
    // fork in test := false,
    // fork in run := false
    // git.remoteRepo := "https://github.com/fmv1992/scala_cli_parser"
  )
)

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

lazy val scalatest = "org.scalatest" %% "scalatest" % "3.2.11"
lazy val ujson = "com.lihaoyi" %% "ujson" % "0.7.5"
lazy val fmv1992ScalaCli =
  "io.github.fmv1992" %% "scala_cli_parser" % "0.4.5"

name := "scala_pandoc"

(assembly / test) := {}

lazy val commonSettings = Seq(
  organization := "fmv1992",
  licenses += "GPLv2" -> url("https://www.gnu.org/licenses/gpl-2.0.html"),
  version := IO
    .readLines(new File("./src/main/resources/version"))
    .mkString(""),
  scalaVersion := scala213,
  pollInterval := scala.concurrent.duration.FiniteDuration(500L, "ms"),
  maxErrors := 10,
  (Compile / resourceDirectory) := file(".") / "./src/main/resources",
  (assembly / test) := {},
  (assembly / assemblyMergeStrategy) := {
    case "version" => MergeStrategy.first
    case x => {
      val oldStrategy = (assembly / assemblyMergeStrategy).value
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
    fmv1992ScalaCli
  ),
  scalacOptions ++= (Seq(
    "-feature",
    "-deprecation",
    "-Xfatal-warnings"
  )
    ++ sys.env.get("SCALAC_OPTS").getOrElse("").split(" ").toSeq)
)

lazy val fmv1992 = (project in file("."))
  .settings(commonSettings)
  .settings((assembly / assemblyJarName) := "scala_pandoc.jar")

// vim: set filetype=sbt fileformat=unix nowrap spell:
