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
  def train(data: DataSet): Unit
  def apply(data: DataSet): Evaluation[Nothing]
}

class ConvolutionalNN(
    numRows: Int = 2,
    numColumns: Int = 2,
    nChannels: Int = 1,
    outputNum: Int = 3,
    numSamples: Int = 10,
    batchSize: Int = 110,
    iterations: Int = 10,
    splitTrainNum: Int = 100,
    seed: Int = 123,
    listenerFreq: Int = 1
                     ) extends ImageNN {

  val log = LoggerFactory.getLogger(getClass)

  val model = init()

  private def init(): MultiLayerNetwork = {

//    val builderSimple = new NeuralNetConfiguration.Builder()
//          .seed(123)
//          .iterations(10)
//          .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer)
//          .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
//          .list(3)
//          .layer(0, new ConvolutionLayer.Builder(10,10)
//            .stride(2,2)
//            .nIn(nChannels)
//            .nOut(3)
//            .weightInit(WeightInit.XAVIER)
//            .activation("relu")
//            .build())
//          .layer(1, new SubsamplingLayer.Builder(
//            SubsamplingLayer.PoolingType.MAX, Array(2,2))
//            .stride(2,2)
//            .build())
//          .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
//            .nOut(3)
//            .weightInit(WeightInit.XAVIER)
//            .activation("softmax")
//            .build())
//          .backprop(true)
//          .pretrain(false)
//
//        new ConvolutionLayerSetup(builderSimple, 10, 30,
//          nChannels)    // FIXME: what does this do?
//
//        val confSimple = builderSimple.build()
//
//        val modelSimple = new MultiLayerNetwork(confSimple)
//        modelSimple.init()
//        modelSimple.setListeners(
//          Seq[IterationListener](new ScoreIterationListener(5)).asJava)// util.Arrays.asList((IterationListener) new ScoreIterationListener(5)))
//        modelSimple

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

  def apply(data: DataSet): Evaluation[Nothing] = {
    val eval: Evaluation[Nothing] = new Evaluation(outputNum)
    val output = model.output(data.getFeatureMatrix)
    eval.eval(data.getLabels, output)
    eval
  }

}
