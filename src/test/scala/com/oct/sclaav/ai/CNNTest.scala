package com.oct.sclaav.ai

import java.util.Random

import com.oct.sclaav.TestHelpers
import com.sksamuel.scrimage.Image
import org.deeplearning4j.datasets.iterator.DataSetIterator
import org.deeplearning4j.datasets.iterator.impl.IrisDataSetIterator
import org.deeplearning4j.eval.Evaluation
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.layers.setup.ConvolutionLayerSetup
import org.deeplearning4j.nn.conf.layers.{ConvolutionLayer, OutputLayer}
import org.deeplearning4j.nn.conf.{GradientNormalization, MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.params.DefaultParamInitializer
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.api.IterationListener
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.{DataSet, SplitTestAndTrain}
import org.nd4j.linalg.lossfunctions.LossFunctions
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._


class CNNTest extends FunSuite with BeforeAndAfter with Matchers with TestHelpers {

  test("something happens") {

    /*this test is from:

    https://github.com/kogecoo/dl4j-0.4-examples-scala/blob/master/src/main/scala/org/deeplearning4j/examples/convolution/CNNIrisExample.scala

    */
    val foothillsImg = Image.fromFile(mosaicBoulderFoothills)

    val argbs: Array[Array[Int]] = foothillsImg.argb

    lazy val log = LoggerFactory.getLogger(getClass)


    val numRows = 2
    val numColumns = 2
    val nChannels = 1
    val outputNum = 3
    val numSamples = 150
    val batchSize = 110
    val iterations = 10
    val splitTrainNum = 100
    val seed = 123
    val listenerFreq = 1


    /**
      *Set a neural network configuration with multiple layers
      */
    log.info("Load data....")
    val irisIter: DataSetIterator = new IrisDataSetIterator(batchSize, numSamples)
    val iris: DataSet = irisIter.next()
    iris.normalizeZeroMeanZeroUnitVariance()

    val trainTest: SplitTestAndTrain = iris.splitTestAndTrain(splitTrainNum, new Random(seed))

    val builder: MultiLayerConfiguration.Builder = new NeuralNetConfiguration.Builder()
      .seed(seed)
      .iterations(iterations)
//      .batchSize(batchSize)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .gradientNormalization(GradientNormalization)
      .l2(2e-4)
      .regularization(true)
      .useDropConnect(true)
      .list(2)
      .layer(0, new ConvolutionLayer.Builder(Array(1, 1):_*)
        .nIn(nChannels)
        .nOut(6).dropOut(0.5)
        .activation("relu")
        .weightInit(WeightInit.XAVIER)
        .build())
      .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
        .nIn(6)
        .nOut(outputNum)
        .weightInit(WeightInit.XAVIER)
        .activation("softmax")
        .build())

      .backprop(true).pretrain(false)
    new ConvolutionLayerSetup(builder, numRows, numColumns, nChannels);

    val conf: MultiLayerConfiguration = builder.build()

    log.info("Build model....")
    val model: MultiLayerNetwork = new MultiLayerNetwork(conf)
    model.init()
    model.setListeners(Seq[IterationListener](new ScoreIterationListener(listenerFreq)).asJava)

    log.info("Train model....")
    model.fit(trainTest.getTrain())

    log.info("Evaluate weights....")
    model.getLayers.foreach { case (layer: org.deeplearning4j.nn.api.Layer) =>
      val w: INDArray = layer.getParam(DefaultParamInitializer.WEIGHT_KEY)
      log.info("Weights: " + w)
    }

    log.info("Evaluate model....")
    val eval = new Evaluation(outputNum)
    val output: INDArray = model.output(trainTest.getTest().getFeatureMatrix())
    eval.eval(trainTest.getTest().getLabels(), output)
    log.info(eval.stats())

    log.info("****************Example finished********************")



  }

}
