package com.oct.sclaav

import java.io.File

trait TestHelpers {
  val bapImagesDir = new File(getClass.getResource("/bap-images").getPath)
  val testRootPath = getClass.getResource("/").getPath
}
