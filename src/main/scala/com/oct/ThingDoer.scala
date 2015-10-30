package com.oct

import com.sksamuel.scrimage.{ScaleMethod, Image}
import org.slf4j.LoggerFactory

import scala.util.Random

object ThingDoer {

  lazy val log = LoggerFactory.getLogger(getClass)

  trait ImageManipulator {
    def apply(img: Image): Image
  }

  object Manipulate {
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

  class ManipulatorOne extends ImageManipulator {
    override def apply(img: Image): Image = {
      img.flipX
    }
  }

  object Distance2 {
    def apply(arr1: List[Double], arr2: List[Double]): Double = {
      assert(arr1.length == arr2.length)
      math.sqrt(arr1.zip(arr2).map{case (a, b) => math.pow(a-b, 2.0)}.foldLeft(0.0)(_ + _))
    }
  }

  object ImageSimilarity {
    def apply(img1: Image, img2: Image): Double = {

      println("stuff")

      log.info(s"getting similarity")

      val argb1: Array[Array[Int]] = img1.scaleTo(8, 8, ScaleMethod.FastScale).argb
      val argb2: Array[Array[Int]] = img2.scaleTo(8, 8, ScaleMethod.FastScale).argb

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



  def doIt() = {

  }

}
