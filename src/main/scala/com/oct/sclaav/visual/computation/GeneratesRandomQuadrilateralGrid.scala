package com.oct.sclaav.visual.computation

import com.oct.sclaav.visual.computation.CellIntersectsExisting.{FillQuadWithSingles, ApplyCellToTruthTable}
import com.oct.sclaav.{ImageToQuadGridThing, QuadrilateralCell, QuadrilateralGrid}
import com.sksamuel.scrimage.Image
import org.slf4j.LoggerFactory

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class GeneratesRandomQuadrilateralGrid extends ImageToQuadGridThing {
  def apply(img: Image, rows: Int, cols: Int): QuadrilateralGrid = {
    GeneratesRandomQuadrilateralGrid(rows, cols)
  }
}

object GeneratesRandomQuadrilateralGrid {
  val log = LoggerFactory.getLogger(getClass)

  def apply(rows: Int, cols: Int, iterations: Int = 50000, truthTable: Option[ArrayBuffer[ArrayBuffer[Boolean]]] = None): QuadrilateralGrid = generateRandomly(rows, cols, iterations, truthTable)

  def allTrue(arr: ArrayBuffer[ArrayBuffer[Boolean]]) = arr.forall(r => r.forall(c => c))

  def generateRandomly(rows: Int, cols: Int, iterations: Int = 5000, truthTable: Option[ArrayBuffer[ArrayBuffer[Boolean]]] = None): QuadrilateralGrid = {

    var arrBuff = truthTable match {
      case Some(theTable) => theTable
      case None => ArrayBuffer.fill(cols, rows)(false)
    }
    var cells = Array[QuadrilateralCell]()

    var iter = 0
    Random.setSeed(13)
    while(! allTrue(arrBuff) && iter < iterations) {

      val c = Random.nextInt(cols)
      val r = Random.nextInt(rows)

      val dc = math.min(cols - c - 1, Random.nextInt(cols / 2))
      val dr = math.min(rows - r - 1, Random.nextInt(rows / 2))

      val cell = new QuadrilateralCell(c, r, c + dc, r + dr)

      if( ! CellIntersectsExisting(arrBuff, cell)) {
        //FIXME this is both mutating the arr buff and re-assigning. i assume it will be optimized by compiler but not certain
        arrBuff = ApplyCellToTruthTable(arrBuff, cell)
        cells = cells.+:(cell)
      }

      iter = iter + 1
    }

    cells = cells.++:(FillQuadWithSingles(arrBuff))
    val quadGrid = new QuadrilateralGrid(rows, cols, cells)
    quadGrid
  }

}