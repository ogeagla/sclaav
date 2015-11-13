package com.oct.sclaav

import java.net.URI

import com.oct.sclaav.visual.{MapsModes, Config}
import com.oct.sclaav.visual.Mode._
import scopt.OptionParser

object Parser {
  def parse(args: Array[String]): Option[Config] = parser.parse(args, Config())

  private lazy val parser = new OptionParser[Config]("Sclaav") {

    override def showUsageOnError = true

    def validateUri(failureMsg: String): URI => Either[String, Unit] = (uri: URI) => uri.getScheme match {
      case "file" => success
      case _ => failure(failureMsg)
    }

    implicit val modeRead: scopt.Read[Mode] = scopt.Read.reads(MapsModes(_))

    head("Scala Mosaic", "0.x.x")
    opt[Mode]('m', "mode") action { (x, c) =>
      c.copy(mode = x) } text "mode is an enum property"
    opt[URI]('t', "target") action { (x, c) =>
      c.copy(singleTarget = Some(x)) } text "target is a file property" validate validateUri("Target must be valid URI")
    opt[Boolean]('f', "filters") action { (x, c) =>
      c.copy(manipulate = x) } text "filters is a boolean property"
    opt[Int]('r', "rows") action { (x, c) =>
      c.copy(rows = x) } text "rows is an integer property"
    opt[Int]('c', "cols") action { (x, c) =>
      c.copy(cols = x) } text "cols is an integer property"
    opt[Int]('s', "samples") action { (x, c) =>
      c.copy(maxSamplePhotos = x) } text "samples is an integer property"
    opt[URI]('i', "in") required() valueName "<file>" action { (x, c) =>
      c.copy(in = Some(x)) } text "in is a required file property" validate validateUri("Input must be valid URI")
    opt[URI]('o', "out") required() valueName "<file>" action { (x, c) =>
      c.copy(out = Some(x)) } text "out is a required file property" validate validateUri("Output must be valid URI")
    opt[Unit]("verbose") action { (_, c) =>
      c.copy(verbose = true) } text "verbose is a flag"
    opt[Unit]("debug") hidden() action { (_, c) =>
      c.copy(debug = true) } text "this option is hidden in the usage text"
    note("some notes.\n")
    help("help") text "prints this usage text"

    checkConfig(_.validate match {
      case Right(_) => success
      case Left(msg) => failure(msg)
    })


  }

}
