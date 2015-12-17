package com.oct.sclaav.visual.raw

import java.io.{FileInputStream, FileOutputStream}
import java.util

import com.oct.sclaav.TestHelpers
import org.im4java.core.{DcrawCmd, DCRAWOperation}
import org.im4java.process.{Pipe, ProcessStarter}
import org.scalatest.{Matchers, BeforeAndAfter, FunSuite}


/*
## Raw WIP

to be truly dep-free, should I?:
 - create docker image with some ubuntu base
 - install dcraw/libraw
 - have a scala wrapper around the runnin docker container:
   - wrapper uploads raw images into container and dls the finished extracted filet to local FS

WIP -> use im4j
NXT -> use jrawio: either compile code into this proj or add jar manually to classpath... http://jrawio.rawdarkroom.org/
  dcraw Java interface: http://ij-plugins.sourceforge.net/plugins/dcraw/
  use libraw from Rust: http://www.libraw.org/docs
  use libraw-rust: does not currently compile
  use dcraw from Rust: WIP


*/
class ConvertsRawImagesTest extends FunSuite with BeforeAndAfter with Matchers with TestHelpers {

  test("converts raw imgs") {

    def printCmd(cmd: util.LinkedList[String]) = {
      cmd.toArray.map(_.toString).toList.foreach(println(_))
    }

    ProcessStarter.setGlobalSearchPath("/usr/local/bin/")

    val inPath = s"${testRootPath}raw/cr2/E1DXLL0000503.CR2"
    val outPath = s"${testRootPath}raw/cr2/thumb.jpeg"

    println(s"Inpath: $inPath, Outpath: $outPath")

    val dcRawOp = new DCRAWOperation

    dcRawOp.halfSize()
//    dcRawOp.createTIFF()
//    dcRawOp.setGamma(2.4, 12.9)
//    dcRawOp.extractThumbnail()
    dcRawOp.write2stdout()
    dcRawOp.addImage(inPath)

    val opArgs = dcRawOp.getCmdArgs

    println("Running cmd args:")
    printCmd(opArgs)

    val fileOutStream = new FileOutputStream(outPath)
    val fileInStream = new FileInputStream(inPath)
    val pipeOutToStream = new Pipe(null, fileOutStream)
    val pipeIn = new Pipe(fileInStream, null)
    val dcRawCmd = new DcrawCmd


    println("Running:")
    val cmds = dcRawCmd.getCommand
    printCmd(cmds)


    dcRawCmd.setInputProvider(pipeIn)
    dcRawCmd.setOutputConsumer(pipeOutToStream)
    dcRawCmd.run(dcRawOp)
    fileInStream.close()
    fileOutStream.close()

  }

}
