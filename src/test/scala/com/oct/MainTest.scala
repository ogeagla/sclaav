package com.oct

import java.io.File

import com.oct.ThingDoer.ImageSimilarity
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.nio.JpegWriter
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class MainTest extends FunSuite with BeforeAndAfter with Matchers {


  test("stuff works") {

    implicit val writer = JpegWriter.Default

    val folder = new File(getClass.getResource("/").getPath)
    val files = folder.listFiles().filter(_.isFile)

    val images = files.map(Image.fromFile)

    var array = List[(Image, Image)]()

    for (i1 <- images) {
      for (i2 <- images) {
        val tup = (i1, i2)
        val put = (i2, i1)

        if (array.contains(tup) || array.contains(put) || i1 == i2) {

        } else {
          array = tup :: array
        }

      }
    }

//    val imagesCartesian = for { i1 <- images; i2 <- images } yield (i1, i2)
//
//    val imagesCartesianUnique = imagesCartesian.filter { case (i1, i2) => ! i1.equals(i2) }

    val distances = array.map { case (i1, i2) => (i1, i2, ImageSimilarity(i1, i2)) }

    val distancesSorted = distances.sortBy{ case (i1, i2, dist) => dist }

    val topFew = distancesSorted.take(6)

    topFew.foreach { case (i1, i2, dist) => }

    println("")

//
//    val outPath = getClass.getResource("/").getPath + "1_flip_XY.jpeg"
//
//    i1.flipX.flipY.output(outPath)
//
//    ThingDoer.doIt()

  }
}
