import _root_.sbtassembly.Plugin.{Assembly, MergeStrategy, PathList, _}
import sbtassembly.Plugin.AssemblyKeys._

enablePlugins(DockerPlugin)

docker <<= (docker dependsOn assembly)

assemblySettings

organization := "ogeagla"

name := "sclaav"

version := "0.0.1"

scalaVersion := "2.11.7"

resolvers += Resolver.sonatypeRepo("public")

libraryDependencies += "com.sksamuel.scrimage" % "scrimage-core_2.11" % "2.1.1"

libraryDependencies += "com.sksamuel.scrimage" % "scrimage-io-extra_2.11" % "2.1.1"

libraryDependencies += "com.sksamuel.scrimage" % "scrimage-filters_2.11" % "2.1.1"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.12"

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.12"

libraryDependencies += "com.github.scopt" %% "scopt" % "3.3.0"

libraryDependencies += "org.im4java" % "im4java" % "1.4.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

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