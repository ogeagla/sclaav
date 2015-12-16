package com.oct.sclaav.visual.search

import java.io.File

import com.oct.sclaav.visual.computation.{SimpleArgbDistance, SimpleArgbEstimator}
import com.oct.sclaav.{ArgbDistance, ArgbEstimator}
import com.sksamuel.scrimage.{ScaleMethod, Image}

object MatchByArgbAverage {
  def apply(refImage: Image, otherImages: Array[Image], argbEstimator: ArgbEstimator = SimpleArgbEstimator, argbDistance: ArgbDistance = SimpleArgbDistance): Image = {

    val refArgb = argbEstimator(refImage)

    val argbs = otherImages.map {
      i => (i, argbEstimator(i))
    }

    val argbsWDistance = argbs.map {
      case (i, argb) => (i, argbDistance(refArgb, argb))
    }

    argbsWDistance.sortBy {
      case (i, dist) => dist
    }.head._1
  }
}

object MatchesByArgbAverageThresh {
  def apply(refFile: File, otherImages: Array[File], threshold: Double = 0.85, scaleTo: Double = 0.5, argbEstimator: ArgbEstimator = SimpleArgbEstimator, argbDistance: ArgbDistance = SimpleArgbDistance): Array[(File, Image)] = {

    val refImage = Image.fromFile(refFile)

    val (targetImgW, targetImgH) = (refImage.width, refImage.height)

    val (scTargetImgW, scTargetImgH) = ((targetImgW*scaleTo).toInt, (targetImgH*scaleTo).toInt)


    val scaledTargetImg = refImage.scaleTo(scTargetImgW, scTargetImgH, ScaleMethod.FastScale)
    val scaledSampleFiles = otherImages.map(f => (f, Image.fromFile(f).scaleTo(scTargetImgW, scTargetImgH)))

    val refArgb = argbEstimator(scaledTargetImg)

    val argbs = scaledSampleFiles.map {
      case (f, i) =>
        (f, i, argbEstimator(i))
    }

    val argbsWDistance = argbs.map {
      case (f, i, argb) => (f, i, argbDistance(refArgb, argb))
    }

    val smallestDistancesFirst = argbsWDistance.sortBy {
      case (f, i, dist) => dist
    }

    val smallestDist = smallestDistancesFirst.head._3
    val distThresh = smallestDist / threshold
    val imgsBelowCutoff = smallestDistancesFirst.filter {
      case (f, i, dist) =>
        dist <= distThresh
    }
    imgsBelowCutoff.map(t => (t._1, t._2))
  }
}
