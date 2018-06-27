package net.swinno.faultprediction;

import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.learning.config.Sgd;
import org.deeplearning4j.util.ModelSerializer;
import org.deeplearning4j.spark.impl.multilayer.SparkDl4jMultiLayer;
import org.deeplearning4j.spark.api.TrainingMaster;
import java.io.File;
import java.io.IOException;
import org.apache.spark.api.java.JavaSparkContext;;
import org.deeplearning4j.spark.impl.paramavg.ParameterAveragingTrainingMaster;

public class LSTMLearner {

    private static double LEARNING_RATE = 0.05;
    private static int lstmLayerSize = 24;
    private static int NB_INPUTS = 8;
    private static int numLabelClasses = 1;
    private static String SAVE_MODEL_FILE_NAME = "LSTMModel.zip";
    private ComputationGraph model;
    private boolean isCreated = false;
    private static int NB_EPOCHS = 5;
    private File locationToSave;
    private JavaSparkContext sc;

    public LSTMLearner() {

    }

    public LSTMLearner(JavaSparkContext sc, double learningRate,
                       int lstmLayerSize, int nbInputs,
                       int numLabelClasses, int nbEpochs) {
        this.sc = sc;
        this.LEARNING_RATE = learningRate;
        this.lstmLayerSize = lstmLayerSize;
        this.NB_INPUTS = nbInputs;
        this.numLabelClasses = numLabelClasses;
        this.NB_EPOCHS = nbEpochs;

    }

    public void create() {

        ComputationGraphConfiguration conf = new NeuralNetConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Sgd(LEARNING_RATE))
                .dropOut(0.2)
                .graphBuilder()
                .addInputs("trainFeatures")
                .setOutputs("predictMortality")
                .addLayer("L1", new GravesLSTM.Builder()
                        .nIn(NB_INPUTS)
                        .nOut(lstmLayerSize)
                        .activation(Activation.SOFTSIGN)
                        .weightInit(WeightInit.DISTRIBUTION)
                        .build(), "trainFeatures")
                .addLayer("L2", new GravesLSTM.Builder()
                        .nIn(lstmLayerSize)
                        .nOut((int)(lstmLayerSize*1.5))
                        .activation(Activation.SOFTSIGN)
                        .weightInit(WeightInit.DISTRIBUTION)
                        .build(), "L1")
                .addLayer("L3", new GravesLSTM.Builder()
                        .nIn((int)(lstmLayerSize*1.5))
                        .nOut(lstmLayerSize)
                        .activation(Activation.SOFTSIGN)
                        .weightInit(WeightInit.DISTRIBUTION)
                        .build(), "L2")
                .addLayer("predictMortality", new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                        .activation(Activation.SOFTMAX)
                        .weightInit(WeightInit.DISTRIBUTION)
                        .nIn(lstmLayerSize).nOut(numLabelClasses).build(),"L3")
                .pretrain(false).backprop(true)
                .build();

        model = new ComputationGraph(conf);
        model.init();
        isCreated = true;
        int batchSizePerWorker = 20;
        TrainingMaster tm = new ParameterAveragingTrainingMaster.Builder(batchSizePerWorker)    //Each DataSet object: contains (by default) 32 examples
                .averagingFrequency(5)
                .workerPrefetchNumBatches(2)            //Async prefetching: 2 examples per worker
                .batchSizePerWorker(batchSizePerWorker)
                .build();
    }

    public void fit(DataSetIterator trainData) {

        for( int i=0; i<NB_EPOCHS; i++ ){
            model.fit(trainData);
        }
    }

    public void saveModel() throws IOException {

        //Save the model
        File locationToSave = new File(SAVE_MODEL_FILE_NAME);
        boolean saveUpdater = true;
        ModelSerializer.writeModel(model, locationToSave, saveUpdater);

    }

    public void restoreModel() throws IOException {

        //Load the model
        model = ModelSerializer.restoreComputationGraph(locationToSave);
    }
}
