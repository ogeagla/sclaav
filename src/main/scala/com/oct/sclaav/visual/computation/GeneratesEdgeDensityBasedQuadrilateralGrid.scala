package com.oct.sclaav.visual.computation

import com.oct.sclaav.visual.manipulators.SimpleCrop
import com.oct.sclaav.{Argb, QuadrilateralCell, QuadrilateralGrid}
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


    DensitiesToGridQuads(rows, cols, theDistances)
  }

}

object DensitiesToGridQuads {
  val log = LoggerFactory.getLogger(getClass)

  def apply(rows: Int, cols: Int, densities: Array[(Double, (Int, Int))]): QuadrilateralGrid = {


    val sortedDistancesByBrightestFirst = densities.sortBy(d => d._1).reverse

    log.info("top 50 sorted by brightest first:")
    sortedDistancesByBrightestFirst.take(50).foreach {
      case (d, (c, r)) =>
        log.info(s"($c, $r) : $d")
    }

    /*

    1.
       Highest Edge Density

     */
    val percentageForHighestDensity = 0.2
    val sizeForHighestGran = (1, 1)
    val numberOfHighestGranularityCells = (percentageForHighestDensity * (rows * cols)).toInt
    val highestDensities = sortedDistancesByBrightestFirst.take(numberOfHighestGranularityCells)
    val nonHighestDensities = sortedDistancesByBrightestFirst.diff(highestDensities)

    var arrBuff: ArrayBuffer[ArrayBuffer[Boolean]] = ArrayBuffer.fill(cols, rows)(false)
    var cells = Array[QuadrilateralCell]()

    //take each highest density and make those use highest resolution
    highestDensities.foreach { hd =>
      cells = cells.+:(QuadrilateralCell(hd._2._1, hd._2._2, hd._2._1 + sizeForHighestGran._1, hd._2._2  + sizeForHighestGran._2))
    }

    /*

    2.
       Try to fill using multiple sizes somehow

     */

    //these sizes should be proportional to grid size
    val scalarR = rows / 100
    val scalarC = cols / 100
    val nextSizes = List((1,2), (2, 1), (2, 2)).map {
      case (c, r) =>
        (c * scalarC, r * scalarR)
    }

    val midDensityThresh = 0.5 * (1.0 / (1.0 - percentageForHighestDensity))
    val midDensityCount = (midDensityThresh * nonHighestDensities.length).toInt
    val nextCellsWeCareAbout = nonHighestDensities.take(midDensityCount)
    val remaining = nonHighestDensities.diff(nextCellsWeCareAbout)



    def tryToMakeAQuad(ofSize: (Int, Int), truthTable: ArrayBuffer[ArrayBuffer[Boolean]]) = {

    }




    ???
  }

}

