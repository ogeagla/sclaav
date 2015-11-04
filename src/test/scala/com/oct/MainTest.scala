package com.oct

import java.io.File

import com.oct.mosaic._
import com.sksamuel.scrimage.nio.JpegWriter
import com.sksamuel.scrimage.{Color, Image, Position, ScaleMethod}
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}
import org.slf4j.LoggerFactory

class MainTest extends FunSuite with BeforeAndAfter with Matchers {

  val log = LoggerFactory.getLogger(getClass)

  test("builds composite using random method and stuff") {
    implicit val writer = JpegWriter.Default
    val outPath = getClass.getResource("/").getPath
    val folder = new File(getClass.getResource("/bap-images").getPath)
    val files = folder.listFiles().filter(_.isFile).take(400)

    val theImage1 = Image.fromFile(files.filter(f => f.getAbsolutePath.contains("0010-2015-07-1112-24-27")).head)
    val theImage2 = Image.fromFile(files.filter(f => f.getAbsolutePath.contains("0068-2014-11-2816-57-05")).head)

    val otherImages = files.tail.map(f => Image.fromFile(f).scale(0.25, ScaleMethod.FastScale))
    val emptyImage = Image.filled(theImage1.width, theImage1.height, Color.Transparent)

    val composite1 = SimpleCompleteRandomAssembler(theImage1, emptyImage, otherImages)
    composite1.output(outPath + s"composite-1.jpeg")
    theImage1.output(outPath + s"ref-1.jpeg")

    val composite2 = SimpleCompleteRandomAssembler(theImage2, emptyImage, otherImages)
    composite2.output(outPath + s"composite-2.jpeg")
    theImage2.output(outPath + s"ref-2.jpeg")


  }

  ignore("builds composites for realz") {

    implicit val writer = JpegWriter.Default
    val outPath = new File(getClass.getResource("/").getFile)

    val folder = new File(getClass.getResource("/bap-images").getPath)
    val files = folder.listFiles().filter(_.isFile).take(400)

    for(file <- files) {
      val controlFile = file
      val sampleFiles = files.filter(_ != controlFile)

      DoMosaic(controlFile, sampleFiles, 128, 128, outPath)
    }
  }

  ignore("builds similar composites") {

    implicit val writer = JpegWriter.Default
    val outPath = getClass.getResource("/").getPath

    val folder = new File(getClass.getResource("/bap-images").getPath)
    val files = folder.listFiles().filter(_.isFile).take(10)

    val filesHead = files.head
    val filesTail = files.tail

    val controlImage = Image.fromFile(filesHead)
    val controlSize = (controlImage.width, controlImage.height)

    val cols = 2
    val rows = 2

    val colWidth = controlSize._1 / cols
    val rowHeight = controlSize._2 / rows

    val images = filesTail.map(f => Image.fromFile(f).scaleTo(colWidth, rowHeight, ScaleMethod.FastScale))

    val topLeft =     SimpleCrop((cols, rows), (0, 0), controlImage)
    val topRight =    SimpleCrop((cols, rows), (1, 0), controlImage)
    val bottomLeft =  SimpleCrop((cols, rows), (0, 1), controlImage)
    val bottomRight = SimpleCrop((cols, rows), (1, 1), controlImage)

    topLeft.output(outPath + s"topLeft.jpeg")
    topRight.output(outPath + s"topRight.jpeg")
    bottomLeft.output(outPath + s"bottomLeft.jpeg")
    bottomRight.output(outPath + s"bottomRight.jpeg")
    controlImage.output(outPath + "ref.jpeg")

    val topLMatch = MatchByArgbAverage(SimpleArgbEstimator, SimpleArgbDistance, topLeft, images)
    val topRMatch = MatchByArgbAverage(SimpleArgbEstimator, SimpleArgbDistance, topRight, images)
    val botLMatch = MatchByArgbAverage(SimpleArgbEstimator, SimpleArgbDistance, bottomLeft, images)
    val botRMatch = MatchByArgbAverage(SimpleArgbEstimator, SimpleArgbDistance, bottomRight, images)

    val img = controlImage
      .overlay(topLMatch, 0, 0)
      .overlay(topRMatch, topRight.width, 0)
      .overlay(botLMatch, 0, bottomLeft.height)
      .overlay(botRMatch, bottomRight.width, bottomRight.height)
      .output(outPath + "matched.jpeg")

  }

  ignore("similarity pairs") {

    implicit val writer = JpegWriter.Default
    val outPath = getClass.getResource("/").getPath


    val folder = new File(getClass.getResource("/bap-images").getPath)
    val files = folder.listFiles().filter(_.isFile).take(400)

    val images = files.map(f => Image.fromFile(f).scaleTo(256, 256, ScaleMethod.FastScale))

    val array = UniqueCartesian2(images, images)

    val distances = array.par.map { case (i1, i2) => (i1, i2, ImageSimilarityArgbDistance2(i1, i2)) }.toList

    val distancesSorted = distances.sortBy{ case (i1, i2, dist) => dist }

    val topFew = distancesSorted.take(15)

    val bottomFew = distancesSorted.takeRight(15)

    topFew.++(bottomFew).foreach { case (i1, i2, dist) =>
      val i1Scaled = i1.scaleTo(128, 128, ScaleMethod.FastScale)
      val i2Scaled = i2.scaleTo(128, 128, ScaleMethod.FastScale)

      val img = i1Scaled.resizeTo(256, 128, Position.CenterLeft).overlay(i2Scaled, 128, 0).output(outPath + s"${dist}.jpeg")
    }

  }
}
