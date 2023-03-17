import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Reorder;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.filters.unsupervised.instance.Randomize;

import java.io.File;
import java.util.Random;

public class fssInfoGain {
    public static void main(String[] args) {
        try {
            args= new String[]{"trainBOW.arff", "hiztegiBerria.txt", " trainBOW_FSS_InfoGain.arff"};
            /** BOW/TFIDF --> InfoGain LORTU
             *  0. parametroa: .arff fitxategia (atributu guztiekin)
             *  1. parametroa: Hiztegi berria gordetzeko .txt
             *  2. parametroa: TrainBOW-ren bertsio berria gordetzeko. arff
             */

            //1. DATUAK LORTU
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(args[0]);
            Instances data = source.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);

            //2. DATUAK RANDOMIZATU
            Randomize r = new Randomize();
            r.setSeed(42);
            r.setInputFormat(data);
            data=Filter.useFilter(data, r);

            //3. ATRIBUTUEN SELEKZIOA PLANTEATU:
            Ranker ranker = new Ranker();
            AttributeSelection as = new AttributeSelection();
            as.setEvaluator(new InfoGainAttributeEval());
            as.setSearch(ranker);
            as.setInputFormat(data);
            System.out.println(data.numAttributes());

            int numaux=-1; //Numero de atributos que se quieren mantener (-1=todos)
            double taux=0.0; //Threshold puede ir del 0 (se mantienen todos los atributos) al 1 (se borran todos los atributos)
            double fmax = 0.0;

            for(int n=0; n<data.numAttributes()-1; n+=10){
                ranker.setNumToSelect(n);
                for(double t=0.0; t<1.0; t+=0.1){
                    ranker.setThreshold(t);
                    as.setSearch(ranker);
                    as.setInputFormat(data);
                    Instances filteredData= Filter.useFilter(data, as);
                    filteredData.setClassIndex(filteredData.numAttributes()-1);

                    Evaluation evaluation = new Evaluation(filteredData);
                    evaluation.crossValidateModel(new RandomForest(), filteredData, 3, new Random());
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

            ranker.setNumToSelect(numaux);
            ranker.setThreshold(taux);
            as.setSearch(ranker);
            as.setInputFormat(data);
            Instances filteredData= Filter.useFilter(data, as);
            filteredData.setClassIndex(filteredData.numAttributes()-1);

            //4. HIZTEGIA GORDE
            File hiztegia = new File(args[1]);
            Instances trainBOWBerria= stringToWordVector(filteredData,hiztegia);

            //5. DATUAK GORDE
            datuakGorde(args[2], trainBOWBerria);

        }catch (Exception e){e.printStackTrace();}
    }

    private static void datuakGorde(String path, Instances data) throws Exception {

        Reorder reorder = new Reorder();
        reorder.setAttributeIndices("2-last,1");        //klasea azken atributua izateko
        reorder.setInputFormat(data);
        data = Filter.useFilter(data, reorder);


        ArffSaver s = new ArffSaver();
        s.setInstances(data);
        s.setFile(new File(path));
        s.writeBatch();
    }

    private static Instances stringToWordVector(Instances train, File hiztegia) throws Exception {

        StringToWordVector stringToWordVector= new StringToWordVector();
        stringToWordVector.setLowerCaseTokens(true);                        // letra larria eta xeheak baliokideak
        stringToWordVector.setIDFTransform(false);
        stringToWordVector.setTFTransform(false);
        stringToWordVector.setDictionaryFileToSaveTo(hiztegia);             // hiztegia gordetzeko fitxategia ezarri
        //stringToWordVector.setWordsToKeep(2000);                            // bektorearen dimentsioa, coje las primeras x que tengan maiztasun mas grande ???
        stringToWordVector.setOutputWordCounts(false);                      // bitarra
        stringToWordVector.setInputFormat(train);
        Instances trainBOW= Filter.useFilter(train,stringToWordVector);
        return trainBOW;
    }
}
