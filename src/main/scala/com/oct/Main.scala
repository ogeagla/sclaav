package com.oct

import java.io.File

import com.sksamuel.scrimage.nio.JpegWriter

object Main {

  def main(args: Array[String])  {

    println(s"inpath: ${args(0)} outpath: ${args(1)}")

    implicit val writer = JpegWriter.Default
    val outPath = args(1)

    val folder = new File(args(0))
    val files = folder.listFiles().filter(_.isFile).take(400)

    for(file <- files) {
      val controlFile = file
      val sampleFiles = files.filter(_ != controlFile)

      DoMosaic(controlFile, sampleFiles, 128, 128, outPath)
    }
  }
}
