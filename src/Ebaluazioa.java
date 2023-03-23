import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.Randomize;
import weka.filters.unsupervised.instance.Resample;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Random;

public class Ebaluazioa {

    public static void main (String[] args){

        main (args[0], args[1], args[2], Integer.parseInt(args[3]), Integer.parseInt(args[3]), Integer.parseInt(args[3]), Integer.parseInt(args[3]), args[5]);


    }


    public static void main(String trainPath,String devPath,String dataPath, int p1, int p2, int p3, int p4, String emaitzak) { //TODO blind test gehitu?
        /**
         * trainPath    ->  trainBowFSS.arff
         * devPath    ->  devFSS.arff
         * parametroak  ->  RandomForest parametro ekorketatik ateratakoak
         * emaitzak     ->  test_predictions.txt
         */
        try {

            //1. DATUAK KARGATU
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(dataPath);
            Instances data = source.getDataSet();
            data.setClassIndex(data.numAttributes()-1);

            //2. RANDOM FOREST PARAMETROAK KARGATU
            RandomForest randomForest = new RandomForest();
            randomForest.setNumExecutionSlots(Runtime.getRuntime().availableProcessors());
            randomForest.setNumFeatures(p1);
            randomForest.setNumIterations(p2);
            randomForest.setBagSizePercent(p3);
            randomForest.setMaxDepth(p4);
            randomForest.buildClassifier(data);

            //.MODEL GORDE
            SerializationHelper.write("RF.model",randomForest);


            FileWriter f = new FileWriter(emaitzak);
            BufferedWriter bf = new BufferedWriter(f);


            //3. EBALUAZIO EZ ZINTZOA
            System.out.println("Ebaluazio ez zintzoa burutzen...");
            bf.append("\n=============================================================\n");
            bf.append("EBALUAZIO EZ ZINTZOA:\n");

            Evaluation evaluation = new Evaluation(data);
            evaluation.evaluateModel(randomForest, data);

            bf.append(evaluation.toSummaryString()+"\n");
            bf.append(evaluation.toClassDetailsString()+"\n");
            bf.append(evaluation.toMatrixString());


            //4. K-FOLD CROSS EBALUAZIOA
            System.out.println("K-Fold cross ebaluazioa burutzen...");
            bf.append("\n=============================================================\n");
            bf.append("K-FOLD CROSS EBALUAZIOA:\n");

            evaluation = new Evaluation(data);
            evaluation.crossValidateModel(randomForest, data, 5, new Random(1));

            bf.append(evaluation.toSummaryString()+"\n");
            bf.append(evaluation.toClassDetailsString()+"\n");
            bf.append(evaluation.toMatrixString());



            //5. STRATIFIED HOLD OUT
            System.out.println("Hold out ebaluazioa burutzen...");
            bf.append("\n=============================================================\n");
            bf.append("STRATIFIED 50 REPEATED HOLD OUT (%80):\n");

            //TRAINFSS ETA TESTFSS LORTU
            source = new ConverterUtils.DataSource(trainPath);
            Instances train = source.getDataSet();
            train.setClassIndex(train.numAttributes()-1);

            source = new ConverterUtils.DataSource(devPath);
            Instances test = source.getDataSet();
            test.setClassIndex(test.numAttributes()-1);

            evaluation = new Evaluation(data);


            randomForest = new RandomForest();
            randomForest.setNumExecutionSlots(Runtime.getRuntime().availableProcessors());
            randomForest.setNumFeatures(p1);
            randomForest.setNumIterations(p2);
            randomForest.setBagSizePercent(p3);
            randomForest.setMaxDepth(p4);
            randomForest.buildClassifier(train);

            evaluation.evaluateModel(randomForest, test);


            bf.append(evaluation.toSummaryString()+"\n");
            bf.append(evaluation.toClassDetailsString()+"\n");
            bf.append(evaluation.toMatrixString());

            bf.close();


        }catch (Exception e){e.printStackTrace();}
    }
}