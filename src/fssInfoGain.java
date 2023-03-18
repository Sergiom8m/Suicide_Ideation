import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.attribute.Reorder;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.filters.unsupervised.instance.Randomize;

import java.io.File;
import java.util.Random;

public class fssInfoGain {
    public static void main(String[] args) {
        try {
            args= new String[]{"trainBOW.arff", "hiztegiBerria.txt", "trainBOW_FSS_InfoGain.arff"};
            /** BOW/TFIDF --> InfoGain LORTU
             *  0. parametroa: .arff fitxategia (atributu guztiekin)
             *  1. parametroa: Hiztegi berria gordetzeko .txt
             *  2. parametroa: TrainBOW-ren bertsio berria gordetzeko. arff
             */

            //1. DATUAK LORTU
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(args[0]);
            Instances data = source.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);

            System.out.println(data.numAttributes());

            //2. DATUAK SEPARATU RESAMPLE BIDEZ
            Resample resample = new Resample();
            resample.setRandomSeed(42);
            resample.setInvertSelection(false);
            resample.setNoReplacement(true);
            resample.setSampleSizePercent(66);
            resample.setInputFormat(data);
            Instances train = Filter.useFilter(data, resample);
            train.setClassIndex(train.numAttributes()-1);

            resample.setRandomSeed(42);
            resample.setInvertSelection(true);
            resample.setNoReplacement(true);
            resample.setSampleSizePercent(66);
            resample.setInputFormat(data);
            Instances test = Filter.useFilter(data, resample);
            test.setClassIndex(test.numAttributes()-1);

            //3. ATRIBUTUEN SELEKZIOA PLANTEATU:
            Ranker ranker = new Ranker();
            AttributeSelection as = new AttributeSelection();
            as.setEvaluator(new InfoGainAttributeEval());
            as.setSearch(ranker);
            as.setInputFormat(train);
            System.out.println(train.numAttributes());


            int numaux=-1; //Numero de atributos que se quieren mantener (-1=todos)
            double taux=0.0; //Threshold puede ir del 0 (se mantienen todos los atributos) al 1 (se borran todos los atributos)
            double fmax = 0.0;

            for(int n=0; n<data.numAttributes()-1; n++){
                ranker.setNumToSelect(n);
                System.out.println(n);
                for(double t=0.0; t<1.01; t+=0.1){
                    ranker.setThreshold(t);
                    as.setSearch(ranker);
                    as.setInputFormat(train);
                    Instances filteredData= Filter.useFilter(train, as);
                    filteredData.setClassIndex(filteredData.numAttributes()-1);

                    RandomForest rf = new RandomForest();
                    rf.buildClassifier(train);

                    FilteredClassifier fc = new FilteredClassifier();
                    fc.setClassifier(rf);
                    fc.buildClassifier(filteredData);

                    Evaluation evaluation = new Evaluation(filteredData);
                    evaluation.evaluateModel(fc, test);
                    double f= evaluation.weightedFMeasure();
                    if(fmax<f){
                        System.out.println("Fmax berria: "+f);
                        fmax=f;
                        numaux=n;
                        taux=t;
                    }
                    System.out.println(5);
                }
            }
            System.out.println("\nATERA DIREN PARAMETROAK:" +
                    "\nNumToSelect: " + numaux+
                    "\nThreshold: "+taux);
            System.out.println("LORTU DEN F-MEASURE MAXIMOA:"+ fmax);


            ranker.setNumToSelect(numaux);
            ranker.setThreshold(taux);
            as.setSearch(ranker);
            as.setInputFormat(data);
            Instances filteredData= Filter.useFilter(data, as);
            filteredData.setClassIndex(filteredData.numAttributes()-1);

            System.out.println(filteredData.numAttributes());

            //5. DATUAK GORDE
            datuakGorde(args[2], filteredData);

        }catch (Exception e){e.printStackTrace();}
    }

    private static void datuakGorde(String path, Instances data) throws Exception {

        Reorder reorder = new Reorder();
        reorder.setInputFormat(data);
        data = Filter.useFilter(data, reorder);

        ArffSaver s = new ArffSaver();
        s.setInstances(data);
        s.setFile(new File(path));
        s.writeBatch();
    }
}