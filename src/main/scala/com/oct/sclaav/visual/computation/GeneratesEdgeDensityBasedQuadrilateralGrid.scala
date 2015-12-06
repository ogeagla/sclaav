package com.oct.sclaav.visual.computation

import com.oct.sclaav.visual.manipulators.SimpleCrop
import com.oct.sclaav.{QuadrilateralCell, Argb, QuadrilateralGrid}
import com.sksamuel.scrimage.filter.{EdgeFilter, ThresholdFilter}
import com.sksamuel.scrimage.{Image, ScaleMethod}
import org.slf4j.LoggerFactory

import scala.collection.mutable.ArrayBuffer
import scala.collection.parallel.mutable.ParArray

object GeneratesEdgeDensityBasedQuadrilateralGrid {

  val log = LoggerFactory.getLogger(getClass)

  def getImageToCropFromOriginal(img: Image): Image = {
    val imgScaled = img.scale(0.2, ScaleMethod.FastScale)
    val edges = imgScaled.filter(EdgeFilter).filter(ThresholdFilter(180))
    edges
  }

  def apply(img: Image, rows: Int = 50, cols: Int = 50, granularity: Double = 0.1, iterations: Int = 5): QuadrilateralGrid = {

    val edgesImg = getImageToCropFromOriginal(img)

    val listBuffer = new ParArray[(Double, (Int, Int))](rows * cols)
    val blackArgb = new Argb(0, 0, 0, 0)

    for (c <- (0 to cols - 1).par) {
      log.info(s"cropping edge-filtered image ${c*rows} of ${cols*rows}")
      for(r <- 0 to rows - 1) {
        val cropped = SimpleCrop((cols, rows), (c, r), edgesImg)

        val avgArgbOfCropped = SimpleArgbEstimator(cropped)

        val dist = SimpleArgbDistance(blackArgb, avgArgbOfCropped)

        listBuffer.update(rows*c + r, (dist, (c, r)))
      }
    }

    val theDistances = listBuffer.toArray

    val sortedDistancesByBrightestFirst = theDistances.sortBy(d => d._1).reverse

    log.info("top 50 sorted by brightest first:")
    sortedDistancesByBrightestFirst.take(50).foreach {
      case (d, (c, r)) =>
        log.info(s"($c, $r) : $d")
    }

    DensitiesToGridQuads(rows, cols, sortedDistancesByBrightestFirst)
  }

}

object DensitiesToGridQuads {
  def apply(rows: Int, cols: Int, densities: Array[(Double, (Int, Int))]): QuadrilateralGrid = {

    val percentageForHighestDensity = 0.2

    val numberOfHighestGranularityCells = (percentageForHighestDensity * (rows * cols)).toInt


    var arrBuff = ArrayBuffer.fill(cols, rows)(false)
    var cells = Array[QuadrilateralCell]()





    ???
  }
}

