package com.oct.sclaav

import java.io.File

import com.sksamuel.scrimage.nio.JpegWriter

trait TestHelpers {
  implicit val writer = JpegWriter.Default
  val bapImagesDir = new File(getClass.getResource("/below-average-photography").getPath)
  val testRootPath = getClass.getResource("/").getPath
  val mosaicBoulderFoothills = new File(getClass.getResource("/assembled/mosaic/boulder-foothills-mosaic.jpeg").getPath)
  val mosaicBoulderFoothillsLowRes = new File(getClass.getResource("/assembled/mosaic/boulder-foothills-mosaic-low-res.jpeg").getPath)
  val blackAndYellowFlag = new File(getClass.getResource("/other/black-yellow-flag.jpg").getPath)

}
