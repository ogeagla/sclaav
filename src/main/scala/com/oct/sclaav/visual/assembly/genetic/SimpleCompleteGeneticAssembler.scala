package com.oct.sclaav.visual.assembly.genetic

import com.oct.sclaav.visual.computation.{ComputesMeanAndStddev, Distance2, ImageSimilarityArgbDistance2, ImageSimilarityRgbDistance2}
import com.oct.sclaav.visual.manipulators._
import com.oct.sclaav.{CompleteAssembler, ImageManipulator}
import com.sksamuel.scrimage.{Image, ScaleMethod}

import scala.util.Random


object SimpleCompleteGeneticAssembler {
  def apply(theImageToAssemble: Image, theBackgroundImage: Image, samples: Array[Image]): Image = {
    (new SimpleCompleteGeneticAssembler)(theImageToAssemble, theBackgroundImage, samples)
  }
}

class SimpleCompleteGeneticAssembler(
                                      initChainSizeMax: Int = 5,
                                      chainsInPopulation: Int = 1000,
                                      iterations: Int = 10,
                                      topToTake: Int = 10,
                                      splitChainOnSize: Option[Int] = Some(15000)) extends CompleteAssembler {
  override def apply(theImageToAssemble: Image, theBackgroundImage: Image, samples: Array[Image]): Image = {

    println(s"GA assembler. chain size max: $initChainSizeMax, chain population $chainsInPopulation, iterations: $iterations, topToTake: $topToTake")

    val (maxW, maxH) = (theBackgroundImage.width, theBackgroundImage.height)
    val refDistance = ImageSimilarityRgbDistance2(theBackgroundImage, theImageToAssemble)

    scala.util.Random.setSeed(13)

    val initChains = (0 to chainsInPopulation - 1).map { c =>
      createAChain(maxW, maxH, samples, initChainSizeMax)
    }.toArray

    val finalChains = iterateSteps(initChains, iterations, theBackgroundImage, theImageToAssemble, topToTake, chainsInPopulation, splitChainOnSize)

    val topChain = takeTopApplied(getApplied(finalChains, theBackgroundImage, theImageToAssemble), topToTake).map(_._1).head

    val (topChainAgain, topImage, topDistance) = getApplied(Array(topChain), theBackgroundImage, theImageToAssemble).head

    println(s"final distance: $topDistance")

    topImage
  }


  def iterateSteps(initChains: Array[Array[ImageManipulator]], iterations: Int, theBackgroundImage: Image, theImageToAssemble: Image, topNSize: Int, manipChainPopulationSize: Int, splitChainOnSize: Option[Int] = None): Array[Array[ImageManipulator]] = {
    var theChains = initChains
    for (iter <- 0 to iterations - 1) {
      println(s"iteration: $iter, chain size: ${theChains.length}")
      theChains = doOneStep(theChains, theBackgroundImage, theImageToAssemble, topNSize, manipChainPopulationSize, splitChainOnSize)
    }
    theChains
  }

  def getApplied(chains: Array[Array[ImageManipulator]], theBackgroundImage: Image, theImageToAssemble: Image): Array[(Array[ImageManipulator], Image, Double)] = {
    chains.par.map { chain =>
      val appliedImage = ApplyManipulations(theBackgroundImage, chain)
      val distance = ImageSimilarityArgbDistance2(appliedImage, theImageToAssemble)
      (chain, appliedImage, distance)
    }.toArray
  }

  def takeTopApplied(chainsWDistance: Array[(Array[ImageManipulator], Image, Double)], topCount: Int): Array[(Array[ImageManipulator], Image, Double)] = {
    chainsWDistance.sortBy {
      case (chain, appliedImage, dist) =>
        dist
    }.take(topCount)
  }

  def doOneStep(chainsToIterateOn: Array[Array[ImageManipulator]], theBackgroundImage: Image, theImageToAssemble: Image, topNSize: Int, manipChainPopulationSize: Int, splitChainOnSize: Option[Int] = None): Array[Array[ImageManipulator]] = {

    println("applying chains + getting distances")
    val distances: Array[(Array[ImageManipulator], Image, Double)] = getApplied(chainsToIterateOn, theBackgroundImage, theImageToAssemble)

    val chainSizes = distances.map(_._1.length.toDouble)
    val justDistances = distances.map(_._3)
    val populationFitness = Distance2(justDistances, distances.map(t => 0.0))
    val (distMean, distStddev) = ComputesMeanAndStddev(justDistances)
    val (chainSizeMean, chainSizeStddev) = ComputesMeanAndStddev(chainSizes)
    println(s"population fitness: $populationFitness")
    println(s"population dist mean, stddev: $distMean +/- $distStddev")
    println(s"population chain size mean, stddev: $chainSizeMean +/- $chainSizeStddev")

    val topChainsWDist = takeTopApplied(distances, topNSize)

    topChainsWDist.foreach{ tc =>
      println(s"top chain distance: ${tc._3}, size: ${tc._1.length}")
    }

    val topChains = topChainsWDist.map(_._1)

    val notTopChains = chainsToIterateOn.filter(!topChains.contains(_))

    val chainsToIterateOnAndMaybeSplit: Array[Array[ImageManipulator]] = splitChainOnSize match {
      //TODO when do we want to split?  before computing fitness, after hybridization, etc?
      case Some(sizeToSplitOn) =>
        println("splitting chains")
        doChainSplit(chainsToIterateOn, sizeToSplitOn)
      case None => chainsToIterateOn
    }

    val newChainsCorpus = topChains
      .++:(notTopChains)
      .++(hybridizeChainsBySplit(chainsToIterateOnAndMaybeSplit, chainsToIterateOnAndMaybeSplit))
      .++(hybridizeChainsPointWise(chainsToIterateOnAndMaybeSplit, chainsToIterateOnAndMaybeSplit))
      .++(hybrdizeChainsCombine(chainsToIterateOnAndMaybeSplit, chainsToIterateOnAndMaybeSplit))
      .++(chainsToIterateOnAndMaybeSplit.map(c => ModManipulationsRandomlyRemove(c)))
      .++(chainsToIterateOnAndMaybeSplit.map(c => ModManipulationsRandomlySplit(c)))

    val newChainsChosenToLiveAndTheRestToDieMuahaha =
      topChains.++:(Random.shuffle(newChainsCorpus.toList).take(chainsToIterateOn.length - topNSize - (manipChainPopulationSize / iterations)))

    newChainsChosenToLiveAndTheRestToDieMuahaha
  }

  def doChainSplit(chains: Array[Array[ImageManipulator]], size: Int): Array[Array[ImageManipulator]] = {
    //FIXME: below, you will see an IntelliJ warning to refactor to flatMap,
    //but if you do so, the 2.11.7 compiler will yell at you and fail
    chains.par.map { chain =>

      //      if (chain.length > size) {
      //        Array(chain.slice(0, size), chain.slice(size, chain.length))
      //      } else {
      //        Array(chain)
      //      }

      chain.length > size match {
        case true => chain.grouped(size).toArray.++:(Array(chain))
        case false => Array(chain)
      }
    }.flatten.toArray

  }

  def hybrdizeChainsCombine(chain1: Array[Array[ImageManipulator]],
                            chain2: Array[Array[ImageManipulator]]): Array[Array[ImageManipulator]] = {
    var hybChains = Array[Array[ImageManipulator]]()
    for (c1: Array[ImageManipulator] <- chain1; c2: Array[ImageManipulator] <- chain2) {
      hybChains = hybChains.+:(MixManipulationsCombinator(c1, c2))
    }
    hybChains
  }

  def hybridizeChainsPointWise(chain1: Array[Array[ImageManipulator]],
                               chain2: Array[Array[ImageManipulator]]): Array[Array[ImageManipulator]] = {
    var hybChains = Array[Array[ImageManipulator]]()
    for (c1: Array[ImageManipulator] <- chain1; c2: Array[ImageManipulator] <- chain2) {
      hybChains = hybChains.+:(MixManipulationsRandomlyPointwise(c1, c2))
    }
    hybChains
  }

  def hybridizeChainsBySplit(
                              chain1: Array[Array[ImageManipulator]],
                              chain2: Array[Array[ImageManipulator]]): Array[Array[ImageManipulator]] = {

    var hybChains = Array[Array[ImageManipulator]]()
    for (c1: Array[ImageManipulator] <- chain1; c2: Array[ImageManipulator] <- chain2) {
      if (!(c1 sameElements c2))
        hybChains = hybChains.+:(MixManipulationsRandomlyBy2SegmentSwap(c1, c2))
    }
    hybChains
  }

  def gaussianScaleFactor(cap: Double = 3.0): Double = {

    val gaussian = Random.nextGaussian()
    val scaleFactor = gaussian match {
      case g if g <= -cap => 1.0 / cap
      case g if g == 0.0 => 1.0
      case g if g < 0.0 => 1.0 / ((-g) + 0.1)
      case g if g >= cap => cap
      case g => g + 0.1
    }
    scaleFactor
  }

  def createAChain(maxW: Int, maxH: Int, sampleImgs: Array[Image], maxSize: Int): Array[ImageManipulator] = {

    val size = scala.util.Random.nextInt(maxSize)

    val manips: Array[ImageManipulator] = (0 to size - 1).par.map { s =>
      val randomX = scala.util.Random.nextInt(maxW)
      val randomY = scala.util.Random.nextInt(maxH)
      val randomIndex = scala.util.Random.nextInt(sampleImgs.length)
      val randomImg = sampleImgs(randomIndex)

      val scaleFactor = gaussianScaleFactor()

      val newWidth = (scaleFactor * randomImg.width.toDouble).toInt
      val newHeight = (scaleFactor * randomImg.height.toDouble).toInt

      val manip = new AlphaCompositeManipulator(randomImg.scaleTo(newWidth, newHeight, ScaleMethod.FastScale), randomX, randomY)
      manip
    }.toArray

    manips
  }

}