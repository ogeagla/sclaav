package com.oct.sclaav.visual.computation

import com.oct.sclaav.{QuadrilateralCell, Argb, QuadrilateralGrid}
import com.oct.sclaav.visual.manipulators.SimpleCrop
import com.sksamuel.scrimage.filter.{ThresholdFilter, EdgeFilter}
import com.sksamuel.scrimage.{Image, ScaleMethod}
import org.slf4j.LoggerFactory

import scala.collection.parallel.mutable.ParArray

object GeneratesEdgeDensityBasedQuadrilateralGrid {

  val log = LoggerFactory.getLogger(getClass)

  def getImageToCropFromOriginal(img: Image): Image = {
    val imgScaled = img.scale(0.2, ScaleMethod.FastScale)
    val edges = imgScaled.filter(EdgeFilter).filter(ThresholdFilter(200))
    edges
  }

  def apply(img: Image, rows: Int = 20, cols: Int = 20, granularity: Double = 0.1, iterations: Int = 5): QuadrilateralGrid = {

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

    log.info("sorted by brightest first:")
    theDistances.sortBy(d => d._1).reverse.foreach {
      case (d, (c, r)) =>
        log.info(s"($c, $r) : $d")
    }


    new QuadrilateralGrid(0, 0, Array(new QuadrilateralCell(0,0,0,0)))
  }


}

