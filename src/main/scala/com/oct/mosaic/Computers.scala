package com.oct.mosaic

import com.sksamuel.scrimage.filter.GrayscaleFilter
import com.sksamuel.scrimage.{Image, ScaleMethod}


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

    val rgb1: Array[Array[Int]] =
      img1.scaleTo(scaleWidth, scaleHeight, ScaleMethod.FastScale).filter(GrayscaleFilter).rgb
    val rgb2: Array[Array[Int]] =
      img2.scaleTo(scaleWidth, scaleHeight, ScaleMethod.FastScale).filter(GrayscaleFilter).rgb

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