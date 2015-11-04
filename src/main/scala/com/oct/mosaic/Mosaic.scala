package com.oct.mosaic

import java.io.File

import com.sksamuel.scrimage.composite.AlphaComposite
import com.sksamuel.scrimage.filter._
import com.sksamuel.scrimage.nio.JpegWriter
import com.sksamuel.scrimage.{Color, Image, ScaleMethod}
import org.slf4j.LoggerFactory

import scala.collection.parallel.mutable.ParArray
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

object DoMosaic {

  val log = LoggerFactory.getLogger(getClass)

  implicit val writer = JpegWriter.Default
  def apply(controlFile: File, sampleFiles: Array[File], cols: Int, rows: Int, outPath: File, doManipulate: Boolean = false) = {

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

    val assembledPath = new File(outPath, s"${controlFilePhotoName}_assembled.jpeg")
    val refPath = new File(outPath, s"${controlFilePhotoName}_ref.jpeg")

    assembledImage.output(assembledPath)
    controlImage.output(refPath)
  }
}

object MatchByArgbAverage {
  def apply(argbEstimator: ArgbEstimator, argbDistance: ArgbDistance, refImage: Image, otherImages: Array[Image]): Image = {

    val refArgb = argbEstimator(refImage)

    val argbs = otherImages.map {
      i => (i, argbEstimator(i))
    }

    val argbsWDistance = argbs.map {
      case (i, argb) => (i, argbDistance(refArgb, argb))
    }

    argbsWDistance.sortBy {
      case (i, dist) => dist
    }.head._1
  }
}

object SimpleCompleteRandomAssembler extends RandomMinCompleteAssembler {
  override def apply(theReferenceImage: Image, theBackgroundImage: Image, samples: Array[Image]): Image = {

    var theImage = theBackgroundImage
    var distance = ImageSimilarityArgbDistance2(theReferenceImage, theBackgroundImage)
    val (w, h) = (theImage.width, theImage.height)

    scala.util.Random.setSeed(13)

    for (i <- 0 to 1000) {
      println(s"$i")

      val randomX = scala.util.Random.nextInt(w)
      val randomY = scala.util.Random.nextInt(h)
      val randomImageIndex = scala.util.Random.nextInt(samples.length)

//      println(s"x: $randomX y: $randomY index: $randomImageIndex")

      val imgTest = samples(randomImageIndex)
      val sampledImgSize = (imgTest.width, imgTest.height)
      val newW = Random.nextInt(sampledImgSize._1) + 1
      val newH = Random.nextInt(sampledImgSize._2) + 1
//      println(s"sample size: $newW $newH")

      val scaledImg = imgTest.scaleTo(newW, newH, ScaleMethod.FastScale)

      val halfSeeThroughImg = Image.filled(newW, newH, Color.Transparent).composite(new AlphaComposite(0.5), scaledImg)

      val maybeNewImage = SimpleSingleAbsoluteAssembler(theImage, (randomX, randomY), halfSeeThroughImg)

      val maybeNewDistance = ImageSimilarityArgbDistance2(theReferenceImage, maybeNewImage)

//      println(s"old dist: $distance ; new distance: $maybeNewDistance")

      distance - maybeNewDistance match {
        case better if better > 0.0 =>
          theImage = maybeNewImage
          distance = maybeNewDistance
        case worse if worse <= 0.0 =>
      }
    }

    theImage
  }
}

object SimpleCompleteGridAssembler extends CompleteGridAssembler {

  val log = LoggerFactory.getLogger(getClass)

  override def apply(backgroundImage: Image, imagesWIndex: Array[(Image, (Int, Int))], gridSize: (Int, Int)): Image = {

    val (canvasW, canvasH) = (backgroundImage.width, backgroundImage.height)

    log.info("computing pixel locations from grid locations")

    val imagesWPixelLocations = imagesWIndex.map {
      case (i, (colIndex, rowIndex)) =>
        (i, SimplePixelLocationComputer(gridSize, (colIndex, rowIndex), (canvasW, canvasH)))
    }

    log.info("assembling image")

    val theAssembledImage = imagesWPixelLocations.foldLeft(backgroundImage) {
      case (canvasImage, (image, (i1, i2))) =>
        SimpleSingleAbsoluteAssembler(canvasImage, (i1, i2), image)
    }

    theAssembledImage
  }
}

object SimpleSingleAbsoluteAssembler extends SingleAbsoluteAssembler {
  override def apply(backgroundImage: Image, pixelLocation: (Int, Int), theImageToInsert: Image): Image = {
    backgroundImage.overlay(theImageToInsert, pixelLocation._1, pixelLocation._2)
  }
}

object SimpleCrop extends DiscreteCropper {
  override def apply(gridSize: (Int, Int), locationToCrop: (Int, Int), img: Image): Image = {
    val (imgW, imgH) = (img.width, img.height)
    val (colCellSize, rowCellSize) = (imgW / gridSize._1, imgH / gridSize._2)
    val (xToCrop, yToCrop) =  SimplePixelLocationComputer(gridSize, locationToCrop, (imgW, imgH))
    img.trim(xToCrop, yToCrop, imgW - xToCrop - colCellSize, imgH - yToCrop - rowCellSize)
  }
}

object ManipulateAllWithAllOnce {
  val log = LoggerFactory.getLogger(getClass)

  def apply(imgs: Array[Image], manips: Array[ImageManipulator]): Array[Image] = {

    val listBuffer = new ParArray[Image](imgs.length * manips.length)

    for (imgIndex <- imgs.indices.par) {
      log.info(s"manipping ${imgIndex + 1} of ${imgs.length}")
      for (manipsIndex <- manips.indices) {
        val img = imgs(imgIndex)
        val manip = manips(manipsIndex)

        listBuffer.update(manips.length * imgIndex + manipsIndex, manip(img))
      }
    }
    listBuffer.toArray
  }
}

object ApplyManipulations {
  def apply(img: Image, manips: List[ImageManipulator]): Image = {
    manips.foldLeft(img)((image, maniper) => maniper(image))
  }
}

object MixManipulations {
  def apply(man1: List[ImageManipulator], man2: List[ImageManipulator]): List[ImageManipulator] = {
    val slice1 = new Random().nextInt(man1.length)
    val slice2 = new Random().nextInt(man2.length)

    val flipper = new Random().nextBoolean()

    val (man1b, man2b) = flipper match {
      case true =>
        val a = man1.slice(0, slice1)
        val b = man2.slice(slice2, man2.length)
        (a, b)
      case false =>
        val a = man1.slice(slice1, man1.length)
        val b = man2.slice(0, slice2)
        (a, b)
    }

    man1b ++: man2b
  }
}

object SummerManipulator extends ImageManipulator {
  override def apply(img: Image): Image = {
    img.filter(SummerFilter())
  }
}

object ChromeManipulator extends ImageManipulator {
  override def apply(img: Image): Image = {
    img.filter(ChromeFilter())
  }
}

object DiffuseManipulator extends ImageManipulator {
  override def apply(img: Image): Image = {
    img.filter(DiffuseFilter())
  }
}

object GlowManipulator extends ImageManipulator {
  override def apply(img: Image): Image = {
    img.filter(GlowFilter())
  }
}