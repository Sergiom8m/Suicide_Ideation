package Aurreprozesamendua;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
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

public class InfoGainNuevo {
    public static void main(String args[]) {
        //args= new String[]{"trainBOW.arff","trainFSSNUEVO.arff"};
        String trainBowPath = "trainBOW.arff";
        String FSSArffPath = "trainFSSNUEVO.arff";
        try {
            /*
               0. parametroa: .arff fitxategia (atributu guztiekin)
               1. parametroa: TrainBOW-ren bertsio berria gordetzeko. arff
             */

            //DATUAK LORTU
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(trainBowPath);
            Instances data = source.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);

            int max=data.numAttributes();
            System.out.println(max);

            RandomForest randomForest = new RandomForest();

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

            randomForest.buildClassifier(train);

            Evaluation evaluation = new Evaluation(train);
            evaluation.evaluateModel(randomForest, test);

            System.out.println(evaluation.toClassDetailsString());

            //ATRIBUTUEN AUKERAKETA
            AttributeSelection as = new AttributeSelection();
            as.setEvaluator(new InfoGainAttributeEval());
            as.setSearch(new Ranker());
            as.setInputFormat(train);

            //Instances filteredData= Filter.useFilter(train, as);
            //filteredData.setClassIndex(filteredData.numAttributes()-1);

            FilteredClassifier fk = new FilteredClassifier();
            fk.setFilter(as);
            fk.setClassifier(randomForest);
            fk.buildClassifier(train);

            evaluation = new Evaluation(train);
            evaluation.evaluateModel(fk, test);

            System.out.println(evaluation.toClassDetailsString());

            //ConverterUtils.DataSink ds = new ConverterUtils.DataSink(FSSArffPath);
            //ds.write(filteredData);
            //DATUAK GORDE
            //datuakGorde(FSSArffPath, filteredData);


            // HIZTEGIA SORTU ETA GORDE
            //HashMap<String, Integer> hiztegia = hiztegiaSortu("hiztegia.txt",filteredData);
            //hiztegiaGorde(hiztegia,"hiztegiaFSSNUEVO.txt",filteredData);
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