package com.oct

import java.io.File

import com.oct.ThingDoer.ImageSimilarityArgbDistance2
import com.sksamuel.scrimage.nio.JpegWriter
import com.sksamuel.scrimage.{Image, Position, ScaleMethod}
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class MainTest extends FunSuite with BeforeAndAfter with Matchers {

  test("stuff works") {

    implicit val writer = JpegWriter.Default

    val folder = new File(getClass.getResource("/bap-images").getPath)
    val files = folder.listFiles().filter(_.isFile).take(200)

    val images = files.map(f => Image.fromFile(f).scaleTo(256, 256, ScaleMethod.FastScale))

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
    
    val distances = array.par.map { case (i1, i2) => (i1, i2, ImageSimilarityArgbDistance2(i1, i2)) }.toList

    val distancesSorted = distances.sortBy{ case (i1, i2, dist) => dist }

    val topFew = distancesSorted.take(5)

    val bottomFew = distancesSorted.takeRight(5)

    topFew.++(bottomFew).foreach { case (i1, i2, dist) =>
      val outPath = getClass.getResource("/").getPath

      val i1Scaled = i1.scaleTo(128, 128, ScaleMethod.FastScale)
      val i2Scaled = i2.scaleTo(128, 128, ScaleMethod.FastScale)

      val img = i1Scaled.resizeTo(256, 128, Position.CenterLeft).overlay(i2Scaled, 128, 0).output(outPath + s"${dist}.jpeg")
    }

  }
}
