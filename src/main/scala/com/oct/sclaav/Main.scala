package com.oct.sclaav

import java.io.File

import com.oct.sclaav.visual.Mode.Mode
import com.oct.sclaav.visual.assembly.mosaic.DoMosaic
import com.oct.sclaav.visual.{Config, MapsModes, Mode}
import org.slf4j.LoggerFactory

object Main {

  val log = LoggerFactory.getLogger(getClass)

  def parseArgs(args: Array[String]): Config = {

    implicit val tenantKeyRead: scopt.Read[Mode] = scopt.Read.reads(MapsModes(_))

    val parser = new scopt.OptionParser[Config]("Sclaav.jar") {
      head("Scala Mosaic", "0.0.x")
      opt[Mode]('m', "mode") action { (x, c) =>
        c.copy(mode = x) } text "mode is an enum property"
      opt[File]('t', "target") action { (x, c) =>
        c.copy(singleTarget = x) } text "target is a file property"
      opt[Boolean]('f', "filters") action { (x, c) =>
        c.copy(manipulate = x) } text "filters is a boolean property"
      opt[Int]('r', "rows") action { (x, c) =>
        c.copy(rows = x) } text "rows is an integer property"
      opt[Int]('c', "cols") action { (x, c) =>
        c.copy(cols = x) } text "cols is an integer property"
      opt[Int]('s', "samples") action { (x, c) =>
        c.copy(maxSamplePhotos = x) } text "samples is an integer property"
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
    parser.parse(args, Config()) match {
      case Some(config) =>
        config
      case None =>
        log.error(s"bad args")
        throw new IllegalArgumentException(s"bad args")
    }
  }


  def main(args: Array[String])  {

    val config = parseArgs(args)

    val inPath = config.in
    val outPath = config.out
    val maxSamples = config.maxSamplePhotos
    val rows = config.rows
    val cols = config.cols
    val doManipulate = config.manipulate
    val mode = config.mode

    log.info(s"inPath: $inPath outPath: $outPath")

    val files = inPath.listFiles().filter(_.isFile).take(maxSamples)

    mode match {

      case Mode.MOSAIC_SINGLE_FILE =>
        log.info("using single file target")
        val target = config.singleTarget
        DoMosaic(target, files, cols, rows, outPath, doManipulate = doManipulate)

      case Mode.MOSAIC_PERMUTE_ALL_FILES =>
        log.info("permuting all files in input dir")
        for(file <- files) {
          val controlFile = file
          val sampleFiles = files.filter(_ != controlFile)

          log.info(s"running with control image: ${controlFile.getName}")

          DoMosaic(controlFile, sampleFiles, cols, rows, outPath, doManipulate = doManipulate)
        }
    }
  }
}
