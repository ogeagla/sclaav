package com.oct.sclaav.visual.assembly.grid

import com.oct.sclaav.TestHelpers
import com.sksamuel.scrimage.nio.JpegWriter
import com.sksamuel.scrimage.{Color, Image, ScaleMethod}
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class QuadrilateralAssemblerTest extends FunSuite with BeforeAndAfter with Matchers with TestHelpers {

  test("builds composite using random method and stuff") {
    implicit val writer = JpegWriter.Default
    val files = bapImagesDir.listFiles().filter(_.isFile).take(400)

    val theImage1 = Image.fromFile(files.filter(f => f.getAbsolutePath.contains("0010-2015-07-1112-24-27")).head)
    val theImage2 = Image.fromFile(files.filter(f => f.getAbsolutePath.contains("0068-2014-11-2816-57-05")).head)

    val otherImages = files.tail.map(f => Image.fromFile(f).scale(0.25, ScaleMethod.FastScale))
    val emptyImage = Image.filled(theImage1.width, theImage1.height, Color.Transparent)


    val composite1 = QuadrilateralAssembler(theImage1, emptyImage, otherImages)
    composite1.output(testRootPath + s"quad-composite-1.jpeg")
    theImage1.output(testRootPath + s"quad-ref-1.jpeg")

  }
}
