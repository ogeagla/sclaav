package com.oct.sclaav.visual.manipulators

import com.oct.sclaav.TestHelpers
import com.sksamuel.scrimage.{Color, Image}
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class ManipulatorsTest extends FunSuite with BeforeAndAfter with Matchers with TestHelpers {

  test("create a transparent image") {
    //CreatesTransparentImage
    val theImage = CreatesTransparentImage(200, 150)
    assert(theImage.width === 200, "img width should match")
    assert(theImage.height === 150, "img height should match")

  }

  test("add transparency to image") {
    //AddTransaprencyToImage
    val someImage = Image.filled(200, 100, Color.Black)
    val someImageWTrans = AddTransparencyToImage(someImage)

    assert((someImageWTrans.width, someImageWTrans.height) === (200, 100), "img dims should match")
    assert(someImageWTrans.argb.map(_.apply(0)).count(_ == 127) === 0, "all alphas are 0.5 / 1.0")
  }

  test("place image on transparent background at given coordiantes") {

    val someImage = Image.filled(200, 100, Color.Black)
    val somePixel = someImage.argb(10, 10)

    val someImageOnBackground = (new ToPositionOnTransparentBackground(500, 250, 100, 100))(someImage)
    val somePixelWBackground = someImageOnBackground.argb(101, 101)

    assert(somePixel === somePixelWBackground, "pixels should match")
  }

  test("create an alpha composite") {
    val imgToApply = Image.filled(200, 100, Color.White)
    val imgOnWhichWeApply = Image.filled(400, 500, Color.Black)

    val compositeImg = (new AlphaCompositeManipulator(imgToApply, 100, 50))(imgOnWhichWeApply)

    val somePixel = compositeImg.argb(150, 100)

    //FIXME is the alpha correct?
    assert(somePixel === Array(255, 128, 128, 128), "should be grey at 1.0 alpha")
  }

  test("crops") {
    val gridSize = (8, 8)
    val img = Image.filled(320, 240, Color.Black)

    val cropped = SimpleCrop(gridSize, (2, 2), img)

    assert((cropped.width, cropped.height) === (40, 30), "cropped size")
    assert(cropped.argb(10, 10) === Array(255, 0, 0, 0), "cropped color should not change")
  }

}
