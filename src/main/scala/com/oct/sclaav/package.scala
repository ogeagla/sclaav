package com.oct

import java.net.URI

import com.oct.sclaav.Mode.Mode
import com.sksamuel.scrimage.Image

package object sclaav {

  object MapsModes {
    def apply(mode: String) = mode match {
      case "permute" => Mode.MOSAIC_PERMUTE_ALL_FILES
      case "single" => Mode.MOSAIC_SINGLE_FILE
      case "free-random-composite" => Mode.FREE_COMPOSITE_RANDOM
      case "free-ga-composite" => Mode.FREE_COMPOSITE_GA
    }
  }

  object Mode extends Enumeration {
    type Mode = Value
    val MOSAIC_PERMUTE_ALL_FILES,
        MOSAIC_SINGLE_FILE,
        FREE_COMPOSITE_RANDOM,
        FREE_COMPOSITE_GA = Value
  }

  case class Config(
                     maxSamplePhotos: Int = 10,
                     rows: Int = 8,
                     cols: Int = 8,
                     mode: Mode = Mode.MOSAIC_SINGLE_FILE,
                     in: Option[URI] = None,
                     out: Option[URI] = None,
                     singleTarget: Option[URI] = None,
                     manipulate: Boolean = false,
                     verbose: Boolean = false,
                     debug: Boolean = false) {

    def validate: Either[String, Unit] = {
      val validations = Seq(validateMode)
      validations.foldLeft[Either[String, Unit]](Right(Unit)) { (vs, v) =>
        if (vs.isLeft)
          vs
        else if (v.isLeft)
          v
        else
          Right(Unit)
      }
    }

    def validateMode: Either[String, Unit] = (mode, singleTarget) match {
      case (Mode.MOSAIC_SINGLE_FILE, None) =>
        Left("Should provide a target file when using Mosaic mode with a single file")
      case (Mode.MOSAIC_PERMUTE_ALL_FILES, Some(_)) =>
        Left("Should not provide a target file when using Mosaic mode with permuting all input files")
    }

  }

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

  trait SingleAbsoluteAssembler {
    def apply(backgroundImage: Image, pixelLocation: (Int, Int), theImageToInsert: Image): Image
  }

  trait CompleteGridAssembler {
    def apply(backgroundImage: Image, imagesWIndex: Array[(Image, (Int, Int))], gridSize: (Int, Int)): Image
  }

  trait CompleteAssembler {
    def apply(theReferenceImage: Image, theBackgroundImage: Image, samples: Array[Image]): Image
  }

  trait PixelLocationComputer {
    def apply(gridSize: (Int, Int), theGridLocation: (Int, Int), canvasSizeInPixels: (Int, Int)): (Int, Int)
  }

  trait ManipulationsCrossHybridizer {
    def apply(mans1: Array[ImageManipulator], mans2: Array[ImageManipulator]): Array[ImageManipulator]
  }

  trait ManipulationsHybridizer {
    def apply(man: Array[ImageManipulator]): Array[ImageManipulator]
  }

  case class IterationStats(
                             chainSizeMeans: Array[Double] = Array(),
                             chainSizeStddevs: Array[Double] = Array(),
                             populationFitness: Array[Double] = Array(),
                             populationDistanceMeans: Array[Double] = Array(),
                             populationDistanceStddevs: Array[Double] = Array(),
                             bestDistances: Array[Double] = Array(),
                             worstDistances: Array[Double] = Array()
                           )
}
