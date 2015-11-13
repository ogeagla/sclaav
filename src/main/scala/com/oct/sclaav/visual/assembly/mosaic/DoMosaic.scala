package com.oct.sclaav.visual.assembly.mosaic

import java.io.File

import com.oct.sclaav.visual.assembly.basic.SimpleCompleteGridAssembler
import com.oct.sclaav.visual.computation.{MatchByArgbAverage, SimpleArgbDistance, SimpleArgbEstimator}
import com.oct.sclaav.visual.manipulators._
import com.sksamuel.scrimage.nio.JpegWriter
import com.sksamuel.scrimage.{Color, Image, ScaleMethod}
import org.slf4j.LoggerFactory

import scala.collection.parallel.mutable.ParArray

object DoMosaic {

  val log = LoggerFactory.getLogger(getClass)

  implicit val writer = JpegWriter.Default
  def apply(
             controlFile: File,
             sampleFiles: Array[File],
             cols: Int,
             rows: Int,
             outPath: File,
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
        val manips = Array(SummerManipulator, DiffuseManipulator, GlowManipulator, ChromeManipulator)
        val manipped = ManipulateAllWithAllOnce(images, manips)
        images.++(manipped)
    }

    val listBuffer = new ParArray[(Image, (Int, Int))](rows * cols)

    log.info(s"cropping and matching using sample size ${allImages.length}")

    for (c <- (0 to cols - 1).par) {
      log.info(s"${c + 1} of $cols cols complete")
      for(r <- 0 to rows - 1) {
        val cropped = SimpleCrop((cols, rows), (c, r), controlImage)

        val matchToCropped = MatchByArgbAverage(SimpleArgbEstimator, SimpleArgbDistance, cropped, allImages)

        listBuffer.update(rows*c + r, (matchToCropped, (c, r)))
      }
    }

    val transparentCanvas = Image.filled(controlSize._1, controlSize._2, Color.Transparent)

    log.info("assembling")
    val assembledImage = SimpleCompleteGridAssembler(transparentCanvas,listBuffer.seq.toArray , (cols, rows))

    log.info("persisting")

    val controlFilePhotoName = controlFile.getPath.split("/").last

    if (writeReferenceImg) {
      val refPath = new File(outPath, s"${controlFilePhotoName}_ref.jpeg")
      controlImage.output(refPath)
    }

    val assembledPath = outputFilename match {
      case None => new File(outPath, s"${controlFilePhotoName}_assembled.jpeg")
      case Some(name) => new File(outPath, s"${name}")
    }
    assembledImage.output(assembledPath)
    assembledImage
  }
}
