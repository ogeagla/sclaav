package com.oct.sclaav.visual.computation

import com.oct.sclaav.QuadrilateralCell
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

import scala.collection.mutable.ArrayBuffer

class GeneratesQuadrilateralGridTest  extends FunSuite with BeforeAndAfter with Matchers {

  test("quad grid stuff") {

    val arrBuff = ArrayBuffer.fill(25, 25)(false)
    val arrBuffTrue = ArrayBuffer.fill(5, 5)(true)

    assert(GeneratesQuadrilateralGrid.allTrue(arrBuff) === false, "not all true")
    assert(GeneratesQuadrilateralGrid.allTrue(arrBuffTrue) === true, "all true")

    val cell = new QuadrilateralCell(2, 3, 6, 12)

    val arrBuffWCell = GeneratesQuadrilateralGrid.flipToTrue(arrBuff, cell)

    assert(arrBuffWCell(2)(3) === true)
    assert(arrBuffWCell(2)(2) === false)
    assert(arrBuffWCell(1)(3) === false)
    assert(arrBuffWCell(3)(4) === true)
    assert(arrBuffWCell(6)(12) === true)
    assert(arrBuffWCell(7)(13) === false)

    val anotherCellNoIntersect = new QuadrilateralCell(0, 0, 1, 2)
    val anotherCellYesIntersect = new QuadrilateralCell(0, 0, 2, 3)

    assert(GeneratesQuadrilateralGrid.intersectsExisting(arrBuffWCell, anotherCellNoIntersect) === false)
    assert(GeneratesQuadrilateralGrid.intersectsExisting(arrBuffWCell, anotherCellYesIntersect) === true)

    val cellsFromFalses = GeneratesQuadrilateralGrid.falseToSingleCells(arrBuffWCell)
    assert(cellsFromFalses.length === 25*25 - 50)

    val randomlyGeneratedOne = GeneratesQuadrilateralGrid.generateRandomly(4, 4)

    assert(randomlyGeneratedOne.listOfTheStuff.length === 12)

  }
}
