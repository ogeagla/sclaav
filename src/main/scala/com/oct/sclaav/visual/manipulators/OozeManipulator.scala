package com.oct.sclaav.visual.manipulators

import com.oct.sclaav.ImageManipulator
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.filter.OilFilter

object OozeManipulator extends ImageManipulator {
  override def apply(img: Image): Image = {


//    img.filter(DiffuseFilter(10.0f))
//    img.filter(EdgeFilter)
    img.filter(OilFilter(6, 8))

  }
}
