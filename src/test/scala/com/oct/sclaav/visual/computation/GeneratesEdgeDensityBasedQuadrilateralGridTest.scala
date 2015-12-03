package com.oct.sclaav.visual.computation

import com.oct.sclaav.TestHelpers
import com.sksamuel.scrimage.Image
import org.scalatest.{Matchers, BeforeAndAfter, FunSuite}

class GeneratesEdgeDensityBasedQuadrilateralGridTest extends FunSuite with BeforeAndAfter with Matchers with TestHelpers {

  test("stuff happens") {

    val flagPaintingImg = Image.fromFile(blackAndYellowFlag)

    val edgesImgToBeUsed = GeneratesEdgeDensityBasedQuadrilateralGrid.getImageToCropFromOriginal(flagPaintingImg)
    edgesImgToBeUsed.output(testRootPath + "edges-img.jpeg")

    GeneratesEdgeDensityBasedQuadrilateralGrid(flagPaintingImg)

  }

}
