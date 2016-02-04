import _root_.sbtassembly.Plugin.{Assembly, MergeStrategy, PathList, _}
import sbtassembly.Plugin.AssemblyKeys._

enablePlugins(DockerPlugin)

docker <<= (docker dependsOn assembly)

assemblySettings

organization := "ogeagla"

name := "sclaav"

version := "0.0.1"

scalaVersion := "2.11.7"

val nd4jVersion = "0.4-rc3.8"
val dl4jVersion =	"0.4-rc3.8"
val canovaVersion = "0.0.0.14"
val jacksonVersion = "2.5.1"

resolvers += Resolver.sonatypeRepo("public")

libraryDependencies += "com.sksamuel.scrimage" % "scrimage-core_2.11" % "2.1.1"

libraryDependencies += "com.sksamuel.scrimage" % "scrimage-io-extra_2.11" % "2.1.1"

libraryDependencies += "com.sksamuel.scrimage" % "scrimage-filters_2.11" % "2.1.1"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.12"

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.12"

libraryDependencies += "com.github.scopt" %% "scopt" % "3.3.0"

libraryDependencies += "org.im4java" % "im4java" % "1.4.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

//libraryDependencies  ++= Seq(
//  // other dependencies here
//  "org.scalanlp" %% "breeze" % "0.11.2",
//  // native libraries are not included by default. add this if you want them (as of 0.7)
//  // native libraries greatly improve performance, but increase jar sizes.
//  // It also packages various blas implementations, which have licenses that may or may not
//  // be compatible with the Apache License. No GPL code, as best I know.
//  "org.scalanlp" %% "breeze-natives" % "0.11.2",
//  // the visualization library is distributed separately as well.
//  // It depends on LGPL code.
//  "org.scalanlp" %% "breeze-viz" % "0.11.2"
//)

libraryDependencies ++= Seq(
//  "commons-io" % "commons-io" % "2.4",
//  "com.google.guava" % "guava" % "18.0",
  "org.deeplearning4j" % "deeplearning4j-core" % dl4jVersion,
  "org.deeplearning4j" % "deeplearning4j-nlp" % dl4jVersion,
  "org.deeplearning4j" % "deeplearning4j-ui" % dl4jVersion,
  "org.nd4j" % "canova-nd4j-image" % canovaVersion,
  "org.nd4j" % "canova-nd4j-codec" % canovaVersion,
  "org.nd4j" % "nd4j-x86" % nd4jVersion
)

resolvers ++= Seq(
  // other resolvers here
  // if you want to use snapshot builds (currently 0.12-SNAPSHOT), use this.
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
)

testOptions in Test += Tests.Argument("-oD")

mainClass in assembly := Some("com.oct.sclaav.Main")

val assJarName = "Sclaav.jar"

val assJarRelPath = s"bin/$assJarName"

jarName in assembly := assJarName

outputPath in assembly := file(assJarRelPath)

test in assembly := {}

//Assembly:
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

//Docker:
dockerfile in docker := {
  val artifact = (outputPath in assembly).value
  val artifactTargetPath = s"/opt/sclaav/$assJarName"
  new Dockerfile {
    from("fedora:23")
    run("dnf", "-y", "update")
    run("dnf", "-y", "install", "java-1.8.0-openjdk", "gcc", "tar", "gettext", "jasper-devel", "lcms2-devel")
    copy(new File("dcraw-embedded/dcraw-9.26.0.tar.gz"), "/opt/dcraw/dcraw-9.26.0.tar.gz")
    copy(new File("src/test/resources/raw/cr2/E1DXLL0000503.CR2"), "/opt/dcraw/samples/E1DXLL0000503.CR2")
    run("tar", "-xvf", "/opt/dcraw/dcraw-9.26.0.tar.gz", "-C", "/opt/dcraw")
    workDir("/opt/dcraw/dcraw")
    run("sh", "/opt/dcraw/dcraw/install")
    copy(new File("dcraw-scripts/test.sh"), "/opt/dcraw/scripts/test.sh")
    run("sh", "/opt/dcraw/scripts/test.sh")
    add(artifact, artifactTargetPath)
    workDir("/opt/sclaav")
//    entryPoint("java", "-jar", artifactTargetPath)
    entryPoint("sleep", "12000")
  }
}

imageNames in docker := Seq(
  ImageName(s"${organization.value}/${name.value}:latest"), // Sets the latest tag
  ImageName(
    namespace = Some(organization.value),
    repository = name.value,
    tag = Some("v" + version.value)
  ) // Sets a name with a tag that contains the project version
)