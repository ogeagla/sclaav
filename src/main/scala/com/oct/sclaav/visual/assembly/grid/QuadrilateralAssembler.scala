package com.oct.sclaav.visual.assembly.grid

import com.oct.sclaav.{ImageToQuadGridThing, QuadrilateralGridToAbsolutePositions, CompleteAssembler}
import com.oct.sclaav.visual.computation._
import com.oct.sclaav.visual.manipulators.SimpleAbsoluteCrop
import com.oct.sclaav.visual.search.MatchByArgbAverage
import com.sksamuel.scrimage.nio.JpegWriter
import com.sksamuel.scrimage.{Image, ScaleMethod}
import org.slf4j.LoggerFactory

import scala.collection.parallel.mutable.ParArray

object QuadrilateralAssembler extends CompleteAssembler {
  override def apply(theReferenceImage: Image, theBackgroundImage: Image, samples: Array[Image]): Image = (new QuadrilateralAssembler)(theReferenceImage, theBackgroundImage, samples)
}

class QuadrilateralAssembler(cols: Int = 20, rows: Int = 20) {

  val log = LoggerFactory.getLogger(getClass)
  implicit val writer = JpegWriter.Default

  def apply(theReferenceImage: Image, theBackgroundImage: Image, samples: Array[Image], gridGen: ImageToQuadGridThing = new GeneratesRandomQuadrilateralGrid): Image = {
    val controlSize = (theReferenceImage.width, theReferenceImage.height)

    val (colWidth, rowHeight) = (controlSize._1 / cols, controlSize._2 / rows)

    log.info("generating quad grid")
    val quadsGrid = gridGen(theReferenceImage, rows, cols)
    val absQuadsGrid = (new QuadrilateralGridToAbsolutePositions(controlSize._1, controlSize._2))(quadsGrid)
    val listBuffer = new ParArray[(Image, (Int, Int))](absQuadsGrid.length)

    log.info(s"finding matches for ${listBuffer.length} crops")
    for (i <- absQuadsGrid.indices.par) {

      log.info(s"cropping quad")
      val q = absQuadsGrid(i)

      val quadSize = (q.endW - q.startW, q.endH - q.startH)

      val scaledSamples = samples.map(i => i.scaleTo(quadSize._1, quadSize._2, ScaleMethod.FastScale))

      val cropped = SimpleAbsoluteCrop(q.startW, q.startH, q.endW, q.endH, theReferenceImage)

      val matchToCropped = MatchByArgbAverage(cropped, scaledSamples)

      listBuffer.update(i, (matchToCropped, (q.startW, q.startH)))
    }

    log.info(s"assembling using ${listBuffer.length} sub-images")
    val assembled = SimpleCompleteAbsoluteAssembler(theBackgroundImage, listBuffer.toArray)

    assembled
  }
}
