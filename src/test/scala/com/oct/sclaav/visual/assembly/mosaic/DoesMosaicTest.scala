package com.oct.sclaav.visual.assembly.mosaic

import java.io.File
import com.oct.sclaav.TestHelpers
import com.sksamuel.scrimage.nio.JpegWriter
import org.scalatest.{Matchers, BeforeAndAfter, FunSuite}

class DoesMosaicTest extends FunSuite with BeforeAndAfter with Matchers with TestHelpers {

  ignore("builds composites for realz") {

    implicit val writer = JpegWriter.Default
    val outPath = new File(testRootPath)

    val files = bapImagesDir.listFiles().filter(_.isFile).take(400)

    for(file <- files) {
      val controlFile = file
      val sampleFiles = files.filter(_ != controlFile)

      DoMosaic(controlFile, sampleFiles, 128, 128, outPath)
    }
  }
}
