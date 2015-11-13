package com.oct.sclaav.visual.assembly.mosaic

import java.io.File

import com.oct.sclaav.TestHelpers
import com.oct.sclaav.visual.computation.RelativeImageSimilarityArgbDistance2
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.nio.JpegWriter
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class DoesMosaicTest extends FunSuite with BeforeAndAfter with Matchers with TestHelpers {

  test("builds composites for realz") {

    implicit val writer = JpegWriter.Default
    val outPath = new File(testRootPath)

    val files = bapImagesDir.listFiles().filter(_.isFile).take(400)

    val imageToCreate = files.filter(_.getName.contains("0207-")).head

//    for(file <- files) {
//      val controlFile = file
//      val sampleFiles = files.filter(_ != controlFile)
//
//      DoMosaic(controlFile, sampleFiles, 32, 32, outPath)
//    }
    val theAssembledImage = DoMosaic(imageToCreate, files, 64, 64, outPath, Some("boulder-foothills-mosaic.jpeg"))

    val mosaicImgFromResources = Image.fromFile(mosaicBoulderFoothills)

    val distance = RelativeImageSimilarityArgbDistance2(mosaicImgFromResources, theAssembledImage)

    distance should be <= 0.05
  }
}
