package com.oct.sclaav.visual.assembly.mosaic

import java.io.File

import com.oct.sclaav.visual.manipulators._
import com.sksamuel.scrimage.{ScaleMethod, Image}
import com.sksamuel.scrimage.nio.JpegWriter
import org.slf4j.LoggerFactory

object DoMosaicOfMosaics {


  val log = LoggerFactory.getLogger(getClass)

  implicit val writer = JpegWriter.Default

  def apply(
             controlFile: File,
             sampleFiles: Array[File],
             cols: Int,
             rows: Int,
             outPath: Option[File],
             outputFilename: Option[String] = None,
             doManipulate: Boolean = false,
             writeReferenceImg: Boolean = false): Image = {


    val controlImage = Image.fromFile(controlFile)
    val controlSize = (controlImage.width, controlImage.height)

    val colWidth = controlSize._1 / cols
    val rowHeight = controlSize._2 / rows

    log.info(s"loading ${sampleFiles.length} images")

    val images = sampleFiles.map(f => Image.fromFile(f).scaleTo(colWidth, rowHeight, ScaleMethod.FastScale))

    val allFiles = sampleFiles.+:(controlFile)
    var counter = 0
    val mosaics = allFiles.map{ f =>
      counter = counter + 1
      log.info(s"Doing mosaic for ${counter} of ${allFiles.length}")
      DoMosaic(f, allFiles, 4, 4)
    }

    val allImages = doManipulate match {
      case false => images.++(mosaics)
      case true =>
        log.info(s"doing manipulations")
        val manips = Array(SummerManipulator, DiffuseManipulator, GlowManipulator, ChromeManipulator, OilManipulator, EdgeManipulator, LensBlurManipulator) //LensBlurManipulator
      val manipped = ManipulateAllWithAllOnce(images, manips)
        images.++(manipped).++(mosaics)
    }

    val assembledImage = DoMosaic.compose(rows, cols, allImages, controlImage, controlSize)

    log.info("persisting")

    val controlFilePhotoName = controlFile.getPath.split("/").last

    outPath match {
      case Some(outPathFile) =>
        if (writeReferenceImg) {
          val refPath = new File(outPathFile, s"${controlFilePhotoName}_ref.jpeg")
          controlImage.output(refPath)
        }
        val assembledPath = outputFilename match {
          case None => new File(outPathFile, s"${controlFilePhotoName}_assembled.jpeg")
          case Some(name) => new File(outPathFile, s"${name}")
        }
        assembledImage.output(assembledPath)
      case None =>

    }

    assembledImage
  }
}
