package com.oct

import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.nio.JpegWriter
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class MainTest extends FunSuite with BeforeAndAfter with Matchers {


  test("stuff works") {

    val image = Image.fromResource("/0001-2015-09-2614-50-18-IMG_20150926_145015_marked.jpg")
    implicit val writer = JpegWriter.Default

    val outPath = getClass.getResource("/").getPath + "1_flip_XY.jpeg"

    image.flipX.flipY.output(outPath)

    ThingDoer.doIt()

  }
}
