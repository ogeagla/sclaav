package com.oct.mosaic

import com.sksamuel.scrimage.composite.AlphaComposite
import com.sksamuel.scrimage.filter.{GlowFilter, DiffuseFilter, ChromeFilter, SummerFilter}
import com.sksamuel.scrimage.{Color, Image}
import org.slf4j.LoggerFactory

import scala.collection.parallel.mutable.ParArray
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global

//bitches

object AddTransparencyToImage extends ImageManipulator {
  override def apply(img: Image): Image = {
    val (w, h) = (img.width, img.height)
    Image.filled(w, h, Color.Transparent).composite(new AlphaComposite(0.5), img)
  }
}

class AlphaCompositeManipulator(baseImage: Image, x: Int, y: Int) extends ImageManipulator {
  lazy val seeThruBaseImage = AddTransparencyToImage(baseImage)
  override def apply(img: Image): Image = {
    val seeThruProvidedImg = AddTransparencyToImage(img)
    SimpleSingleAbsoluteAssembler(baseImage, (x, y), seeThruProvidedImg)
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
  def apply(img: Image, manips: Array[ImageManipulator]): Image = {
    manips.foldLeft(img)((image, maniper) => maniper(image))
  }
}

object MixManipulationsRandomly {
  def apply(man1: Array[ImageManipulator], man2: Array[ImageManipulator]): Array[ImageManipulator] = {
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