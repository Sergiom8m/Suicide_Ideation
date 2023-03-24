import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.instance.Randomize;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Random;

public class Baseline {

    public static void main (String[] args){
        try {
            baseline (args[0], args[1], args[2], args[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void baseline(String dataPath, String trainPath, String devPath, String emaitzak) throws Exception {

        System.out.println("NAIVE BAYES ERABILIZ BASELINE BURUTUKO DA" + "\n");

        //DATUAK DITUEN FITXATEGIA KARGATU
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(dataPath);
        Instances data = source.getDataSet();
        data.setClassIndex(data.numAttributes() - 1);

        source = new ConverterUtils.DataSource(trainPath);
        Instances train = source.getDataSet();
        train.setClassIndex(train.numAttributes() - 1);

        source = new ConverterUtils.DataSource(devPath);
        Instances dev = source.getDataSet();
        dev.setClassIndex(dev.numAttributes() - 1);

        //NAIVE BAYES CLASSIFIER SORTU
        NaiveBayes klasifikadore = new NaiveBayes();
        klasifikadore.buildClassifier(data);

        //.MODEL GORDE
        SerializationHelper.write("Baseline.model",klasifikadore);

        FileWriter f = new FileWriter(emaitzak);
        BufferedWriter bf = new BufferedWriter(f);

        //1. EBALUAZIO EZ ZINTZOA
        System.out.println("EBALUAZIO EZ ZINTZOA BURUTZEN..." + "\n");
        bf.append("\n=============================================================\n");
        bf.append("EBALUAZIO EZ ZINTZOA:\n");

        Evaluation evaluation = new Evaluation(data);
        evaluation.evaluateModel(klasifikadore, data);

        bf.append(evaluation.toSummaryString()+"\n");
        bf.append(evaluation.toClassDetailsString()+"\n");
        bf.append(evaluation.toMatrixString());


        //2. K-FOLD CROSS EBALUAZIOA
        System.out.println("K-FOLD CROSS VALIDATION BURUTZEN..." + "\n");
        bf.append("\n=============================================================\n");
        bf.append("K-FOLD CROSS EBALUAZIOA:\n");

        evaluation = new Evaluation(data);
        evaluation.crossValidateModel(klasifikadore, data, 10, new Random(1));

        bf.append(evaluation.toSummaryString()+"\n");
        bf.append(evaluation.toClassDetailsString()+"\n");
        bf.append(evaluation.toMatrixString());


        //3. STRATIFIED HOLD OUT
        System.out.println("HOLD OUT BURUTZEN..." + "\n");
        bf.append("\n=============================================================\n");
        bf.append("STRATIFIED HOLD OUT (%80):\n");

        evaluation = new Evaluation(train);

        klasifikadore = new NaiveBayes();
        klasifikadore.buildClassifier(train);

        evaluation.evaluateModel(klasifikadore, dev);

        bf.append(evaluation.toSummaryString()+"\n");
        bf.append(evaluation.toClassDetailsString()+"\n");
        bf.append(evaluation.toMatrixString());

        bf.close();

    }
}
