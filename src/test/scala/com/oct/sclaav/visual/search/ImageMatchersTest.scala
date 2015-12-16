package com.oct.sclaav.visual.search

import java.io.File

import com.oct.sclaav.TestHelpers
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.nio.JpegWriter
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class ImageMatchersTest extends FunSuite with BeforeAndAfter with Matchers with TestHelpers {

  test("matches by thresh") {

    implicit val writer = JpegWriter.Default
    val outPath = new File(testRootPath)

    val files = bapImagesDir.listFiles().filter(_.isFile).take(400)

    val imgToMatch = files.filter(_.getName.contains("0207-")).head

    val theMatches = MatchesByArgbAverageThresh(imgToMatch, files)

    Image.fromFile(imgToMatch).output(testRootPath + "match_thresh_ref.jpeg")

    theMatches.map {
      case (f, i) =>
        i.output(testRootPath + s"matched_${f.getName}.jpeg")}

    assert(theMatches.head._1.getName.contains("0207-"), "img should match itself")
  }
}
