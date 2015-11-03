package com.oct

import java.io.File

import com.sksamuel.scrimage.nio.JpegWriter
import org.slf4j.LoggerFactory

object Main {

  val log = LoggerFactory.getLogger(getClass)

  def main(args: Array[String])  {

    val inPath = args(0)
    val outPath = args(1)

    log.info(s"inPath: $inPath outPath: $outPath")

    implicit val writer = JpegWriter.Default

    val folder = new File(inPath)
    val files = folder.listFiles().filter(_.isFile).take(400)

    for(file <- files) {
      val controlFile = file
      val sampleFiles = files.filter(_ != controlFile)

      DoMosaic(controlFile, sampleFiles, 128, 128, outPath)
    }
  }
}
