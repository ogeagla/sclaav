package com.oct.sclaav.visual.computation

import com.oct.sclaav.{AbsoluteQuadrilateralPosition, QuadrilateralCell, QuadrilateralGrid}
import org.slf4j.LoggerFactory

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class QuadrilateralGridToAbsolutePositions(sizeW: Int, sizeH: Int) {
  def apply(grid: QuadrilateralGrid): Array[AbsoluteQuadrilateralPosition] = {

    val cols = grid.cols
    val rows = grid.rows

    val colPixels = sizeW / cols
    val rowPixels = sizeH / rows

    grid.listOfTheStuff.map { cell =>

      val colW = math.max(cell.endCol - cell.startCol, 1)
      val rowW = math.max(cell.endRow - cell.startRow, 1)

      val startWP = cell.startCol * colPixels
      val endWP = (cell.endCol + 1) * colPixels
      val startHP = cell.startRow * rowPixels
      val endHP = (cell.endRow + 1) * rowPixels

      new AbsoluteQuadrilateralPosition(startWP, startHP, endWP, endHP)
    }
  }
}

object GeneratesRandomQuadrilateralGrid {
  val log = LoggerFactory.getLogger(getClass)

  def apply(rows: Int, cols: Int, iterations: Int = 50000): QuadrilateralGrid = generateRandomly(rows, cols, iterations)

  def flipToTrue(arrBuff: ArrayBuffer[ArrayBuffer[Boolean]], cell: QuadrilateralCell): ArrayBuffer[ArrayBuffer[Boolean]] = {
    for(c <- cell.startCol to cell.endCol; r <- cell.startRow to cell.endRow) {
      arrBuff(c).update(r, true)
    }
    arrBuff
  }

  def allTrue(arr: ArrayBuffer[ArrayBuffer[Boolean]]) = arr.forall(r => r.forall(c => c))

  def intersectsExisting(arrBuff: ArrayBuffer[ArrayBuffer[Boolean]], cell: QuadrilateralCell): Boolean = {
    var doesNotInter = true

    for(c <- cell.startCol to cell.endCol; r <- cell.startRow to cell.endRow) {
      try {
        doesNotInter = doesNotInter && (!arrBuff(c)(r))
      } catch {
        case e: Exception =>
          log.info(e.getMessage)
      }
    }
    ! doesNotInter
  }

  def fillRemainingWithSingleCells(arrBuff: ArrayBuffer[ArrayBuffer[Boolean]]): Array[QuadrilateralCell] = {

    val cols = arrBuff.length
    val rows = arrBuff(0).length

    var cells = Array[QuadrilateralCell]()
    for (c <- 0 to cols - 1; r <- 0 to rows - 1) {
      arrBuff(c)(r) match {
        case false => cells = cells.+:(new QuadrilateralCell(c, r, c, r))
        case true =>
      }
    }
    cells
  }

  def generateRandomly(rows: Int, cols: Int, iterations: Int = 5000): QuadrilateralGrid = {

    var arrBuff = ArrayBuffer.fill(cols, rows)(false)
    var cells = Array[QuadrilateralCell]()

    var iter = 0
    Random.setSeed(13)
    while(! allTrue(arrBuff) && iter < iterations) {

      val c = Random.nextInt(cols)
      val r = Random.nextInt(rows)

      val dc = math.min(cols - c - 1, Random.nextInt(cols / 2))
      val dr = math.min(rows - r - 1, Random.nextInt(rows / 2))

      val cell = new QuadrilateralCell(c, r, c + dc, r + dr)

      if( ! intersectsExisting(arrBuff, cell)) {
        //FIXME this is both mutating the arr buff and re-assigning. i assume it will be optimized by compiler but not certain
        arrBuff = flipToTrue(arrBuff, cell)
        cells = cells.+:(cell)
      }

      iter = iter + 1
    }

    cells = cells.++:(fillRemainingWithSingleCells(arrBuff))
    val quadGrid = new QuadrilateralGrid(rows, cols, cells)
    quadGrid
  }

}