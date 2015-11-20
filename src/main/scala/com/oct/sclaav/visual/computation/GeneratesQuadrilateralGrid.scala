package com.oct.sclaav.visual.computation

import com.oct.sclaav.{QuadrilateralCell, QuadrilateralGrid}

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object GeneratesQuadrilateralGrid {

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
      doesNotInter = doesNotInter && (!arrBuff(c)(r))
    }
    ! doesNotInter
  }

  def falseToSingleCells(arrBuff: ArrayBuffer[ArrayBuffer[Boolean]]): Array[QuadrilateralCell] = {

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

    var arrBuff = ArrayBuffer.fill(rows, cols)(false)
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

    cells = cells.++:(falseToSingleCells(arrBuff))
    val quadGrid = new QuadrilateralGrid(rows, cols, cells)
    quadGrid
  }

}