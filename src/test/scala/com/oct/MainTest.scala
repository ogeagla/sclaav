package com.oct

import java.io.File

import com.sksamuel.scrimage.nio.JpegWriter
import com.sksamuel.scrimage.{Image, Position, ScaleMethod}
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class MainTest extends FunSuite with BeforeAndAfter with Matchers {

  test("builds similar composites") {

    implicit val writer = JpegWriter.Default

    val folder = new File(getClass.getResource("/bap-images").getPath)
    val files = folder.listFiles().filter(_.isFile).take(400)

    val filesHead = files.head
    val filesTail = files.tail

    val controlImage = Image.fromFile(filesHead)
    val controlSize = (controlImage.width, controlImage.height)

    val cols = 8
    val rows = 8

    val colWidth = controlSize._1 / cols
    val rowHeight = controlSize._2 / rows

    val images = filesTail.map(f => Image.fromFile(f).scaleTo(colWidth, rowHeight, ScaleMethod.FastScale))
  }

  ignore("similarity pairs") {

    implicit val writer = JpegWriter.Default

    val folder = new File(getClass.getResource("/bap-images").getPath)
    val files = folder.listFiles().filter(_.isFile).take(400)

    val images = files.map(f => Image.fromFile(f).scaleTo(256, 256, ScaleMethod.FastScale))

    val array = UniqueCartesian2(images.toList, images.toList)

    val distances = array.par.map { case (i1, i2) => (i1, i2, ImageSimilarityArgbDistance2(i1, i2)) }.toList

    val distancesSorted = distances.sortBy{ case (i1, i2, dist) => dist }

    val topFew = distancesSorted.take(15)

    val bottomFew = distancesSorted.takeRight(15)

    topFew.++(bottomFew).foreach { case (i1, i2, dist) =>
      val outPath = getClass.getResource("/").getPath

      val i1Scaled = i1.scaleTo(128, 128, ScaleMethod.FastScale)
      val i2Scaled = i2.scaleTo(128, 128, ScaleMethod.FastScale)

      val img = i1Scaled.resizeTo(256, 128, Position.CenterLeft).overlay(i2Scaled, 128, 0).output(outPath + s"${dist}.jpeg")
    }

  }
}
