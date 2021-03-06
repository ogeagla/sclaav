package com.oct.sclaav.visual.manipulators

import com.oct.sclaav._
import com.oct.sclaav.visual.assembly.grid.SimpleSingleAbsoluteAssembler
import com.oct.sclaav.visual.computation.SimplePixelLocationComputer
import com.sksamuel.scrimage.composite.AlphaComposite
import com.sksamuel.scrimage.filter._
import com.sksamuel.scrimage.{Color, Image}
import org.slf4j.LoggerFactory

import scala.collection.mutable.ArrayBuffer
import scala.collection.parallel.mutable.ParArray
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

//bitches

object CreatesTransparentImage {
  def apply(width: Int, height: Int): Image = {
    Image.filled(width, height, Color.Transparent)
  }
}

object AddTransparencyToImage extends ImageManipulator {
  override def apply(img: Image): Image = {
    val (w, h) = (img.width, img.height)
    Image.filled(w, h, Color.Transparent).composite(new AlphaComposite(0.5), img)
  }
}

class ToPositionOnTransparentBackground(backgroundW: Int, backgroundH: Int, x: Int, y: Int) extends ImageManipulator {
  override def apply(img: Image): Image = {
    val canvas = CreatesTransparentImage(backgroundW, backgroundH)
    SimpleSingleAbsoluteAssembler(canvas, (x, y), img)
  }
}

class AlphaCompositeManipulator(theImageToApply: Image, x: Int, y: Int) extends ImageManipulator {
  override def apply(img: Image): Image = {

    val (baseW, baseH) = (img.width, img.height)
    val applicator = (new ToPositionOnTransparentBackground(baseW, baseH, x, y))(AddTransparencyToImage(theImageToApply))
    SimpleSingleAbsoluteAssembler(img, (0, 0), applicator)
  }
}

class TransparencyComposityManipulator(x: Int, y: Int, baseImage: Option[Image] = None) extends ImageManipulator {
  lazy val theBaseImage = baseImage match {
    case None => Image.filled(x, y, Color.Transparent)
    case Some(img) => AddTransparencyToImage(img)
  }
  override def apply(img: Image): Image = {
    val seeThruProvidedImg = AddTransparencyToImage(img)
    SimpleSingleAbsoluteAssembler(theBaseImage, (x, y), seeThruProvidedImg)
  }
}

object SimpleCrop extends UniformGridCropper {
  override def apply(gridSize: (Int, Int), locationToCrop: (Int, Int), img: Image): Image = {
    val (imgW, imgH) = (img.width, img.height)
    val (colCellSize, rowCellSize) = (imgW / gridSize._1, imgH / gridSize._2)
    val (xToCrop, yToCrop) =  SimplePixelLocationComputer(gridSize, locationToCrop, (imgW, imgH))
    img.trim(xToCrop, yToCrop, imgW - xToCrop - colCellSize, imgH - yToCrop - rowCellSize)
  }
}

object SimpleAbsoluteCrop extends AbsoluteCropper {
  override def apply(startW: Int, startH: Int, endW: Int, endH: Int, img: Image): Image = {
    img.trim(startW, startH, img.width - endW, img.height - endH)
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

object ModManipulationsRandomlyRemove extends ManipulationsHybridizer {
  override def apply(man: Array[ImageManipulator]): Array[ImageManipulator] = {
    man.length match {
      case len if len / 4 > 0 =>
        val elems: ArrayBuffer[ImageManipulator] = man.to[ArrayBuffer]
        val howManyToRemove = Random.nextInt(len)
        (0 to howManyToRemove - 1).foreach { i =>
          val index = Random.nextInt(elems.length)
          elems.remove(index)
        }
        elems.toArray
      case len =>
        man
    }
  }
}

object ModManipulationsRandomlySplit extends ManipulationsHybridizer {
  override def apply(man: Array[ImageManipulator]): Array[ImageManipulator] = {
    man.length match {
      case len if len > 0 =>
        Random.nextBoolean() match {
          case true =>
            man.slice(0, Random.nextInt(man.length))
          case false =>
            man.slice(Random.nextInt(man.length), man.length)
        }
      case len =>
        man
    }
  }
}

object MixManipulationsCombinator extends ManipulationsCrossHybridizer {
  override def apply(mans1: Array[ImageManipulator], mans2: Array[ImageManipulator]): Array[ImageManipulator] = {
    Random.nextBoolean() match {
      case true =>
        mans1.++:(mans2)
      case false =>
        mans2.++:(mans1)
    }
  }
}

object MixManipulationsRandomlyPointwise extends ManipulationsCrossHybridizer {
  override def apply(mans1: Array[ImageManipulator], mans2: Array[ImageManipulator]): Array[ImageManipulator] = {

    val size1 = mans1.length
    val size2 = mans2.length

    (size1, size2) match {
      case (s1, s2) if math.min(s1, s2) / 4 > 0 =>
        val howManyToSwitch = Random.nextInt( math.min(s1, s2) / 4 )

        val (whichToReturn, whichToNotReturn) = Random.nextBoolean() match {
          case true => (mans1, mans2)
          case false => (mans2, mans2)
        }

        val pts = (0 to howManyToSwitch - 1).map { h =>
          val pt1 = Random.nextInt(whichToReturn.length)
          val pt2 = Random.nextInt(whichToNotReturn.length)
          (pt1, pt2)
        }

        val returnThing: ArrayBuffer[ImageManipulator] = whichToReturn.to[ArrayBuffer]

        for ((pt1, pt2) <- pts) {
          returnThing.update(pt1, whichToNotReturn(pt2))
        }

        returnThing.toArray
      case (s1, s2) =>
        mans1 ++: mans2
    }
  }
}

object MixManipulationsRandomlyBy2SegmentSwap extends ManipulationsCrossHybridizer {
  def apply(man1: Array[ImageManipulator], man2: Array[ImageManipulator]): Array[ImageManipulator] = {

    val (len1, len2) = (man1.length, man2.length)

    (len1, len2) match {
      case (l1, l2) if (l1 > 0) && (l2 > 0) =>
        val slice1 = Random.nextInt(l1)
        val slice2 = Random.nextInt(l2)
        val flipper = Random.nextBoolean()
        val (man1b, man2b) = flipper match {
          case true =>
            val a = man1.slice(0, slice1)
            val b = man2.slice(slice2, l2)
            (a, b)
          case false =>
            val a = man1.slice(slice1, l1)
            val b = man2.slice(0, slice2)
            (a, b)
        }
        man1b ++: man2b
      case (l1, l2) =>
        man1 ++: man2
    }
  }
}

object EdgeManipulator extends ImageManipulator {
  override def apply(img: Image): Image = {
    img.filter(EdgeFilter)
  }
}

object OilManipulator extends ImageManipulator {
  override def apply(img: Image): Image = {
    img.filter(OilFilter(6, 8))
  }
}

object LensBlurManipulator extends ImageManipulator {
  override def apply(img: Image): Image = {
    img.filter(LensBlurFilter())
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