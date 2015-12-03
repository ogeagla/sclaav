package com.oct.sclaav.visual.computation

import com.oct.sclaav.QuadrilateralGrid
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.filter.EdgeFilter
import org.slf4j.LoggerFactory

object GeneratesEdgeDensityBasedQuadrilateralGrid {

  val log = LoggerFactory.getLogger(getClass)

  def apply(img: Image, rows: Int = 10, cols: Int = 10, granularity: Double = 0.1, iterations: Int = 5): QuadrilateralGrid = {

    val imgScaled = img.scale(0.1)

    val edges = imgScaled.filter(EdgeFilter)

    ???
  }


}

