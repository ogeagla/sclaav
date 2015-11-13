package com.oct.sclaav.visual.assembly.genetic

import com.oct.sclaav.TestHelpers
import com.oct.sclaav.visual.manipulators.CreatesTransparentImage
import com.sksamuel.scrimage.{ScaleMethod, Image}
import com.sksamuel.scrimage.nio.JpegWriter
import org.scalatest.{Matchers, BeforeAndAfter, FunSuite}

class SimpleCompleteGeneticAssemblerTest extends FunSuite with BeforeAndAfter with Matchers with TestHelpers {

  test("builds composite using genetic algorithm") {
    implicit val writer = JpegWriter.Default
    val files = bapImagesDir.listFiles().filter(_.isFile).take(400)

    //    val imgDevilsThumb = Image.fromFile(
    //      files.filter(f => f.getAbsolutePath.contains("0010-2015-07-1112-24-27")).head).scale(0.25, ScaleMethod.FastScale)
    //    val imgWetMtns = Image.fromFile(
    //      files.filter(f => f.getAbsolutePath.contains("0068-2014-11-2816-57-05")).head)
    val imgFlagstaffSunrise = Image.fromFile(
      files.filter(f => f.getAbsolutePath.contains("0006-2015-07-1405-29-39")).head)
      .scale(0.125, ScaleMethod.FastScale)

    val otherImages = files.tail.map(f => Image.fromFile(f).scale(0.01, ScaleMethod.FastScale))
    val emptyImage = CreatesTransparentImage(imgFlagstaffSunrise.width, imgFlagstaffSunrise.height)

    //    val transMaker1 = new AlphaCompositeManipulator(imgDevilsThumb.scale(0.25, ScaleMethod.FastScale), 50, 50)
    //    val transMaker2 = new AlphaCompositeManipulator(imgWetMtns.scale(0.25, ScaleMethod.FastScale), 200, 200)
    //    val transMaker3 = new AlphaCompositeManipulator(imgWetMtns.scale(0.25, ScaleMethod.FastScale), 300, 300)
    //
    //    val manips: Array[ImageManipulator] = Array(transMaker1, transMaker2, transMaker3)
    //
    //    val (chain, img, dist) = (new SimpleCompleteGeneticAssembler).getApplied(Array(manips), emptyImage, imgWetMtns).head
    //    println(dist)
    //    img.output(outPath + s"applied-1.jpeg")


    val composite1 = SimpleCompleteGeneticAssembler(imgFlagstaffSunrise, emptyImage, otherImages)
    composite1.output(testRootPath + s"composite-1.jpeg")
    imgFlagstaffSunrise.output(testRootPath + s"ref-1.jpeg")

  }
}
