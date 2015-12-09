package com.oct.sclaav.visual.computation

import com.oct.sclaav.visual.computation.CellIntersectsExisting.{FillQuadWithSingles, ApplyCellToTruthTable}
import com.oct.sclaav.visual.manipulators.SimpleCrop
import com.oct.sclaav.{ImageToQuadGridThing, Argb, QuadrilateralCell, QuadrilateralGrid}
import com.sksamuel.scrimage.filter.{EdgeFilter, ThresholdFilter}
import com.sksamuel.scrimage.{Image, ScaleMethod}
import org.slf4j.LoggerFactory

import scala.collection.mutable.ArrayBuffer
import scala.collection.parallel.mutable.ParArray

class GeneratesEdgeDensityBasedQuadrilateralGrid extends ImageToQuadGridThing {
  def apply(img: Image, rows: Int, cols: Int): QuadrilateralGrid = {
    GeneratesEdgeDensityBasedQuadrilateralGrid(img, rows, cols)
  }
}

object GeneratesEdgeDensityBasedQuadrilateralGrid extends ImageToQuadGridThing {

  val log = LoggerFactory.getLogger(getClass)

  def getImageToCropFromOriginal(img: Image): Image = {
    val imgScaled = img.scale(0.2, ScaleMethod.FastScale)
    val edges = imgScaled.filter(EdgeFilter).filter(ThresholdFilter(180))
    edges
  }

  def apply(img: Image, rows: Int = 100, cols: Int = 100): QuadrilateralGrid = {

    println(s"running with cols: $cols rows: $rows")

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

    val levels = 3
    val sizesForLevels = (0 to levels - 1).map{l => l -> (2 * l + 1)}.toMap

    println("sizes for levels:")
    sizesForLevels.foreach{
      case (k, v) => println(s"$k -> $v")
    }

    val asArray = ArrayBuffer.fill(cols, rows)(-1.0)

    theDistances.foreach{
      case (d, (c, r)) =>
        asArray(c).update(r, d)
    }

    val distArr = asArray.map(_.toArray).toArray

    val levelsArr = ValuesToLevels(distArr, levels)

    val levelsArrWSizes = levelsArr.map(v => v.map {
      e => (e, sizesForLevels(e))
    })

    val forPrinting = levelsArrWSizes.flatten.sortBy(_._1)

    forPrinting.take(10).foreach{ case (l, s) => println(s"level $l has size $s")}
    forPrinting.reverse.take(10).foreach{ case (l, s) => println(s"level $l has size $s")}

    val quadCells = LevelsWithSquareSizesToCells(levelsArrWSizes, cols, rows)

//    DensitiesToGridQuads(rows, cols, theDistances)
    new QuadrilateralGrid(rows, cols, quadCells)
  }

}

object LevelsWithSquareSizesToCells {
  def apply(levelsWithSizes: Array[Array[(Int, Int)]], cols: Int, rows: Int): Array[QuadrilateralCell] = {

    var arr: List[((Int, Int), (Int, Int))] = List[((Int, Int), (Int, Int))]()
    var arrBuff = ArrayBuffer.fill(cols, rows)(false)


    for (c <- levelsWithSizes.indices) {
      for (r <- levelsWithSizes(c).indices) {

        val tup: ((Int, Int), (Int, Int)) = ((c, r), levelsWithSizes(c)(r))
        arr = arr.+:(tup)
      }
    }

    val sortedByLevel = arr.sortBy {
      case ((c, r), (level, size)) => level
    }

    val biggestLevelsFirst = sortedByLevel.reverse

    var cells = Array[QuadrilateralCell]()
    for (l <- biggestLevelsFirst.indices) {

      val ((c, r), (level, size)) = biggestLevelsFirst(l)
      println(s"Doing level index ${l} c $c r $r level $level size $size")

      val totalCells = size * size

      var notThisLevel = 0

      val endCol = c + size match {
        case theOne if theOne < cols => theOne
        case _ =>
          println("attempted to size an image out of bounds (cols)")
          cols - 1
      }

      val endRow = r + size match {
        case theOne if theOne < rows => theOne
        case _ =>
          println("attempted to size an image out of bounds (rows)")
          rows - 1
      }

      for (_c <- c to endCol - 1) {
        for (_r <- r to endRow - 1) {
          if (levelsWithSizes(_c)(_r)._1 != level) {
            println(s"not the same level w ${_c}, ${_r} values: ${levelsWithSizes(_c)(_r)}")
            notThisLevel = notThisLevel + 1
          }
        }
      }

      println(s"$notThisLevel / ${totalCells}")
      notThisLevel.toDouble / totalCells.toDouble match {
        case tooMany if tooMany >= 0.25 =>
          println(s"too many intersected ${tooMany}")
        case justRight =>
          println(s"just right intersected ${justRight}")

          val tryCell = new QuadrilateralCell(c, r, endCol, endRow)
          if ( ! CellIntersectsExisting(arrBuff, tryCell)) {
            arrBuff = ApplyCellToTruthTable(arrBuff, tryCell)
            cells = cells.+:(tryCell)
          }
      }
    }

    cells = cells.++:(FillQuadWithSingles(arrBuff))

    cells
  }
}

