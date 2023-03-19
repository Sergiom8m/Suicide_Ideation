package Aurreprozesamendua;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.attribute.Reorder;

import java.io.*;
import java.util.HashMap;

public class fssInfoGain {
    public static void fssInfoGain(String trainBowPath, String FSSArffPath) {
        try {
            /*
               0. parametroa: .arff fitxategia (atributu guztiekin)
               1. parametroa: TrainBOW-ren bertsio berria gordetzeko. arff
             */

            //DATUAK LORTU
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(trainBowPath);
            Instances data = source.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);


            //TRAIN MULTZOA LORTU
            Resample resample = new Resample();
            resample.setRandomSeed(42);
            resample.setInvertSelection(false);
            resample.setNoReplacement(true);
            resample.setSampleSizePercent(66);
            resample.setInputFormat(data);
            Instances train = Filter.useFilter(data, resample);
            train.setClassIndex(train.numAttributes()-1);

            //TEST MULTZOA LORTU
            resample.setRandomSeed(42);
            resample.setInvertSelection(true);
            resample.setNoReplacement(true);
            resample.setSampleSizePercent(66);
            resample.setInputFormat(data);
            Instances test = Filter.useFilter(data, resample);
            test.setClassIndex(test.numAttributes()-1);

            //ATRIBUTUEN AUKERAKETA
            Ranker ranker = new Ranker();
            AttributeSelection as = new AttributeSelection();
            as.setEvaluator(new InfoGainAttributeEval());
            as.setSearch(ranker);
            as.setInputFormat(train);

            int numaux = -1; //KONTSERBATUKO DIREN ATRIBUTU KOPURUA (-1 = GUZTIAK MANTENDU)
            double taux = 0.0; //THRESHOLD
            double fmax = 0.0; //F-MEASURE
            System.out.println(data.numAttributes());
            for(int n = 1; n < data.numAttributes()-1; n+=50){ //MANTENDUKO DIREN ATRIBUTU KOPURU OPTIMOA LORTU                   TODO TRAINFSS actual esta hecho con n+=500
                ranker.setNumToSelect(n);
                System.out.println(n);
                for(double t = Long.MIN_VALUE; t <1; t +=Long.MAX_VALUE/4){ //THRESHOLD OPTIMOA LORTU
                    ranker.setThreshold(t);
                    as.setSearch(ranker);
                    as.setInputFormat(train);

                    //ERABILIKO DEN OINARRIZKO CLASSIFIER-A (TRAIN MULTZOA ERABILIZ)
                    RandomForest rf = new RandomForest();
                    rf.buildClassifier(train);

                    //ATRIBUTUAK FILTRATUKO DITEN CLASSIFIER-A SORTU (TEST EGOKITZEN DU)
                    FilteredClassifier fc = new FilteredClassifier();
                    fc.setClassifier(rf);
                    fc.setFilter(as);
                    fc.buildClassifier(train);

                    //EBALUAZIOA EGIN FILTERED CLASSIFIER-A ERABILIZ
                    Evaluation evaluation = new Evaluation(train);
                    evaluation.evaluateModel(fc, test);
                    //SORTUTAKO MODELOAREN KALITATEA AZTERTZEKO F-MEASURE METRIKA AZTERTUKO DA
                    double fMeasure= evaluation.weightedFMeasure();

                    //F-MEASURE MAXIMOA EGUNRETZEA
                    if(fmax < fMeasure){
                        System.out.println("Fmax berria: "+fMeasure);
                        fmax = fMeasure;
                        numaux = n;
                        taux = t;
                    }
                }
            }
            System.out.println("\nPARAMETRO EKORKETAREN EMAITZAK:" +
                    "\nNumToSelect: " + numaux +
                    "\nThreshold: "+ taux);
            System.out.println("LORTU DEN F-MEASURE MAXIMOA:"+ fmax);


            //LORTUTAKO PARAMETROEKIN DATUAK FILTRATU ETA MULTZO BERRIA LORTU
            ranker.setNumToSelect(numaux);
            ranker.setThreshold(taux);
            as.setSearch(ranker);
            as.setInputFormat(data);
            Instances filteredData= Filter.useFilter(data, as);
            filteredData.setClassIndex(filteredData.numAttributes()-1);

            //DATUAK GORDE
            datuakGorde(FSSArffPath, filteredData);


            // HIZTEGIA SORTU ETA GORDE
            HashMap<String, Integer> hiztegia = hiztegiaSortu("hiztegia.txt",filteredData);
            hiztegiaGorde(hiztegia,"hiztegiaFSS.txt",filteredData);

        }catch (Exception e){e.printStackTrace();}
    }

    public static void hiztegiaGorde(HashMap<String, Integer> hiztegia, String path, Instances data) throws IOException {
        FileWriter fw = new FileWriter(path);
        fw.write("@@@numDocs="+data.numInstances()+"@@@\n"); //Beharrezkoa TF·IDF bihurketa egiteko

        for(int i=0; i<data.numAttributes()-1;i++){
            String atributua = data.attribute(i).name();
            if(hiztegia.containsKey(atributua)){
                fw.write(atributua+","+hiztegia.get(atributua)+"\n");
            }
        }
        fw.close();
    }


    public static HashMap<String,Integer> hiztegiaSortu(String pathRaw, Instances data) throws IOException {
        HashMap<String, Integer> hiztegia = new HashMap();

        for(int i=0;i<data.numAttributes()-1;i++) {
            Attribute attrib = data.attribute(i);
            hiztegia.put(attrib.name(),1);
        }

        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(pathRaw));
            String contentLine = br.readLine();
            contentLine = br.readLine();            //pa que se salte el @numDocs=numinstances
            while (contentLine != null) {
                String[] lerroa = contentLine.split(",");
                String atributua = lerroa[0];
                Integer maiztasuna = Integer.parseInt(lerroa[1]);

                if(hiztegia.containsKey(atributua)){
                    hiztegia.put(atributua,maiztasuna);
                }
                contentLine = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();

        }

        return hiztegia;
    }

    private static void datuakGorde(String path, Instances data) throws Exception {
        //INSTANTZIAK GORDE
        ArffSaver s = new ArffSaver();
        s.setInstances(data);
        s.setFile(new File(path));
        s.writeBatch();
    }
}