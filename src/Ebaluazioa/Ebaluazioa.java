package Ebaluazioa;

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

        ebaluazioa(args[0], args[1], args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), args[7]);


    }


    public static void ebaluazioa(String trainPath,String devPath,String dataPath, int p1, int p2, int p3, int p4, String emaitzak) { //TODO blind test gehitu?

        try {

            System.out.println("TRAIN ETA DEV MULTZOAK ERABILITA SAILKATZAILEA EBALUATUKO DA" + "\n");

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
            System.out.println("EBALUAZIO EZ ZINTZOA BURUTZEN..." + "\n");
            bf.append("\n=============================================================\n");
            bf.append("EBALUAZIO EZ ZINTZOA:\n");

            Evaluation evaluation = new Evaluation(data);
            evaluation.evaluateModel(randomForest, data);

            bf.append(evaluation.toSummaryString()+"\n");
            bf.append(evaluation.toClassDetailsString()+"\n");
            bf.append(evaluation.toMatrixString());


            //4. K-FOLD CROSS EBALUAZIOA
            System.out.println("K-FOLD CROSS VALIDATION BURUTZEN..." + "\n");
            bf.append("\n=============================================================\n");
            bf.append("K-FOLD CROSS EBALUAZIOA:\n");

            evaluation = new Evaluation(data);
            evaluation.crossValidateModel(randomForest, data, 10, new Random(1));

            bf.append(evaluation.toSummaryString()+"\n");
            bf.append(evaluation.toClassDetailsString()+"\n");
            bf.append(evaluation.toMatrixString());



            //5. STRATIFIED HOLD OUT
            System.out.println("HOLD OUT BURUTZEN..." + "\n");
            bf.append("\n=============================================================\n");

            //TRAINFSS ETA TESTFSS LORTU
            source = new ConverterUtils.DataSource(trainPath);
            Instances train = source.getDataSet();
            train.setClassIndex(train.numAttributes()-1);

            source = new ConverterUtils.DataSource(devPath);
            Instances test = source.getDataSet();
            test.setClassIndex(test.numAttributes()-1);

            evaluation = new Evaluation(train);


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