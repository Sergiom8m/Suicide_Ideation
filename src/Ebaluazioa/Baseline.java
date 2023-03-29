package Ebaluazioa;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
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

public class Baseline {

    public static void main (String[] args){
        try {
            baseline (args[0], args[1], args[2], args[3]);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Konprobatu ondo sartu direla baseline lortzeko parametroak:" +
                    "\n     1. /path/to/dataFSS.arff"+
                    "\n     2. /path/to/trainFSS.arff"+
                    "\n     3. /path/to/devFSS.arff"+
                    "\n     4. /path/to/BaselineEmaitzak.txt");
        }
    }

    public static void baseline(String dataPath, String trainPath, String devPath, String emaitzak) throws Exception {

        System.out.println("DEFAULT RANDOM FOREST ERABILIZ BASELINE BURUTUKO DA" + "\n");

        //DATUAK DITUEN FITXATEGIA KARGATU
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(dataPath);
        Instances data = source.getDataSet();
        data.setClassIndex(data.numAttributes() - 1);

        //DEFAULT RANDOM FOREST CLASSIFIER SORTU
        RandomForest randomForest = new RandomForest();
        randomForest.buildClassifier(data);

        //.MODEL GORDE
        SerializationHelper.write("Ebaluazioa.Baseline.model",randomForest);

        FileWriter f = new FileWriter(emaitzak);
        BufferedWriter bf = new BufferedWriter(f);

        //1. EBALUAZIO EZ ZINTZOA
        System.out.println("EBALUAZIO EZ ZINTZOA BURUTZEN..." + "\n");
        bf.append("\n=============================================================\n");
        bf.append("EBALUAZIO EZ ZINTZOA:\n");

        Evaluation evaluation = new Evaluation(data);
        evaluation.evaluateModel(randomForest, data);

        bf.append(evaluation.toSummaryString()+"\n");
        bf.append(evaluation.toClassDetailsString()+"\n");
        bf.append(evaluation.toMatrixString());


        //2. K-FOLD CROSS EBALUAZIOA
        System.out.println("K-FOLD CROSS VALIDATION BURUTZEN..." + "\n");
        bf.append("\n=============================================================\n");
        bf.append("K-FOLD CROSS EBALUAZIOA:\n");

        evaluation = new Evaluation(data);
        evaluation.crossValidateModel(randomForest, data, 10, new Random(1));

        bf.append(evaluation.toSummaryString()+"\n");
        bf.append(evaluation.toClassDetailsString()+"\n");
        bf.append(evaluation.toMatrixString());


        //3. STRATIFIED REPEATED HOLD OUT
        System.out.println("HOLD OUT BURUTZEN..." + "\n");
        bf.append("\n=============================================================\n");

        double fMeasureMin = 1;
        String summary = "";
        String classDet = "";
        String matrix = "";

        for (int i = 0; i < 5; i++) {

            //TRAINFSS ETA TESTFSS LORTU
            source = new ConverterUtils.DataSource(dataPath);
            Instances dataHO = source.getDataSet();
            dataHO.setClassIndex(data.numAttributes() - 1);

            weka.filters.unsupervised.instance.Resample r = new Resample();
            r.setRandomSeed(i);
            r.setSampleSizePercent(70);
            r.setNoReplacement(true);
            r.setInvertSelection(false);
            r.setInputFormat(dataHO);
            Instances train = Filter.useFilter(dataHO, r);

            r.setRandomSeed(i);
            r.setSampleSizePercent(70);
            r.setNoReplacement(true);
            r.setInvertSelection(true);
            r.setInputFormat(dataHO);
            Instances test = Filter.useFilter(dataHO, r);

            evaluation = new Evaluation(train);

            randomForest.buildClassifier(train);

            evaluation.evaluateModel(randomForest, test);

            if (evaluation.weightedFMeasure() < fMeasureMin) {

                summary = evaluation.toSummaryString() + "\n";
                classDet = evaluation.toClassDetailsString() + "\n";
                matrix = evaluation.toMatrixString();

            }

            bf.append(summary);
            bf.append(classDet);
            bf.append(matrix);

            bf.close();
        }

    }
}
