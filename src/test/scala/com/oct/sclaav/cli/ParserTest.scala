package com.oct.sclaav.cli

import java.net.URI

import com.oct.sclaav.{Mode, TestHelpers}
import org.scalatest.{Matchers, BeforeAndAfter, FunSuite}

class ParserTest extends FunSuite with BeforeAndAfter with Matchers with TestHelpers {

  test("mosaic for single file") {
    val config = Parser.parse(Array(
      "--in", "file:///file/to/input",
      "--out", "file:///file/to/output",
      "--mode", "single",
      "--rows", "34",
      "--cols", "12",
      "--samples", "50",
      "--filters", "true",
      "--target", "file:///path/to/target"
    )).get

    assert(config.in === Some(new URI("file:///file/to/input")))
    assert(config.out === Some(new URI("file:///file/to/output")))
    assert(config.mode === Mode.MOSAIC_SINGLE_FILE)
    assert(config.rows === Some(34))
    assert(config.cols === Some(12))
    assert(config.maxSamplePhotos === Some(50))
    assert(config.manipulate === true)
    assert(config.singleTarget === Some(new URI("file:///path/to/target")))
  }

}
