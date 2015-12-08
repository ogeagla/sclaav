package com.oct.sclaav.visual.computation

import com.oct.sclaav.visual.computation.CellIntersectsExisting.ApplyCellToTruthTable
import com.oct.sclaav.{QuadrilateralGridToAbsolutePositions, AbsoluteQuadrilateralPosition, QuadrilateralGrid, QuadrilateralCell}
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

import scala.collection.mutable.ArrayBuffer

class GeneratesQuadrilateralGridTest extends FunSuite with BeforeAndAfter with Matchers {

  test("quad grid to absolute positions") {

    val randomlyGeneratedGrid = new QuadrilateralGrid(4, 4, Array(new QuadrilateralCell(1,2,3,2)))

    val (sizeW, sizeH) = (100, 800)

    val converter = new QuadrilateralGridToAbsolutePositions(sizeW, sizeH)

    val theAbsolutes = converter(randomlyGeneratedGrid)

    assert(theAbsolutes.head === new AbsoluteQuadrilateralPosition(25, 400, 100, 600))

  }

  test("quad grid stuff") {

    val arrBuff = ArrayBuffer.fill(25, 25)(false)
    val arrBuffTrue = ArrayBuffer.fill(5, 5)(true)

    assert(GeneratesRandomQuadrilateralGrid.allTrue(arrBuff) === false, "not all true")
    assert(GeneratesRandomQuadrilateralGrid.allTrue(arrBuffTrue) === true, "all true")

    val cell = new QuadrilateralCell(2, 3, 6, 12)

    val arrBuffWCell = ApplyCellToTruthTable(arrBuff, cell)

    assert(arrBuffWCell(2)(3) === true)
    assert(arrBuffWCell(2)(2) === false)
    assert(arrBuffWCell(1)(3) === false)
    assert(arrBuffWCell(3)(4) === true)
    assert(arrBuffWCell(6)(12) === true)
    assert(arrBuffWCell(7)(13) === false)

    val anotherCellNoIntersect = new QuadrilateralCell(0, 0, 1, 2)
    val anotherCellYesIntersect = new QuadrilateralCell(0, 0, 2, 3)

    assert(CellIntersectsExisting(arrBuffWCell, anotherCellNoIntersect) === false)
    assert(CellIntersectsExisting(arrBuffWCell, anotherCellYesIntersect) === true)

    val cellsFromFalses = GeneratesRandomQuadrilateralGrid.fillRemainingWithSingleCells(arrBuffWCell)
    assert(cellsFromFalses.length === 25*25 - 50)

    val randomlyGeneratedOne = GeneratesRandomQuadrilateralGrid(4, 4)

    assert(randomlyGeneratedOne.listOfTheStuff.length === 12)

  }
}
