import _root_.sbtassembly.Plugin.{Assembly, MergeStrategy, PathList, _}
import sbtassembly.Plugin.AssemblyKeys._

assemblySettings

name := "scala-images"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies += "com.sksamuel.scrimage" % "scrimage-core_2.11" % "2.1.1"

libraryDependencies += "com.sksamuel.scrimage" % "scrimage-io-extra_2.11" % "2.1.1"

libraryDependencies += "com.sksamuel.scrimage" % "scrimage-filters_2.11" % "2.1.1"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.12"

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.12"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

testOptions in Test += Tests.Argument("-oD")

mainClass in assembly := Some("com.oct.Main")

jarName in assembly := "Mosaical.jar"

outputPath in assembly := file("bin/Mosaical.jar")

test in assembly := {}

mergeStrategy in assembly := {
  case x if Assembly.isConfigFile(x) =>
    MergeStrategy.concat
  case PathList(ps@_*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
    MergeStrategy.rename
  case PathList("META-INF", xs@_*) =>
    MergeStrategy.discard
  case PathList("log4j.properties", xs@_*) =>
    MergeStrategy.concat
  case _ => MergeStrategy.first
}
