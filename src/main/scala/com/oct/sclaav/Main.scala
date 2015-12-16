package com.oct.sclaav

import java.io.File

import com.oct.sclaav.cli.Parser
import com.oct.sclaav.visual.assembly.mosaic.DoMosaic
import com.oct.sclaav.visual.computation.{SimpleArgbDistance, SimpleArgbEstimator, MatchesByArgbAverageThresh}
import com.sksamuel.scrimage.{ScaleMethod, Image}
import org.slf4j.LoggerFactory

object Main {

  val log = LoggerFactory.getLogger(getClass)

  def main(args: Array[String])  {

    Parser.parse(args) match {
      case Some(config) =>

        val inPath = config.in
        val outPath = config.out
        val maxSamples = config.maxSamplePhotos
        val rows = config.rows
        val cols = config.cols
        val doManipulate = config.manipulate
        val mode = config.mode

        log.info(s"inPath: $inPath outPath: $outPath")

        val files = new File(inPath.get).listFiles().filter(_.isFile).take(maxSamples.get)

        mode match {

          case Mode.SIMILARITY =>

            log.info("doing similarity")

            val target = config.singleTarget
            val targetImg = Image.fromFile(new File(target.get))
            val (targetImgW, targetImgH) = (targetImg.width, targetImg.height)

            val similarityScale = 0.5
            val (scTargetImgW, scTargetImgH) = ((targetImg.width*similarityScale).toInt, (targetImg.height*similarityScale).toInt)


            val scaledTargetImg = targetImg.scaleTo(scTargetImgW, scTargetImgH, ScaleMethod.FastScale)
            val scaledSampleFiles = files.map(f => Image.fromFile(f).scaleTo(scTargetImgW, scTargetImgH))

            val matches = MatchesByArgbAverageThresh(SimpleArgbEstimator, SimpleArgbDistance, scaledTargetImg, scaledSampleFiles)

            log.info(s"Found ${matches.length} matches!")

          case Mode.SIMILARITY_PERMUTE =>


          case Mode.MOSAIC_SINGLE_FILE =>
            log.info("using single file target")
            val target = config.singleTarget
            DoMosaic(new File(target.get), files, cols.get, rows.get, new File(outPath.get), doManipulate = doManipulate)

          case Mode.MOSAIC_PERMUTE_ALL_FILES =>
            log.info("permuting all files in input dir")
            for(file <- files) {
              val controlFile = file
              val sampleFiles = files.filter(_ != controlFile)

              log.info(s"running with control image: ${controlFile.getName}")

              DoMosaic(controlFile, sampleFiles, cols.get, rows.get, new File(outPath.get), doManipulate = doManipulate)
            }
        }
      case None =>
        log.error(s"Failed to parse args ${args.toList.toString}")
    }

  }
}
