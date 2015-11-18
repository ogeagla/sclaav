package com.oct.sclaav.cli

import java.net.URI

import com.oct.sclaav.TestHelpers
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
  }

}
