package com.oct

import java.io.File

import com.oct.mosaic.Mode.Mode
import com.sksamuel.scrimage.Image

package object mosaic {

  object MapsModes {
    def apply(mode: String) = mode match {
      case "permute" => Mode.PERMUTE_ALL_FILES
      case "single" => Mode.SINGLE_FILE
    }
  }

  object Mode extends Enumeration {
    type Mode = Value
    val PERMUTE_ALL_FILES, SINGLE_FILE = Value
  }

  case class Config(
      maxSamplePhotos: Int = 10,
      rows: Int = 8,
      cols: Int = 8,
      manipulate: Boolean = false,
      mode: Mode = Mode.PERMUTE_ALL_FILES,
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

  trait SingleAssembler {
    def apply(backgroundImage: Image, pixelLocation: (Int, Int), theImageToInsert: Image): Image
  }

  trait CompleteAssembler {
    def apply(backgroundImage: Image, imagesWIndex: Array[(Image, (Int, Int))], gridSize: (Int, Int)): Image
  }

  trait PixelLocationComputer {
    def apply(gridSize: (Int, Int), theGridLocation: (Int, Int), canvasSizeInPixels: (Int, Int)): (Int, Int)
  }
}
