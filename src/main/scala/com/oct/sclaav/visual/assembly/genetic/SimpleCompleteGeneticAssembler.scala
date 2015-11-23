package com.oct.sclaav.visual.assembly.genetic

import com.oct.sclaav.visual.computation.{ComputesMeanAndStddev, Distance2, ImageSimilarityArgbDistance2, ImageSimilarityRgbDistance2}
import com.oct.sclaav.visual.manipulators._
import com.oct.sclaav.{CompleteAssembler, ImageManipulator, IterationStats}
import com.sksamuel.scrimage.{Image, ScaleMethod}
import org.slf4j.LoggerFactory

import scala.util.Random


object SimpleCompleteGeneticAssembler {
  def apply(theImageToAssemble: Image, theBackgroundImage: Image, samples: Array[Image]): Image = {
    (new SimpleCompleteGeneticAssembler)(theImageToAssemble, theBackgroundImage, samples)
  }
}

class SimpleCompleteGeneticAssembler(
                                      initChainSizeMax: Int = 5,
                                      chainsInPopulation: Int = 200,
                                      iterations: Int = 10,
                                      topToTakePercent: Double = 0.2,
                                      splitChainOnSize: Option[Int] = Some(1000),
                                      chainSizeAttenuation: Option[Double] = Some(0.02)) extends CompleteAssembler {
  val log = LoggerFactory.getLogger(getClass)

  override def apply(theImageToAssemble: Image, theBackgroundImage: Image, samples: Array[Image]): Image = {

    log.info(s"GA assembler. chain size max: $initChainSizeMax, chain population $chainsInPopulation, iterations: $iterations, topToTake: $topToTakePercent")

    val (maxW, maxH) = (theBackgroundImage.width, theBackgroundImage.height)
    val refDistance = ImageSimilarityRgbDistance2(theBackgroundImage, theImageToAssemble)

    scala.util.Random.setSeed(13)

    val initChains = (0 to chainsInPopulation - 1).map { c =>
      createAChain(maxW, maxH, samples, initChainSizeMax)
    }.toArray

    val (finalChains, finalStats) = iterateSteps(initChains, iterations, theBackgroundImage, theImageToAssemble, topToTakePercent, chainsInPopulation, splitChainOnSize, chainSizeAttenuation)

    val topChain = takeTopApplied(getApplied(finalChains, theBackgroundImage, theImageToAssemble), topToTakePercent).map(_._1).head

    val (topChainAgain, topImage, topDistance) = getApplied(Array(topChain), theBackgroundImage, theImageToAssemble).head

    log.info(s"final distance: $topDistance")

    log.info(s"iteration,chain size mean,chain size stddev,pop fit,pop dist mean,pop dist stddev,best dist,worst dist:")
    (0 to iterations - 1).foreach{ i =>
      log.info(s"$i,${finalStats.chainSizeMeans(i)},${finalStats.chainSizeStddevs(i)},${finalStats.populationFitness(i)},${finalStats.populationDistanceMeans(i)},${finalStats.populationDistanceStddevs(i)},${finalStats.bestDistances(i)},${finalStats.worstDistances(i)}")
    }

    topImage
  }


  def iterateSteps(initChains: Array[Array[ImageManipulator]], iterations: Int, theBackgroundImage: Image, theImageToAssemble: Image, topPercent: Double, manipChainPopulationSize: Int, splitChainOnSize: Option[Int] = None, chainSizeAttenuation: Option[Double]): (Array[Array[ImageManipulator]], IterationStats) = {
    var theChains = initChains
    var iterStats = IterationStats()
    for (iter <- 0 to iterations - 1) {
      log.info(s"iteration: $iter, chain size: ${theChains.length}")
      val results = doOneStep(theChains, theBackgroundImage, theImageToAssemble, topPercent, manipChainPopulationSize, iterStats, splitChainOnSize, chainSizeAttenuation)
      theChains = results._1
      iterStats = results._2
    }
    (theChains, iterStats)
  }

  def getApplied(chains: Array[Array[ImageManipulator]], theBackgroundImage: Image, theImageToAssemble: Image): Array[(Array[ImageManipulator], Image, Double)] = {
    chains.par.map { chain =>
      val appliedImage = ApplyManipulations(theBackgroundImage, chain)
      val distance = ImageSimilarityArgbDistance2(appliedImage, theImageToAssemble)
      (chain, appliedImage, distance)
    }.toArray
  }

  def takeTopApplied(chainsWDistance: Array[(Array[ImageManipulator], Image, Double)], topPercent: Double): Array[(Array[ImageManipulator], Image, Double)] = {
    val takeCount = (topPercent * chainsWDistance.length).toInt
    val theTake = if (takeCount <= 0) 1 else takeCount
    chainsWDistance.sortBy {
      case (chain, appliedImage, dist) =>
        dist
    }.take(theTake)
  }

  def takeBottom(chainsWDistance: Array[(Array[ImageManipulator], Image, Double)], topCount: Int = 1): Array[(Array[ImageManipulator], Image, Double)] = {
    chainsWDistance.sortBy {
      case (chain, appliedImage, dist) =>
        -1 * dist
    }.take(topCount)
  }

  def doOneStep(chainsToIterateOn: Array[Array[ImageManipulator]], theBackgroundImage: Image, theImageToAssemble: Image, topPercent: Double, manipChainPopulationSize: Int, stats: IterationStats, splitChainOnSize: Option[Int] = None, chainSizeAttenuation: Option[Double]): (Array[Array[ImageManipulator]], IterationStats) = {

    val manipsCount = chainsToIterateOn.map(_.length).sum
    log.info(s"applying chains + getting distances for total manips: $manipsCount")
    val distances: Array[(Array[ImageManipulator], Image, Double)] = getApplied(chainsToIterateOn, theBackgroundImage, theImageToAssemble)

    val chainSizes = distances.map(_._1.length.toDouble)
    val justDistances = distances.map(_._3)
    val populationFitness = Distance2(justDistances, distances.map(t => 0.0))
    val (distMean, distStddev) = ComputesMeanAndStddev(justDistances)
    val (chainSizeMean, chainSizeStddev) = ComputesMeanAndStddev(chainSizes)
    log.info(s"population fitness: $populationFitness")
    log.info(s"population dist mean, stddev: $distMean +/- $distStddev")
    log.info(s"population chain size mean, stddev: $chainSizeMean +/- $chainSizeStddev")

    val topChainsWDist = takeTopApplied(distances, topPercent)
    val bottomOne = takeBottom(distances)

    val newStats = stats.copy(
      chainSizeMeans = Array(chainSizeMean).++:(stats.chainSizeMeans),
      chainSizeStddevs = Array(chainSizeStddev).++:(stats.chainSizeStddevs),
      populationFitness = Array(populationFitness).++:(stats.populationFitness),
      populationDistanceMeans = Array(distMean).++:(stats.populationDistanceMeans),
      populationDistanceStddevs = Array(distStddev).++:(stats.populationDistanceStddevs),
      bestDistances = Array(topChainsWDist(0)._3).++:(stats.bestDistances),
      worstDistances = Array(bottomOne(0)._3).++:(stats.worstDistances)
    )

    topChainsWDist.foreach{ tc =>
      log.info(s"top chain distance: ${tc._3}, size: ${tc._1.length}")
    }

    val topChains = topChainsWDist.map(_._1)

    val notTopChains = chainsToIterateOn.filter(!topChains.contains(_))

    val chainsToIterateOnAndMaybeSplit: Array[Array[ImageManipulator]] = splitChainOnSize match {
      //TODO when do we want to split?  before computing fitness, after hybridization, etc?
      case Some(sizeToSplitOn) =>
        log.info("splitting chains")
        doChainSplit(chainsToIterateOn, sizeToSplitOn)
      case None => chainsToIterateOn
    }


    val funcsThatRunOnAllChains: Array[(Array[Array[ImageManipulator]], Array[Array[ImageManipulator]]) => Array[Array[ImageManipulator]]] = Array(hybrdizeChainsCombine(_, _), hybridizeChainsPointWise(_, _), hybridizeChainsBySplit(_, _))

    val funcsThatRunOnSingleChain = Array(ModManipulationsRandomlyRemove(_), ModManipulationsRandomlySplit(_))

    val newChainsFromSingleOps = funcsThatRunOnSingleChain.par.flatMap { f =>
      chainsToIterateOnAndMaybeSplit.map(f)
    }.toArray

    val newChainsFromBulkOps: Array[Array[ImageManipulator]] = funcsThatRunOnAllChains.par.flatMap { f =>
      f(chainsToIterateOnAndMaybeSplit, chainsToIterateOnAndMaybeSplit)
    }.toArray

    val newChainsCorpus = topChains
      .++:(notTopChains)
      .++(newChainsFromBulkOps)
      .++(newChainsFromSingleOps)

    val newChainsChosenToLiveAndTheRestToDieMuahaha =
      topChains
        .++:(Random.shuffle(newChainsCorpus.toList)
        .take(
          chainsToIterateOn.length -
          (topPercent*chainsToIterateOn.length).toInt -
          (manipChainPopulationSize * chainSizeAttenuation.getOrElse(0.0)).toInt))

    (newChainsChosenToLiveAndTheRestToDieMuahaha, newStats)
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
    log.info(s"hybridizing chain by combining, of sizes: ${chain1.length}, ${chain2.length}")
    var hybChains = Array[Array[ImageManipulator]]()
    for (c1: Array[ImageManipulator] <- chain1; c2: Array[ImageManipulator] <- chain2) {
      hybChains = hybChains.+:(MixManipulationsCombinator(c1, c2))
    }
    hybChains
  }

  def hybridizeChainsPointWise(chain1: Array[Array[ImageManipulator]],
                               chain2: Array[Array[ImageManipulator]]): Array[Array[ImageManipulator]] = {
    log.info(s"hybridizing chain by combining point-wise, of sizes: ${chain1.length}, ${chain2.length}")
    var hybChains = Array[Array[ImageManipulator]]()
    for (c1: Array[ImageManipulator] <- chain1; c2: Array[ImageManipulator] <- chain2) {
      hybChains = hybChains.+:(MixManipulationsRandomlyPointwise(c1, c2))
    }
    hybChains
  }

  def hybridizeChainsBySplit(
                              chain1: Array[Array[ImageManipulator]],
                              chain2: Array[Array[ImageManipulator]]): Array[Array[ImageManipulator]] = {

    log.info(s"hybridizing chain by split, of sizes: ${chain1.length}, ${chain2.length}")
    var hybChains = Array[Array[ImageManipulator]]()
    for (c1: Array[ImageManipulator] <- chain1; c2: Array[ImageManipulator] <- chain2) {
      if (!(c1 sameElements c2))
        hybChains = hybChains.+:(MixManipulationsRandomlyBy2SegmentSwap(c1, c2))
    }
    hybChains
  }

  //FIXME this is a computer
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

  //FIXME this is a computer
  def uniformScaleFactor(cap: Double = 2.0): Double = {
    Random.nextDouble() * cap
  }

  def verifySizeNotZero(w: Int, h: Int, default: (Int, Int) = (20, 20)): (Int, Int) = {
    val goodW = w match {
      case badW if badW == 0 => default._1
      case gW => gW
    }

    val goodH = h match {
      case badH if badH == 0 => default._2
      case gH => gH
    }

    (goodW, goodH )
  }

  def createAChain(maxW: Int, maxH: Int, sampleImgs: Array[Image], maxSize: Int): Array[ImageManipulator] = {

    val size = scala.util.Random.nextInt(maxSize)

    val manips: Array[ImageManipulator] = (0 to size - 1).par.map { s =>
      val randomX = scala.util.Random.nextInt(maxW)
      val randomY = scala.util.Random.nextInt(maxH)
      val randomIndex = scala.util.Random.nextInt(sampleImgs.length)
      val randomImg = sampleImgs(randomIndex)

      val scaleFactor = uniformScaleFactor()

      val (newWidth, newHeight) = verifySizeNotZero((scaleFactor * randomImg.width.toDouble).toInt, (scaleFactor * randomImg.height.toDouble).toInt)

      val manip = new AlphaCompositeManipulator(randomImg.scaleTo(newWidth, newHeight, ScaleMethod.FastScale), randomX, randomY)
      manip
    }.toArray

    manips
  }

}