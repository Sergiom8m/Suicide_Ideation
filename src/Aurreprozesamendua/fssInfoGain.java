package Aurreprozesamendua;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.attribute.Reorder;

import java.io.File;

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

            int numaux=-1; //Numero de atributos que se quieren mantener (-1=todos)
            double taux=0.0; //Threshold
            double fmax = 0.0;

            for(int n=0; n<data.numAttributes()-1; n++){
                ranker.setNumToSelect(n);
                System.out.println(n);
                for(double t=0.0; t<1.01; t+=0.1){
                    ranker.setThreshold(t);
                    as.setSearch(ranker);
                    as.setInputFormat(train);

                    //Erabiliko dugun baseko klasifikadorea (train erabiliz)
                    RandomForest rf = new RandomForest();
                    rf.buildClassifier(train);

                    //Train-eri filtroa aplikatuz geratuko diren parametroak ->
                    //Test multzoko atributuak egokitzeko
                    FilteredClassifier fc = new FilteredClassifier();
                    fc.setClassifier(rf);
                    fc.setFilter(as);
                    fc.buildClassifier(train);

                    //Ebaluazioa Filtered Classifier erabiliz egingo da non:
                    //AttributeSelection filtroa eta RandomForest Klasifikadorea jasotzen diren
                    Evaluation evaluation = new Evaluation(train);
                    evaluation.evaluateModel(fc, test);
                    //Ebaluazioaren eboluzioa ikusteko weighted F-Measure erabiliko dugu,
                    //baina beste bat erabili genezake
                    double f= evaluation.weightedFMeasure();

                    if(fmax<f){
                        System.out.println("Fmax berria: "+f);
                        fmax=f;
                        numaux=n;
                        taux=t;
                    }
                }
            }
            System.out.println("\nATERA DIREN PARAMETROAK:" +
                    "\nNumToSelect: " + numaux+
                    "\nThreshold: "+taux);
            System.out.println("LORTU DEN F-MEASURE MAXIMOA:"+ fmax);

            //4. LORTUTAKO PARAMETROEKIN DATUAK FILTRATU ETA MULTZO BERRIA LORTU
            ranker.setNumToSelect(numaux);
            ranker.setThreshold(taux);
            as.setSearch(ranker);
            as.setInputFormat(data);
            Instances filteredData= Filter.useFilter(data, as);
            filteredData.setClassIndex(filteredData.numAttributes()-1);

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