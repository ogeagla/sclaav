package com.oct.sclaav.visual.computation

import com.oct.sclaav.TestHelpers
import com.oct.sclaav.visual.manipulators.CreatesTransparentImage
import com.oct.sclaav.visual.search.MatchByArgbAverage
import com.sksamuel.scrimage.{Color, Image}
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class ComputersTest extends FunSuite with BeforeAndAfter with Matchers with TestHelpers {

  test("simple pixel location computer") {
    val gridSize = (8,8)
    val gridLoc = (2,4)
    val canvasSize = (320, 480)
    val expectedLocation = (80, 240)

    val actualLoc = SimplePixelLocationComputer(gridSize, gridLoc, canvasSize)

    assert(actualLoc === expectedLocation, "pixel location should be computed from grid location")
  }

  test("matches image to list of images by argb average") {
    val theImage = Image.filled(50, 50, Color.Black)
    val theOtherImages = (0 to 10).map(_ => CreatesTransparentImage(50, 50)).toArray

    val matched = MatchByArgbAverage(theImage, theOtherImages.++:(Array(theImage)))

    assert(matched === theImage, "the image should best match itself in a sea of transparency")
  }
}
