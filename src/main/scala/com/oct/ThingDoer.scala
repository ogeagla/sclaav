package com.oct

import com.sksamuel.scrimage.filter.GrayscaleFilter
import com.sksamuel.scrimage.{Image, ScaleMethod}

import scala.util.Random




case class Argb(a: Int, r: Int, g: Int, b: Int)

trait ArgbEstimator {
  def apply(img: Image): Argb
}

trait ArgbDistance {
  def apply(argb1: Argb, argb2: Argb): Double
}

trait ImageManipulator {
  def apply(img: Image): Image
}

trait Similarity {
  def apply(img1: Image, img2: Image, scaleWidth: Int, scaleHeight: Int): Double
}

trait DiscreteCropper {
  def apply(gridSize: (Int, Int), locationToCrop: (Int, Int), img: Image): Image
}

trait SingleAssembler {
  def apply(backgroundImage: Image, pixelLocation: (Int, Int), theImageToInsert: Image): Image
}

trait CompleteAssembler {
  def apply(backgroundImage: Image, imagesWIndex: Array[(Image, (Int, Int))], gridSize: (Int, Int)): Image
}

trait PixelLocationComputer {
  def apply(gridSize: (Int, Int), theGridLocation: (Int, Int), canvasSizeInPixels: (Int, Int)): (Int, Int)
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

object SimplePixelLocationComputer extends PixelLocationComputer {
  override def apply(gridSize: (Int, Int), theGridLocation: (Int, Int), canvasSizeInPixels: (Int, Int)): (Int, Int) = {
    val (colCellSize, rowCellSize) = (canvasSizeInPixels._1 / gridSize._1, canvasSizeInPixels._2 / gridSize._2)
    val (x, y) = (colCellSize * theGridLocation._1, rowCellSize * theGridLocation._2)
    (x, y)
  }
}

object SimpleArgbDistance extends ArgbDistance {
  override def apply(argb1: Argb, argb2: Argb): Double = {
    val da = math.abs(argb1.a - argb2.a)
    val dr = math.abs(argb1.r - argb2.r)
    val dg = math.abs(argb1.g - argb2.g)
    val db = math.abs(argb1.b - argb2.b)

    math.sqrt(da*da + dr*dr + dg*dg + db*db)
  }
}

object SimpleCompleteAssembler extends CompleteAssembler {
  override def apply(backgroundImage: Image, imagesWIndex: Array[(Image, (Int, Int))], gridSize: (Int, Int)): Image = {

    val (canvasW, canvasH) = (backgroundImage.width, backgroundImage.height)

    println("computing pixel locations from grid locations")

    val imagesWPixelLocations = imagesWIndex.map {
      case (i, (colIndex, rowIndex)) =>
        (i, SimplePixelLocationComputer(gridSize, (colIndex, rowIndex), (canvasW, canvasH)))
    }
    
    println("assembling image")

    val theAssembledImage = imagesWPixelLocations.foldLeft(backgroundImage){
      case (canvasImage, (image, (i1, i2))) =>
        SimpleSingleAssembler(canvasImage, (i1, i2), image)
    }

    theAssembledImage
  }
}

object SimpleSingleAssembler extends SingleAssembler {
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

object Average {
  def apply(arr: Array[Int]): Int = arr.sum / arr.length
}

object SimpleArgbEstimator extends ArgbEstimator {
  override def apply(img: Image): Argb = {

    val argb = img.argb

    val a = argb.map(_.apply(0))
    val r = argb.map(_.apply(1))
    val g = argb.map(_.apply(2))
    val b = argb.map(_.apply(3))

    Argb(Average(a), Average(r), Average(g), Average(b))
  }
}

object UniqueCartesian2 {
  def apply(imgs1: List[Image], imgs2: List[Image]): List[(Image, Image)] = {
    var array = List[(Image, Image)]()

    for (i1 <- imgs1) {
      for (i2 <- imgs2) {
        val tup = (i1, i2)
        val put = (i2, i1)

        if (array.contains(tup) || array.contains(put) || i1 == i2) {

        } else {
          array = tup :: array
        }

      }
    }

    array
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

object Distance2 {
  def apply(arr1: List[Double], arr2: List[Double]): Double = {
    assert(arr1.length == arr2.length)
    math.sqrt(arr1.zip(arr2).map{case (a, b) => math.pow(a-b, 2.0)}.foldLeft(0.0)(_ + _))
  }
}

object ImageSimilarityArgbDistance2 {
  def apply(img1: Image, img2: Image): Double = (new ImageSimilarityArgbDistance2)(img1, img2)
}

class ImageSimilarityArgbDistance2 extends Similarity {
  def apply(img1: Image, img2: Image, scaleWidth: Int = 32, scaleHeight: Int = 32): Double = {

    val argb1: Array[Array[Int]] = img1.scaleTo(scaleWidth, scaleHeight, ScaleMethod.FastScale).argb
    val argb2: Array[Array[Int]] = img2.scaleTo(scaleWidth, scaleHeight, ScaleMethod.FastScale).argb

    val a1 = argb1.map(_.apply(0).toDouble).toList
    val r1 = argb1.map(_.apply(1).toDouble).toList
    val g1 = argb1.map(_.apply(2).toDouble).toList
    val b1 = argb1.map(_.apply(3).toDouble).toList

    val a2 = argb2.map(_.apply(0).toDouble).toList
    val r2 = argb2.map(_.apply(1).toDouble).toList
    val g2 = argb2.map(_.apply(2).toDouble).toList
    val b2 = argb2.map(_.apply(3).toDouble).toList

    val aDist = Distance2(a1, a2)
    val rDist = Distance2(r1, r2)
    val gDist = Distance2(g1, g2)
    val bDist = Distance2(b1, b2)

    math.sqrt(aDist*aDist + rDist*rDist + gDist*gDist + bDist*bDist)
  }
}

object ImageSimilarityRgbDistance2 {
  def apply(img1: Image, img2: Image): Double = (new ImageSimilarityRgbDistance2)(img1, img2)
}

class ImageSimilarityRgbDistance2 extends Similarity {
  def apply(img1: Image, img2: Image, scaleWidth: Int = 32, scaleHeight: Int = 32): Double = {

    val rgb1: Array[Array[Int]] = img1.scaleTo(scaleWidth, scaleHeight, ScaleMethod.FastScale).rgb
    val rgb2: Array[Array[Int]] = img2.scaleTo(scaleWidth, scaleHeight, ScaleMethod.FastScale).rgb

    val r1 = rgb1.map(_.apply(0).toDouble).toList
    val g1 = rgb1.map(_.apply(1).toDouble).toList
    val b1 = rgb1.map(_.apply(2).toDouble).toList

    val r2 = rgb2.map(_.apply(0).toDouble).toList
    val g2 = rgb2.map(_.apply(1).toDouble).toList
    val b2 = rgb2.map(_.apply(2).toDouble).toList

    val rDist = Distance2(r1, r2)
    val gDist = Distance2(g1, g2)
    val bDist = Distance2(b1, b2)

    math.sqrt(rDist*rDist + gDist*gDist + bDist*bDist)
  }
}

object ImageSimilarityGrayscaleDistance2 {
  def apply(img1: Image, img2: Image): Double = (new ImageSimilarityGrayscaleDistance2)(img1, img2)
}

class ImageSimilarityGrayscaleDistance2 extends Similarity {
  def apply(img1: Image, img2: Image, scaleWidth: Int = 32, scaleHeight: Int = 32): Double = {

    val rgb1: Array[Array[Int]] = img1.scaleTo(scaleWidth, scaleHeight, ScaleMethod.FastScale).filter(GrayscaleFilter).rgb
    val rgb2: Array[Array[Int]] = img2.scaleTo(scaleWidth, scaleHeight, ScaleMethod.FastScale).filter(GrayscaleFilter).rgb

    val r1 = rgb1.map(_.apply(0).toDouble).toList
    val g1 = rgb1.map(_.apply(1).toDouble).toList
    val b1 = rgb1.map(_.apply(2).toDouble).toList

    val r2 = rgb2.map(_.apply(0).toDouble).toList
    val g2 = rgb2.map(_.apply(1).toDouble).toList
    val b2 = rgb2.map(_.apply(2).toDouble).toList

    val rDist = Distance2(r1, r2)
    val gDist = Distance2(g1, g2)
    val bDist = Distance2(b1, b2)

    math.sqrt(rDist*rDist + gDist*gDist + bDist*bDist)
  }
}
