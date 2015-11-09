package com.oct.sclaav

import java.io.File

import com.oct.sclaav.visual.Mode.Mode
import com.sksamuel.scrimage.Image

package object visual {

  object MapsModes {
    def apply(mode: String) = mode match {
      case "permute" => Mode.MOSAIC_PERMUTE_ALL_FILES
      case "single" => Mode.MOSAIC_SINGLE_FILE
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
                     manipulate: Boolean = false,
                     mode: Mode = Mode.MOSAIC_PERMUTE_ALL_FILES,
                     singleTarget: File = new File("./singleTarget"),
                     in: File = new File("./in"),
                     out: File = new File("./out"),
                     verbose: Boolean = false,
                     debug: Boolean = false)

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
}