object ValuesToLevels {
  def apply(values: Array[Array[Double]], levels: Int = 3): Array[Array[Int]] = {

    println(s"total elements: ${values.map(_.length).sum}")

    val max: Double = values.map(v => v.max).max
    val min = values.map(v => v.min).min

    val delta = (max - min) / levels.toDouble

    println(s"min: $min max: $max delta: $delta")

    val steps = (0 to levels - 1).map { l =>
      (min + l * delta, min + (l + 1) * delta)
    }.toArray.reverse

    def whichStep(steps: Array[(Double, Double)], value: Double): Int = {

      val theStep = steps.indices.flatMap { i =>
        value match {
          case s if s >= steps(i)._1 && s <= steps(i)._2 =>
            Seq(i)
          case _ =>
            Seq()
        }
      }.head

      theStep
    }

    values.map(v => v.map(e => whichStep(steps, e)))
  }
}

//object DensitiesToGridQuads {
//  val log = LoggerFactory.getLogger(getClass)
//
//  def shapesAndScalarsToActualSizes(shapes: List[(Int, Int)], scalarC: Int, scalarR: Int) = {
//    shapes.map{case (c, r) => (scalarC*c, scalarR*r)}
//  }
//
//  def apply(rows: Int, cols: Int, densities: Array[(Double, (Int, Int))]): QuadrilateralGrid = {
//
//
//    val sortedDistancesByBrightestFirst = densities.sortBy(d => d._1).reverse
//
//    log.info("top 50 sorted by brightest first:")
//    sortedDistancesByBrightestFirst.take(50).foreach {
//      case (d, (c, r)) =>
//        log.info(s"($c, $r) : $d")
//    }
//
//    /*
//
//    1.
//       Highest Edge Density
//
//     */
//    val percentageForHighestDensity = 0.2
//    val sizeTup = (1, 1)
//    val sizeForHighestGran =  shapesAndScalarsToActualSizes(List(sizeTup), 1, 1)
//    val numberOfHighestGranularityCells = (percentageForHighestDensity * (rows * cols)).toInt
//    val highestDensities = sortedDistancesByBrightestFirst.take(numberOfHighestGranularityCells)
//    val nonHighestDensities = sortedDistancesByBrightestFirst.diff(highestDensities)
//
//    var arrBuff: ArrayBuffer[ArrayBuffer[Boolean]] = ArrayBuffer.fill(cols, rows)(false)
//    var cells = Array[QuadrilateralCell]()
//
//    //take each highest density and make those use highest resolution
//    highestDensities.foreach {
//      case (d, (c, r)) =>
//        val daCell = QuadrilateralCell(c, r, c + sizeTup._1, r  + sizeTup._2)
//        if (! CellIntersectsExisting(arrBuff, daCell)) {
//          arrBuff = ApplyCellToTruthTable(arrBuff, daCell)
//          cells = cells.+:(daCell)
//        }
//    }
//
//    /*
//
//    2.
//       Try to fill using multiple sizes somehow
//
//     */
//
//    //these sizes should be proportional to grid size
//    val scalarR = rows / 100
//    val scalarC = cols / 100
//    val nextSizes = shapesAndScalarsToActualSizes(List((1,2), (2, 1), (2, 2)), scalarC, scalarR)
//
//    val midDensityThresh = 0.5 * (1.0 / (1.0 - percentageForHighestDensity))
//    val midDensityCount = (midDensityThresh * nonHighestDensities.length).toInt
//    val nextCellsWeCareAbout = nonHighestDensities.take(midDensityCount)
//    val remaining = nonHighestDensities.diff(nextCellsWeCareAbout)
//
//    nextCellsWeCareAbout.map {
//      case (d, (c, r)) =>
//
//    }
//
//
//    def tryToMakeAQuad(ofSize: (Int, Int), truthTable: ArrayBuffer[ArrayBuffer[Boolean]]) = {
//
//      ???
//    }
//
//    def tryToCreateQuadOfGivenSizesForATruthTable(
//                                                   sizes: Array[(Int, Int)],
//                                                   whereToTry: Array[(Int, Int)],
//                                                   truthTable: ArrayBuffer[ArrayBuffer[Boolean]]):
//    (Array[QuadrilateralCell], ArrayBuffer[ArrayBuffer[Boolean]]) = {
//
//      val possiblesForSizes: Map[(Int, Int), (Int, Int)] = sizes.flatMap {
//        case (colsWidth, rowsHeight) =>
//
//          //TODO this piece:
//          val topLefts = Array((1,2), (2, 3))
//
//
//          topLefts.map(tl => (colsWidth, rowsHeight) -> tl)
//      }.toMap
//
//
//
//
//      ???
//    }
//
//
//
//
//    ???
//  }
//
//}

