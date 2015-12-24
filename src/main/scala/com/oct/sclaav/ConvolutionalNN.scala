package com.oct.sclaav

import org.deeplearning4j.eval.Evaluation
import org.deeplearning4j.nn.api.{Layer, OptimizationAlgorithm}
import org.deeplearning4j.nn.conf.layers.setup.ConvolutionLayerSetup
import org.deeplearning4j.nn.conf.layers.{ConvolutionLayer, OutputLayer}
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.params.DefaultParamInitializer
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.api.IterationListener
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.lossfunctions.LossFunctions
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

trait ImageNN {

  def init(): MultiLayerNetwork
  def train(data: DataSet): Unit
  def apply(data: DataSet): Evaluation

}

class ConvolutionalNN() extends ImageNN {

  val log = LoggerFactory.getLogger(getClass)
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

  val model = init()

  def init(): MultiLayerNetwork = {

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
    model
  }

  def train(data: DataSet) = {
    model.fit(data)
  }

  def printWeights() = {
    model.getLayers.foreach {
      case (layer: Layer) =>
        val w = layer.getParam(DefaultParamInitializer.WEIGHT_KEY)
        log.info(s"Weights: ${w.toString}")
    }
  }

  def apply(data: DataSet): Evaluation = {
    val eval: Evaluation = new Evaluation(outputNum)
    val output = model.output(data.getFeatureMatrix)
    eval.eval(data.getLabels, output)
    eval
    ???
  }

}