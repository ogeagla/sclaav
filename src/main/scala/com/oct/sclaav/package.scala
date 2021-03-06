package com.oct

import java.net.URI

import com.oct.sclaav.Mode.Mode
import com.sksamuel.scrimage.Image

package object sclaav {

  object MapsModes {
    def apply(mode: String) = mode match {
      case "permute" => Mode.MOSAIC_PERMUTE_ALL_FILES
      case "single" => Mode.MOSAIC_SINGLE_FILE
      case "mosaic-of-mosaics" => Mode.MOSAIC_OF_MOSAICS
      case "free-random-composite" => Mode.FREE_COMPOSITE_RANDOM
      case "free-ga-composite" => Mode.FREE_COMPOSITE_GA
//      case "similarity-permute" => Mode.SIMILARITY_PERMUTE
      case "similarity" => Mode.SIMILARITY
    }
  }

  object Mode extends Enumeration {
    type Mode = Value
    val MOSAIC_PERMUTE_ALL_FILES,
        MOSAIC_SINGLE_FILE,
        MOSAIC_OF_MOSAICS,
        FREE_COMPOSITE_RANDOM,
        FREE_COMPOSITE_GA,
//        SIMILARITY_PERMUTE,
        SIMILARITY = Value
  }

  case class Config(
      maxSamplePhotos: Option[Int] = Some(10),
      rows: Option[Int] = None,
      cols: Option[Int] = None,
      mode: Mode = Mode.MOSAIC_SINGLE_FILE,
      in: Option[URI] = None,
      out: Option[URI] = None,
      singleTarget: Option[URI] = None,
      manipulate: Boolean = false,
      verbose: Boolean = false,
      debug: Boolean = false) {

    def validate: Either[String, Unit] = {
      val validations = Seq(
        validateMode,
        validateMosaic,
        validateOutputDir
      )
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
      case (Mode.SIMILARITY, None) =>
        Left("Should provide a single target for similarity")
//      case (Mode.SIMILARITY_PERMUTE, Some(_)) =>
//        Left("Should not provide target image when permuting over images")
      case (_, _) =>
        Right(Unit)
    }

    def validateMosaic: Either[String, Unit] = (mode, rows, cols) match {
      case (Mode.MOSAIC_SINGLE_FILE, Some(r), Some(c)) => Right(Unit)
      case (Mode.MOSAIC_PERMUTE_ALL_FILES, Some(r), Some(c)) => Right(Unit)
      case (Mode.MOSAIC_SINGLE_FILE, _, _) => Left("Should provide rows and cols for mosaic")
      case (Mode.MOSAIC_PERMUTE_ALL_FILES, _, _) => Right("Should provide rows and cols for mosaic")
      case (_, _, _) => Right(Unit)
    }

    def validateOutputDir: Either[String, Unit] = (mode, out) match {
      case (Mode.MOSAIC_SINGLE_FILE, None) => Right(Unit)
      case (_, None) => Left("Should provide output dir")
      case _ => Right(Unit)
    }

  }

  case class Argb(a: Int, r: Int, g: Int, b: Int)

  trait ArgbEstimator {
    def apply(img: Image): Argb
  }

  trait ArgbDistance {
    def apply(argb1: Argb, argb2: Argb): Double
  }

  object ImageManipulationMonoid {
    def mappend(i1: Image, i2: Image) = ???

  }

  trait ImageManipulator {
    def apply(img: Image): Image
  }

  trait Similarity {
    def apply(img1: Image, img2: Image, scaleWidth: Int, scaleHeight: Int): Double
  }

  trait UniformGridCropper {
    def apply(gridSize: (Int, Int), locationToCrop: (Int, Int), img: Image): Image
  }

  trait AbsoluteCropper {
    def apply(startH: Int, startW: Int, endH: Int, endW: Int, img: Image): Image
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

  trait CompleteAbsoluteAssembler {
    def apply(backgroundImage: Image, imagesWPosition: Array[(Image, (Int, Int))]): Image
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
      worstDistances: Array[Double] = Array())

  case class QuadrilateralCell(
      startCol: Int,
      startRow: Int,
      endCol: Int,
      endRow: Int)

  case class QuadrilateralGrid(
      rows: Int,
      cols: Int,
      listOfTheStuff: Array[QuadrilateralCell])

  case class AbsoluteQuadrilateralPosition(startW: Int, startH: Int, endW: Int, endH: Int)

  class QuadrilateralGridToAbsolutePositions(sizeW: Int, sizeH: Int) {
    def apply(grid: QuadrilateralGrid): Array[AbsoluteQuadrilateralPosition] = {

      val cols = grid.cols
      val rows = grid.rows

      val colPixels = sizeW / cols
      val rowPixels = sizeH / rows

      grid.listOfTheStuff.map { cell =>

        val colW = math.max(cell.endCol - cell.startCol, 1)
        val rowW = math.max(cell.endRow - cell.startRow, 1)

        val startWP = cell.startCol * colPixels
        val endWP = (cell.endCol + 1) * colPixels
        val startHP = cell.startRow * rowPixels
        val endHP = (cell.endRow + 1) * rowPixels

        new AbsoluteQuadrilateralPosition(startWP, startHP, endWP, endHP)
      }
    }
  }

  trait ImageToQuadGridThing {
    def apply(img: Image, rows: Int, cols: Int): QuadrilateralGrid
  }

}
