package com.oct

import java.io.File

import com.oct.mosaic.{Config, DoMosaic}
import com.sksamuel.scrimage.nio.JpegWriter
import org.slf4j.LoggerFactory

object Main {

  val log = LoggerFactory.getLogger(getClass)


  def parseArgs(args: Array[String]): Config= {
    val parser = new scopt.OptionParser[Config]("Mosaical.jar") {
      head("Scala Mosaic", "0.0.x")
      opt[Int]('m', "maxSamplePhotos") action { (x, c) =>
        c.copy(maxSamplePhotos = x) } text "maxSamplePhotos is an integer property"
      opt[File]('i', "in") required() valueName "<file>" action { (x, c) =>
        c.copy(in = x) } text "in is a required file property"
      opt[File]('o', "out") required() valueName "<file>" action { (x, c) =>
        c.copy(out = x) } text "out is a required file property"
      opt[Unit]("verbose") action { (_, c) =>
        c.copy(verbose = true) } text "verbose is a flag"
      opt[Unit]("debug") hidden() action { (_, c) =>
        c.copy(debug = true) } text "this option is hidden in the usage text"
      note("some notes.\n")
      help("help") text "prints this usage text"
    }
    // parser.parse returns Option[C]
    parser.parse(args, Config()) match {
      case Some(config) =>
        // do stuff
        config
      case None =>
        // arguments are bad, error message will have been displayed
        log.error(s"bad args $args")
        throw new IllegalArgumentException(s"bad args $args")
    }
  }


  def main(args: Array[String])  {

    val config = parseArgs(args)

    val inPath = config.in
    val outPath = config.out
    val maxSamples = config.maxSamplePhotos

    log.info(s"inPath: $inPath outPath: $outPath")

    implicit val writer = JpegWriter.Default

    val files = inPath.listFiles().filter(_.isFile).take(maxSamples)

    for(file <- files) {
      val controlFile = file
      val sampleFiles = files.filter(_ != controlFile)

      log.info(s"running with control image: ${controlFile.getName}")

      DoMosaic(controlFile, sampleFiles, 2, 2, outPath)
    }
  }
}
