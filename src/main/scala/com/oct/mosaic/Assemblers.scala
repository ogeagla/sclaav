package com.oct.mosaic

import com.sksamuel.scrimage.{ScaleMethod, Image}
import org.slf4j.LoggerFactory

import scala.util.Random

object SimpleCompleteGeneticAssembler extends CompleteAssembler {
  override def apply(theImageToAssemble: Image, theBackgroundImage: Image, samples: Array[Image]): Image = {

    val (maxW, maxH) = (theBackgroundImage.width, theBackgroundImage.height)
    val refDistance = ImageSimilarityArgbDistance2(theBackgroundImage, theImageToAssemble)

    val manipChainSize = 10
    val manipChainPopulationSize = 1
    val iterations = 1
    val topNSize = 1

    scala.util.Random.setSeed(13)

    val initChains = (0 to manipChainPopulationSize - 1).map { c =>
      createAChain(maxW, maxH, samples, manipChainSize)
    }.toArray

    val finalChains = iterateSteps(initChains, iterations, theBackgroundImage, theImageToAssemble, topNSize, manipChainPopulationSize)

    val topChain = takeTopApplied(getApplied(finalChains, theBackgroundImage, theImageToAssemble), topNSize).map(_._1).head

    val (topChainAgain, topImage, topDistance) = getApplied(Array(topChain), theBackgroundImage, theImageToAssemble).head

    println(s"final distance: $topDistance")

    topImage
  }


  def iterateSteps(initChains: Array[Array[ImageManipulator]], iterations: Int, theBackgroundImage: Image, theImageToAssemble: Image, topNSize: Int, manipChainPopulationSize: Int): Array[Array[ImageManipulator]] = {
    var theChains = initChains
    for (iter <- 0 to iterations - 1) {
      println(s"iteration: $iter")
      theChains = doOneStep(theChains, theBackgroundImage, theImageToAssemble, topNSize, manipChainPopulationSize)
    }
    theChains
  }

  def getApplied(chains: Array[Array[ImageManipulator]], theBackgroundImage: Image, theImageToAssemble: Image): Array[(Array[ImageManipulator], Image, Double)] = {
    chains.map { chain =>
      println("applying chain...")
      val appliedImage = ApplyManipulations(theBackgroundImage, chain)
      val distance = ImageSimilarityArgbDistance2(appliedImage, theImageToAssemble)
      (chain, appliedImage, distance)
    }
  }

  def takeTopApplied(chainsWDistance: Array[(Array[ImageManipulator], Image, Double)], topCount: Int): Array[(Array[ImageManipulator], Image, Double)] = {
    chainsWDistance.sortBy {
      case (chain, appliedImage, dist) =>
        dist
    }.take(topCount)
  }

  def doOneStep(chainsToIterateOn: Array[Array[ImageManipulator]], theBackgroundImage: Image, theImageToAssemble: Image, topNSize: Int, manipChainPopulationSize: Int): Array[Array[ImageManipulator]] = {

    println("getting distances")
    val distances = getApplied(chainsToIterateOn, theBackgroundImage, theImageToAssemble)
    val topChainsWDist = takeTopApplied(distances, topNSize)

    topChainsWDist.foreach{ tc =>
      println(s"chain distance: ${tc._3}")
    }

    val topChains = topChainsWDist.map(_._1)

    val notTopChains = chainsToIterateOn.filter(!topChains.contains(_))

    val newChainsCorpus = topChains.++:(notTopChains).++(hyvbridizeChains(chainsToIterateOn, chainsToIterateOn))

    val newChainsChosenToLiveAndTheRestToDieMuahahahahahahahahahahahAAAAAAhahahhahahahahahahaha =
      topChains.++:(Random.shuffle(newChainsCorpus.toList).take(manipChainPopulationSize - topNSize))

    newChainsChosenToLiveAndTheRestToDieMuahahahahahahahahahahahAAAAAAhahahhahahahahahahaha
  }

