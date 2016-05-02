package com.oct.sclaav.visual.assembly.mosaic

import java.io.File

import com.oct.sclaav.visual.assembly.grid.SimpleCompleteGridAssembler
import com.oct.sclaav.visual.manipulators._
import com.oct.sclaav.visual.search.MatchByArgbAverage
import com.sksamuel.scrimage.nio.JpegWriter
import com.sksamuel.scrimage.{Color, Image, ScaleMethod}
import org.slf4j.LoggerFactory

import scala.collection.parallel.mutable.ParArray

object DoMosaic {

  val log = LoggerFactory.getLogger(getClass)
  implicit val writer = JpegWriter.Default

  def compose(rows: Int, cols: Int, allImages: Array[Image], controlImage: Image, controlSize: (Int, Int)): Image = {


    val listBuffer = new ParArray[(Image, (Int, Int))](rows * cols)

    log.info(s"cropping and matching using sample size ${allImages.length}")

    for (c <- (0 to cols - 1).par) {
      log.info(s"${c + 1} of $cols cols complete")
      for(r <- 0 to rows - 1) {
        val cropped = SimpleCrop((cols, rows), (c, r), controlImage)

        val matchToCropped = MatchByArgbAverage(cropped, allImages)

        listBuffer.update(rows*c + r, (matchToCropped, (c, r)))
      }
    }

    val transparentCanvas = Image.filled(controlSize._1, controlSize._2, Color.Transparent)

    log.info("assembling")
    val assembledImage = SimpleCompleteGridAssembler(transparentCanvas,listBuffer.seq.toArray , (cols, rows))

    assembledImage
  }

  def apply(
             controlFile: File,
             sampleFiles: Array[File],
             cols: Int,
             rows: Int,
             outPath: Option[File] = None,
             outputFilename: Option[String] = None,
             doManipulate: Boolean = false,
             writeReferenceImg: Boolean = false): Image = {

    val controlImage = Image.fromFile(controlFile)
    val controlSize = (controlImage.width, controlImage.height)

    val colWidth = controlSize._1 / cols
    val rowHeight = controlSize._2 / rows

    log.info(s"loading ${sampleFiles.length} images")

    val images = sampleFiles.map(f => Image.fromFile(f).scaleTo(colWidth, rowHeight, ScaleMethod.FastScale))

    val allImages = doManipulate match {
      case false => images
      case true =>
        log.info(s"doing manipulations")
        val manips = Array(SummerManipulator, DiffuseManipulator, GlowManipulator, ChromeManipulator, OilManipulator, EdgeManipulator, LensBlurManipulator) //LensBlurManipulator
        val manipped = ManipulateAllWithAllOnce(images, manips)
        images.++(manipped)
    }

    val assembledImage = compose(rows, cols, allImages, controlImage, controlSize)

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
