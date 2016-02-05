package com.oct.sclaav.ai

import java.util.Random

import com.oct.sclaav.{ConvolutionalNN, TestHelpers}
import com.sksamuel.scrimage.Image
import org.deeplearning4j.datasets.iterator.impl.IrisDataSetIterator
import org.deeplearning4j.eval.Evaluation
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.layers.setup.ConvolutionLayerSetup
import org.deeplearning4j.nn.conf.layers.{ConvolutionLayer, OutputLayer}
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.params.DefaultParamInitializer
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.api.IterationListener
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.{DataSet, SplitTestAndTrain}
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.lossfunctions.LossFunctions
import org.nd4j.linalg.util.FeatureUtil
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._


class CNNTest extends FunSuite with BeforeAndAfter with Matchers with TestHelpers {

  test("my cNN works") {

    val irisIter: IrisDataSetIterator = new IrisDataSetIterator(150, 150)
    val dataIris: DataSet = irisIter.next()
    dataIris.normalizeZeroMeanZeroUnitVariance()
    val trainTest: SplitTestAndTrain = dataIris.splitTestAndTrain(100, new Random(123))
    val labelsIris = trainTest.getTest().getLabels()

    val cnn = new ConvolutionalNN()

    val nSamples = 10
    val dataRaw = Nd4j.rand(nSamples,300)
    val labels = FeatureUtil.toOutcomeMatrix(Array(0,0,0,1,1,1,2,2,2,2),3)
    val data = new DataSet(dataRaw,labels)
    data.normalizeZeroMeanZeroUnitVariance()

    cnn.train(data)

//    cnn.printWeights()

    cnn(data)

  }

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


//    val nChannelsSimple = 1
//    val nSamples = 10
//    val data = Nd4j.rand(nSamples,300)
//    val labels = FeatureUtil.toOutcomeMatrix(Array(0,0,0,1,1,1,2,2,2,2),3)
//    val d = new DataSet(data,labels)
//    val builderSimple = new NeuralNetConfiguration.Builder()
//      .seed(123)
//      .iterations(10)
//      .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer)
//      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
//      .list(3)
//      .layer(0, new ConvolutionLayer.Builder(10,10)
//        .stride(2,2)
//        .nIn(nChannelsSimple)
//        .nOut(3)
//        .weightInit(WeightInit.XAVIER)
//        .activation("relu")
//        .build())
//      .layer(1, new SubsamplingLayer.Builder(
//        SubsamplingLayer.PoolingType.MAX, Array(2,2))
//        .stride(2,2)
//        .build())
//      .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
//        .nOut(3)
//        .weightInit(WeightInit.XAVIER)
//        .activation("softmax")
//        .build())
//      .backprop(true)
//      .pretrain(false)
//
//    new ConvolutionLayerSetup(builderSimple, 10, 30,
//      nChannelsSimple)    // FIXME: what does this do?
//
//    val confSimple = builderSimple.build()
//
//    val modelSimple = new MultiLayerNetwork(confSimple)
//    modelSimple.init()
//    modelSimple.setListeners(
//      Seq[IterationListener](new ScoreIterationListener(5)).asJava)// util.Arrays.asList((IterationListener) new ScoreIterationListener(5)))
//    modelSimple.fit(d)
//
//    log.info("Evaluate weights....")
//    modelSimple.getLayers.foreach { case (layer: org.deeplearning4j.nn.api.Layer) =>
//      val w: INDArray = layer.getParam(DefaultParamInitializer.WEIGHT_KEY)
//      log.info("Weights: " + w)
//    }
//
//    val trainTestSimple: SplitTestAndTrain = d.splitTestAndTrain(splitTrainNum, new Random(seed))
//
//
//    log.info("Evaluate model....")
//    val evalSimple = new Evaluation(outputNum)
//    val outputSimple: INDArray = modelSimple.output(trainTestSimple.getTest().getFeatureMatrix())
//    evalSimple.eval(trainTestSimple.getTest().getLabels(), outputSimple)
//    log.info(evalSimple.stats())
//
//    log.info("****************Example finished********************")


    log.info("Load data....")
    val irisIter: IrisDataSetIterator = new IrisDataSetIterator(150, 150)
    val iris: DataSet = irisIter.next()
    iris.normalizeZeroMeanZeroUnitVariance()
    Nd4j.shuffle(iris.getFeatureMatrix(), new Random(seed), 1)
    Nd4j.shuffle(iris.getLabels(),new Random(seed),1)
    val trainTest: SplitTestAndTrain = iris.splitTestAndTrain(splitTrainNum, new Random(seed))

    val builder = new NeuralNetConfiguration.Builder()
      .seed(seed)
      .iterations(iterations)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .list(2)
      .layer(0, new ConvolutionLayer.Builder(Array(1, 1):_*)
        .nIn(nChannels)
        .nOut(1000)
        .activation("relu")
        .weightInit(WeightInit.RELU)
        .build())
      .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
        .nOut(outputNum)
        .weightInit(WeightInit.XAVIER)
        .activation("softmax")
        .build())
      .backprop(true).pretrain(false)
    new ConvolutionLayerSetup(builder, numRows, numColumns, nChannels)

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
