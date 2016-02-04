package com.oct.sclaav.visual.computation

import com.oct.sclaav.TestHelpers
import com.oct.sclaav.visual.assembly.grid.QuadrilateralAssembler
import com.sksamuel.scrimage.nio.JpegWriter
import com.sksamuel.scrimage.{Color, Image}
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class GeneratesEdgeDensityBasedQuadrilateralGridTest extends FunSuite with BeforeAndAfter with Matchers with TestHelpers {

  test("img to be decomposed makes sense") {

    val flagPaintingImg = Image.fromFile(blackAndYellowFlag)

    val edgesImgToBeUsed = GeneratesEdgeDensityBasedQuadrilateralGrid.getImageToCropFromOriginal(flagPaintingImg)
    edgesImgToBeUsed.output(testRootPath + "edges-img.jpeg")

    GeneratesEdgeDensityBasedQuadrilateralGrid(flagPaintingImg)

  }

  test("values to levels works") {

    val values = Array(
      Array(0.0, 2.0, 3.0),
      Array(1.5, 2.5, 2.9)
    )

    val steps = ValuesToLevels(values, 3, UniformStepMaker)

    val expectedSteps = Array(
      Array(0, 1, 2),
      Array(1, 2, 2)
    )

    assert(steps === expectedSteps, "steps should match")

  }

  test("generates something not shitty") {

    val flagPaintingImg = Image.fromFile(blackAndYellowFlag)

    val quads = GeneratesEdgeDensityBasedQuadrilateralGrid(flagPaintingImg)

    quads.listOfTheStuff.foreach { q =>
      println(s"(${q.startCol}, ${q.startRow}), (${q.endCol}, ${q.endRow})")
    }
  }

  test("does the whole assembly") {
    val flagPaintingImg = Image.fromFile(blackAndYellowFlag)

    implicit val writer = JpegWriter.Default

    val edgesImgToBeUsed = GeneratesEdgeDensityBasedQuadrilateralGrid.getImageToCropFromOriginal(flagPaintingImg)
    edgesImgToBeUsed.output(testRootPath + "edges-img.jpeg")

    val files = bapImagesDir.listFiles().filter(_.isFile).take(400)

//    val theImage1 = Image.fromFile(files.filter(f => f.getAbsolutePath.contains("0010-2015-07-1112-24-27")).head)
//    val theImage2 = Image.fromFile(files.filter(f => f.getAbsolutePath.contains("0068-2014-11-2816-57-05")).head)

    val otherImages = files.tail.map(f => Image.fromFile(f))
    val emptyImage = Image.filled(flagPaintingImg.width, flagPaintingImg.height, Color.Transparent)


    val composite1 = (new QuadrilateralAssembler(20, 20))(flagPaintingImg, emptyImage, otherImages, new GeneratesEdgeDensityBasedQuadrilateralGrid)
    composite1.output(testRootPath + s"quad-edge-composite-1.jpeg")
    flagPaintingImg.output(testRootPath + s"quad-edge-ref-1.jpeg")

  }

}
