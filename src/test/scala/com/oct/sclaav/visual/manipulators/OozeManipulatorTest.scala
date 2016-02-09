package com.oct.sclaav.visual.manipulators

import java.io.File

import com.oct.sclaav.TestHelpers
import com.oct.sclaav.visual.assembly.mosaic.DoMosaic
import com.sksamuel.scrimage.filter.{LensBlurFilter, BlurFilter, DiffuseFilter, OilFilter}
import com.sksamuel.scrimage.nio.JpegWriter
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class OozeManipulatorTest extends FunSuite with BeforeAndAfter with Matchers with TestHelpers {

  test("oozes") {
    implicit val writer = JpegWriter.Default
    val outPath = new File(testRootPath)

    val files = bapImagesDir.listFiles().filter(_.isFile).take(400)

    val sampleFiles = new File("/home/octavian/github/Bootstrap-Image-Gallery/img/raw/all").listFiles()

    val imageToCreate = files.filter(_.getName.contains("0207-")).head

//    val image = Image.fromFile(files.filter(_.getName.contains("0207-")).head)

    val theAssembledImage = DoMosaic(imageToCreate, sampleFiles, 14, 18, Some(outPath), Some("mosaic-w-manips.jpeg"), doManipulate = true)

//    val oozedImg = OozeManipulator(theAssembledImage)

    val oily = theAssembledImage.filter(OilFilter(6, 8))
    oily.output(testRootPath + "oily-out.jpeg")

    val diffuse = theAssembledImage.filter(DiffuseFilter(15.0f))
    diffuse.output(testRootPath + "diffuse-out.jpeg")

    val blur = theAssembledImage.filter(BlurFilter)
    blur.output(testRootPath + "blur-out.jpeg")

    val lensBlur = theAssembledImage.filter(LensBlurFilter())
    lensBlur.output(testRootPath + "lens-blur-out.jpeg")

    val oilyDiff = diffuse.filter(OilFilter(6, 8))
    oilyDiff.output(testRootPath + "oily-diff-out.jpeg")

    val oilyBlur = blur.filter(OilFilter(6, 8))
    oilyBlur.output(testRootPath + "oily-blur-out.jpeg")

    val oilyLensBlur = lensBlur.filter(OilFilter(6, 8))
    oilyLensBlur.output(testRootPath + "oily-lens-blur-out.jpeg")




  }

}
