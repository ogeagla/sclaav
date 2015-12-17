package com.oct.sclaav.visual.raw

import java.io.{FileInputStream, FileOutputStream}
import java.net.URI
import java.util

import org.im4java.core.{DcrawCmd, DCRAWOperation}
import org.im4java.process.{Pipe, ProcessStarter}
import org.slf4j.LoggerFactory

object ExtractsThumbFromRaw {

  ProcessStarter.setGlobalSearchPath("/usr/local/bin/")

  val log = LoggerFactory.getLogger(getClass)

  def printCmd(cmd: util.LinkedList[String]) = {
    cmd.toArray.map(_.toString).foreach(log.info)
  }


  def apply(in: URI, out: URI) = {
    val inPath = in.getPath
    val outPath = out.getPath

    log.info(s"Inpath: $inPath, outpath: $outPath")


    val dcRawOp = new DCRAWOperation

    dcRawOp.halfSize()
    //    dcRawOp.createTIFF()
    //    dcRawOp.setGamma(2.4, 12.9)
    dcRawOp.extractThumbnail()
    dcRawOp.write2stdout()
    dcRawOp.addImage(inPath)

    val opArgs = dcRawOp.getCmdArgs

    log.info("Running cmd args:")
    printCmd(opArgs)

    val fileOutStream = new FileOutputStream(outPath)
    val fileInStream = new FileInputStream(inPath)
    val pipeOutToStream = new Pipe(null, fileOutStream)
    val pipeIn = new Pipe(fileInStream, null)
    val dcRawCmd = new DcrawCmd

    dcRawCmd.setInputProvider(pipeIn)
    dcRawCmd.setOutputConsumer(pipeOutToStream)
    dcRawCmd.run(dcRawOp)
    fileOutStream.close()
    fileInStream.close()

    log.info("Running:")
    val cmds = dcRawCmd.getCommand
    printCmd(cmds)

    //    DisplayCmd.show(outPath)
  }


}
