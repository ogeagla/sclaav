package com.oct.sclaav.visual.computation

import java.io.File

import com.oct.sclaav._
import com.sksamuel.scrimage.filter.GrayscaleFilter
import com.sksamuel.scrimage.{Color, Image, ScaleMethod}
import org.slf4j.LoggerFactory

import scala.collection.mutable.ArrayBuffer


object ComputesMeanAndStddev {
  def apply(arr: Array[Double]): (Double, Double) = {
    val mean = arr.sum / arr.length
    val devs = arr.map(dist => (dist - mean) * (dist - mean))
    val stddev = Math.sqrt(devs.sum / arr.length)
    (mean, stddev)
  }
}

object Distance2 {
  def apply(arr1: Array[Double], arr2: Array[Double]): Double = {
    assert(arr1.length == arr2.length)
    math.sqrt(arr1.zip(arr2).map{case (a, b) => math.pow(a-b, 2.0)}.foldLeft(0.0)(_ + _))
  }
}

object RelativeImageSimilarityArgbDistance2 {
  def apply(img1: Image, img2: Image): Double = (new RelativeImageSimilarityArgbDistance2)(img1, img2)
}

class RelativeImageSimilarityArgbDistance2 extends Similarity {
  override def apply(img1: Image, img2: Image, scaleWidth: Int = 32, scaleHeight: Int = 32): Double = {

    val imgsDist = (new ImageSimilarityArgbDistance2)(img1, img2, scaleWidth, scaleHeight)
    val transparent = Image.filled(scaleWidth, scaleHeight, Color.Transparent)
    val distToTrans = (new ImageSimilarityArgbDistance2)(img1, transparent, scaleWidth, scaleHeight)

    imgsDist / distToTrans
  }
}

object ImageSimilarityArgbDistance2 {
  def apply(img1: Image, img2: Image): Double = (new ImageSimilarityArgbDistance2)(img1, img2)
}

class ImageSimilarityArgbDistance2 extends Similarity {
  def apply(img1: Image, img2: Image, scaleWidth: Int = 32, scaleHeight: Int = 32): Double = {

    val argb1: Array[Array[Int]] = img1.scaleTo(scaleWidth, scaleHeight, ScaleMethod.FastScale).argb
    val argb2: Array[Array[Int]] = img2.scaleTo(scaleWidth, scaleHeight, ScaleMethod.FastScale).argb

    val a1 = argb1.map(_.apply(0).toDouble)
    val r1 = argb1.map(_.apply(1).toDouble)
    val g1 = argb1.map(_.apply(2).toDouble)
    val b1 = argb1.map(_.apply(3).toDouble)

    val a2 = argb2.map(_.apply(0).toDouble)
    val r2 = argb2.map(_.apply(1).toDouble)
    val g2 = argb2.map(_.apply(2).toDouble)
    val b2 = argb2.map(_.apply(3).toDouble)

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

    val r1 = rgb1.map(_.apply(0).toDouble)
    val g1 = rgb1.map(_.apply(1).toDouble)
    val b1 = rgb1.map(_.apply(2).toDouble)

    val r2 = rgb2.map(_.apply(0).toDouble)
    val g2 = rgb2.map(_.apply(1).toDouble)
    val b2 = rgb2.map(_.apply(2).toDouble)

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

    val r1 = rgb1.map(_.apply(0).toDouble)
    val g1 = rgb1.map(_.apply(1).toDouble)
    val b1 = rgb1.map(_.apply(2).toDouble)

    val r2 = rgb2.map(_.apply(0).toDouble)
    val g2 = rgb2.map(_.apply(1).toDouble)
    val b2 = rgb2.map(_.apply(2).toDouble)

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
  def apply(imgs1: Array[Image], imgs2: Array[Image]): Array[(Image, Image)] = {
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

    array.toArray
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

object CellIntersectsExisting {
  val log = LoggerFactory.getLogger(getClass)

  def apply(arrBuff: ArrayBuffer[ArrayBuffer[Boolean]], cell: QuadrilateralCell): Boolean = {
    var doesNotInter = true

    for(c <- cell.startCol to cell.endCol; r <- cell.startRow to cell.endRow) {
      try {
        doesNotInter = doesNotInter && (!arrBuff(c)(r))
      } catch {
        case e: Exception =>
          log.error(e.getMessage)
      }
    }
    ! doesNotInter
  }

  object ApplyCellToTruthTable {
    def apply(table: ArrayBuffer[ArrayBuffer[Boolean]], cell: QuadrilateralCell): ArrayBuffer[ArrayBuffer[Boolean]] = {
      for(c <- cell.startCol to cell.endCol; r <- cell.startRow to cell.endRow) {
        table(c).update(r, true)
      }
      table
    }
  }
}