  def hyvbridizeChains(
                        chain1: Array[Array[ImageManipulator]],
                        chain2: Array[Array[ImageManipulator]]): Array[Array[ImageManipulator]] = {
    var hybChains = Array[Array[ImageManipulator]]()
    for (c1: Array[ImageManipulator] <- chain1; c2: Array[ImageManipulator] <- chain2) {
      if (!(c1 sameElements c2))
        hybChains = hybChains.++:(Array(MixManipulationsRandomly(c1, c2)))
    }
    hybChains
  }

  def createAChain(maxW: Int, maxH: Int, sampleImgs: Array[Image], size: Int): Array[ImageManipulator] = {
    val manips: Array[ImageManipulator] = (0 to size - 1).map { s =>
      val randomX = scala.util.Random.nextInt(maxW)
      val randomY = scala.util.Random.nextInt(maxH)
      val randomIndex = scala.util.Random.nextInt(sampleImgs.length)
      val randomImg = sampleImgs(randomIndex)
      println(s"creating chain: $randomX, $randomY $randomIndex, randomImg size: ${randomImg.width}, ${randomImg.height}")
      val manip = new AlphaCompositeManipulator(randomImg, randomX, randomY)
      manip
    }.toArray

    manips
  }

}

object SimpleCompleteRandomAssembler extends CompleteAssembler {
  override def apply(theReferenceImage: Image, theBackgroundImage: Image, samples: Array[Image]): Image = {

    var theImage = theBackgroundImage
    var distance = ImageSimilarityArgbDistance2(theReferenceImage, theBackgroundImage)
    val (w, h) = (theImage.width, theImage.height)

    scala.util.Random.setSeed(13)

    for (i <- 0 to 1000) {
      println(s"$i")

      val randomX = scala.util.Random.nextInt(w)
      val randomY = scala.util.Random.nextInt(h)
      val randomImageIndex = scala.util.Random.nextInt(samples.length)

      val imgTest = samples(randomImageIndex)
      val sampledImgSize = (imgTest.width, imgTest.height)
      val newW = Random.nextInt(sampledImgSize._1) + 1
      val newH = Random.nextInt(sampledImgSize._2) + 1

      val scaledImg = imgTest.scaleTo(newW, newH, ScaleMethod.FastScale)

      val halfSeeThroughImg = AddTransparencyToImage(scaledImg)

      val maybeNewImage = SimpleSingleAbsoluteAssembler(theImage, (randomX, randomY), halfSeeThroughImg)

      val maybeNewDistance = ImageSimilarityArgbDistance2(theReferenceImage, maybeNewImage)

      println(s"    old dist: $distance ; new distance: $maybeNewDistance")

      distance - maybeNewDistance match {
        case better if better > 0.0 =>
          theImage = maybeNewImage
          distance = maybeNewDistance
        case worse if worse <= 0.0 =>
      }
    }

    theImage
  }
}

object SimpleCompleteGridAssembler extends CompleteGridAssembler {

  val log = LoggerFactory.getLogger(getClass)

  override def apply(backgroundImage: Image, imagesWIndex: Array[(Image, (Int, Int))], gridSize: (Int, Int)): Image = {

    val (canvasW, canvasH) = (backgroundImage.width, backgroundImage.height)

    log.info("computing pixel locations from grid locations")

    val imagesWPixelLocations = imagesWIndex.map {
      case (i, (colIndex, rowIndex)) =>
        (i, SimplePixelLocationComputer(gridSize, (colIndex, rowIndex), (canvasW, canvasH)))
    }

    log.info("assembling image")

    val theAssembledImage = imagesWPixelLocations.foldLeft(backgroundImage) {
      case (canvasImage, (image, (i1, i2))) =>
        SimpleSingleAbsoluteAssembler(canvasImage, (i1, i2), image)
    }

    theAssembledImage
  }
}

object SimpleSingleAbsoluteAssembler extends SingleAbsoluteAssembler {
  override def apply(backgroundImage: Image, pixelLocation: (Int, Int), theImageToInsert: Image): Image = {
    backgroundImage.overlay(theImageToInsert, pixelLocation._1, pixelLocation._2)
  }
}
