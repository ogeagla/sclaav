package com.oct.sclaav.visual.computation

import com.oct.sclaav.TestHelpers
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
}
