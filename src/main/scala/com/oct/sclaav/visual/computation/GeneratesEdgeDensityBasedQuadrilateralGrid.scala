package com.oct.sclaav.visual.computation

import com.oct.sclaav._
import com.oct.sclaav.visual.computation.CellIntersectsExisting.{ApplyCellToTruthTable, FillQuadWithSingles}
import com.oct.sclaav.visual.manipulators.SimpleCrop
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

object GeneratesEdgeDensityBasedQuadrilateralGrid{

  val log = LoggerFactory.getLogger(getClass)

  def getImageToCropFromOriginal(img: Image): Image = {
    val imgScaled = img.scale(0.2, ScaleMethod.FastScale)
    val edges = imgScaled.filter(EdgeFilter).filter(ThresholdFilter(180))
    edges
  }

  def apply(img: Image, rows: Int = 20, cols: Int = 20, argbEstimator: ArgbEstimator = SimpleArgbEstimator, argbDistance: ArgbDistance = SimpleArgbDistance, stepMaker: StepMaker = PlateauToMaxStepMaker): QuadrilateralGrid = {

    println(s"running with cols: $cols rows: $rows")

    val edgesImg = getImageToCropFromOriginal(img)

    val listBuffer = new ParArray[(Double, (Int, Int))](rows * cols)
    val blackArgb = new Argb(0, 0, 0, 0)

    for (c <- (0 to cols - 1).par) {
      log.info(s"cropping edge-filtered image ${c*rows} of ${cols*rows}")
      for(r <- 0 to rows - 1) {
        val cropped = SimpleCrop((cols, rows), (c, r), edgesImg)

        val avgRgbOfCropped = argbEstimator(cropped)

        val dist = argbDistance(blackArgb, avgRgbOfCropped)

        listBuffer.update(rows*c + r, (dist, (c, r)))
      }
    }

    val theDistances = listBuffer.toArray

    val levels = 3
    val sizesForLevels = (0 to levels - 1).map{l => l -> (l + 1)}.toMap

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

    val levelsArr = ValuesToLevels(distArr, levels, stepMaker)

    val levelsArrWSizes = levelsArr.map(v => v.map {
      e => (e, sizesForLevels(e))
    }.toArray).toArray

    val forPrinting = levelsArrWSizes.flatten.sortBy(_._1)

    forPrinting.take(10).foreach{ case (l, s) => println(s"level $l has size $s")}
    forPrinting.reverse.take(10).foreach{ case (l, s) => println(s"level $l has size $s")}

    val quadCells = LevelsWithSquareSizesToCells(levelsArrWSizes, cols, rows)

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
//    val smallestLevelFirst = sortedByLevel

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

object WhichStep {
  def apply(steps: Array[(Double, Double)], value: Double): Int = {
    steps.indices.flatMap { i =>
      value match {
        case s if s >= steps(i)._1 && s <= steps(i)._2 =>
          Seq(i)
        case s if s < steps(0)._1 =>
          Seq(0)
        case s if s > steps.last._2 =>
          Seq(steps.length - 1)
        case _ =>
          Seq()
      }
    }.head
  }
}

trait StepMaker {
  def apply(levels: Int, min: Double, max: Double, delta: Double): Array[(Double, Double)]
}

object UniformStepMaker extends StepMaker {
  def apply(levels: Int, min: Double, max: Double, delta: Double): Array[(Double, Double)] = {
    (0 to levels - 1).map { l =>
      (min + l * delta, min + (l + 1) * delta)
    }.toArray
  }
}

object PlateauToMaxStepMaker extends StepMaker {

  def apply(levels: Int, min: Double, max: Double, delta: Double): Array[(Double, Double)] = {

    val uniformSteps = UniformStepMaker(levels, min, max, delta)

    /*

    start with uniform:

    |    |    |    |
    min            max


    shift abs max cannot exceed delta, as we may have second to last be gt max

    then define

    let i : zeroindexof(l)
    shift : i -> i/levels * delta if i is not the index of the max, else 0
    l'(i) : l(i) -> l(i) + shift(i)
     */

    val mapOfShift = (0 to levels - 1).map { i =>
      i -> i.toDouble / levels.toDouble
    }.toMap

    val newLevels: Array[((Double), (Double))] = uniformSteps.indices.toArray.map { i =>

      val (l1, l2) = uniformSteps(i)

      val (shift1, shift2) = (mapOfShift.getOrElse(i, 0.0), mapOfShift.getOrElse(i + 1, 0.0))

      (l1 + shift1, l2 + shift2)

    }
    newLevels


  }

}

object ApproxExpStepMaker extends StepMaker {

  override def apply(levels: Int, min: Double, max: Double, delta: Double): Array[(Double, Double)] = {
    /*

    from
    |   |   |   |

    to
    |     |   | |

    maybe something like this: http://math.nist.gov/DFTdata/atomdata/node6.html

    or maybe a forward shift function: f_n: f at edge n where n !=0 and n != N
      => f_n = V / n; where V is some constant < delta
    where edge tranformer is:
      => g_n = e_n + f_n

    */

    val uniformSteps: Array[(Double, Double)] = UniformStepMaker(levels, min, max, delta)

    val midpoint = (max + min) / 2.0

    def moveIt(thePoint: Double, min: Double, max: Double, mid: Double): Double = {
      (thePoint, mid) match {
        case (p, m) if p < m =>
          val distToMin = thePoint - min
          val shave = distToMin / (mid - min)
          min + shave * distToMin
        case (p, m) if p > m =>
          val distToMax = max - thePoint
          val shave = distToMax / (max - mid)
          max - shave * distToMax
        case (p, m) =>
          p
      }
    }

    val nonuniform = uniformSteps.indices map { case i =>
      val (e1, e2) = uniformSteps(i)
      (moveIt(e1, min, max, midpoint), moveIt(e2, min, max, midpoint))
    }


    nonuniform.toArray
  }
}

object ExpStepMaker extends StepMaker {
  override def apply(levels: Int, min: Double, max: Double, delta: Double): Array[(Double, Double)] = {

    val uniformSteps = UniformStepMaker(levels, min, max, delta)

    levels % 2 == 0 match {
      case true =>
        /*
        even # of levels, odd # of edges, like so:

        #levels = 4
        | c1 | c2 | c3 | c4 |

        edges: e_1, ... , e_N; N is odd

        let middle edge value: e_M = e_((N+1)/2)
        let origin-centered edges be: e'_n = e_n - e_M

        the positive half:
        | c3 | c4 |
        with e'_3 = 0.0
        with e'_N = e'_5 = e_5 - e_3

        the grid transformer f must be s.t. f(e'_N) = e'_N = e'_5

        f(e'_n) = e'


        */




      case false =>
        /*
        odd # of levels, even # of edges, like so:

        #levels = 3
        | c1 | c2 | c3 |

        */

    }

    ???
  }
}

object ValuesToLevels {
  def apply(values: Array[Array[Double]], levels: Int, stepMaker: StepMaker): Array[Array[Int]] = {

    println(s"total elements: ${values.map(_.length).sum}")

    val max: Double = values.map(v => v.max).max
    val min = values.map(v => v.min).min
    val delta = (max - min) / levels.toDouble

    println(s"min: $min max: $max delta: $delta")

    val steps = stepMaker(levels, min, max, delta)

    values.map(v => v.map(e => WhichStep(steps, e)))
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

