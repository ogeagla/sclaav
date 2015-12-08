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

  test("values to levels works") {

    val values = Array(
      Array(0.0, 2.0, 3.0),
      Array(1.5, 2.5, 2.9)
    )

    val steps = ValuesToLevels(values)

    val expectedSteps = Array(
      Array(0, 1, 2),
      Array(1, 2, 2)
    )

    assert(steps === expectedSteps, "steps should match")

  }

}
