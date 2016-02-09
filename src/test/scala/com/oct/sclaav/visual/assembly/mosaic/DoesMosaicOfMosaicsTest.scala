package com.oct.sclaav.visual.assembly.mosaic

import java.io.File

import com.oct.sclaav.TestHelpers
import com.oct.sclaav.visual.computation.RelativeImageSimilarityArgbDistance2
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.nio.JpegWriter
import org.scalatest.{Matchers, BeforeAndAfter, FunSuite}

class DoesMosaicOfMosaicsTest extends FunSuite with BeforeAndAfter with Matchers with TestHelpers {

  test("builds mosaic of mosaics for realz") {
    implicit val writer = JpegWriter.Default
    val outPath = new File(testRootPath)

    val files = bapImagesDir.listFiles().filter(_.isFile).take(400)

    val imageToCreate = files.filter(_.getName.contains("0207-")).head

    val theAssembledImage = DoMosaicOfMosaics(imageToCreate, files, 16, 16, outPath, Some("boulder-foothills-mosaic-of-mosaics.jpeg"))

    theAssembledImage.output(testRootPath + "mosaic-of-mosaics-out.jpeg")

    val mosaicImgFromResources = Image.fromFile(mosaicBoulderFoothillsLowRes)

    val relativeDistance = RelativeImageSimilarityArgbDistance2(mosaicImgFromResources, theAssembledImage)

    relativeDistance should be <= 0.075
  }

}
