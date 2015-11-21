package com.oct.sclaav.visual.assembly.grid

import com.oct.sclaav.CompleteAssembler
import com.oct.sclaav.visual.computation.GeneratesRandomQuadrilateralGrid
import com.sksamuel.scrimage.Image

object QuadrilateralAssembler extends CompleteAssembler {
  override def apply(theReferenceImage: Image, theBackgroundImage: Image, samples: Array[Image]): Image = {

    val (rows, cols) = (10, 10)
    val quadsGrid = GeneratesRandomQuadrilateralGrid(rows, cols)



    ???
  }
}
