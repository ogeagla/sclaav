package com.oct.sclaav.visual.manipulators

import com.sksamuel.scrimage.{Color, Image}
import org.scalatest.{Matchers, BeforeAndAfter, FunSuite}

class ManipulatorsTest extends FunSuite with BeforeAndAfter with Matchers {

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
    val imgToApply = Image.filled(200, 100, Color.Black)
    val imgOnWhichWeApply = Image.filled(400, 500, Color.White)

    val compositeImg = (new AlphaCompositeManipulator(imgToApply, 100, 50))(imgOnWhichWeApply)
    
    //TODO
    assert(true, "something")
  }


}
