package Aurreprozesamendua;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.RemoveByName;
import weka.filters.unsupervised.attribute.RemoveUseless;
import weka.filters.unsupervised.attribute.Reorder;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.filters.unsupervised.instance.Randomize;
import weka.filters.unsupervised.instance.RemovePercentage;
import weka.filters.unsupervised.instance.Resample;
import weka.filters.unsupervised.instance.SparseToNonSparse;

import java.io.*;
import java.util.HashMap;

public class getBowArff {

    public static void main(String[] args){

        try{
            getBowArff(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3], args[4], args[5]);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Konprobatu ondo sartu direla BOW lortzeko parametroak: " +
                    "\n     1. /path/to/dataRAW.arff"+
                    "\n     2. Sartu 0 --> BoW edo 1 --> TF-IDF"+
                    "\n     3. Sartu 0 --> Sparse edo 1 --> NonSparse"+
                    "\n     4. /path/to/hiztegia.txt"+
                    "\n     5. /path/to/trainBOW.arff"+
                    "\n     6. /path/to/devRAW.arff");
        }


    }
    public static void getBowArff(String cleanDataArffPath,int errepresentazioBektoriala,int sparse, String hiztegiPath, String trainBoWPath,String devPath) {

        try{

            System.out.println("ARFF GARBITIK ABIATUTA BoW SORTUKO DA" + "\n");

            //DATUAK LORTU
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(cleanDataArffPath);
            Instances data = source.getDataSet();
            data.setClassIndex(data.numAttributes()-1);

            //IZENA ALDATU BEHAR ZAIO KLASEARI StringToWordVector EGIN AHAL IZATEKO
            data.renameAttribute(data.numAttributes()-1, "klasea");
            data.setClassIndex(data.numAttributes()-1);

            //TRAIN MULTZOA LORTU
            Resample resample = new Resample();
            resample.setRandomSeed(42);
            resample.setNoReplacement(true);
            resample.setInvertSelection(false);
            resample.setSampleSizePercent(70);
            resample.setInputFormat(data);
            Instances train=Filter.useFilter(data,resample);
            System.out.println("TRAIN INSTANTZIAK: " + train.numInstances() + "\n");

            //DEV MULTZOA LORTU
            resample.setRandomSeed(42);
            resample.setNoReplacement(true);
            resample.setInvertSelection(true);
            resample.setInputFormat(data);
            Instances dev= Filter.useFilter(data, resample);
            System.out.println("DEV INSTANTZIAK: " + dev.numInstances() + "\n");

            // TEST GORDE
            datuakGorde(devPath,dev);

            //StringToWordVector APLIKATU
            File hiztegia = new File(hiztegiPath); //HIZTEGIAREN FITXATEGIA SORTU
            Instances trainBoW= stringToWordVector(train, hiztegia,errepresentazioBektoriala); //TRAIN MULTZOAREN HIZTEGIA SORTU

            // SPARSE/NONSPARSE
            if(sparse==1){
                trainBoW = SparseToNonSparse(trainBoW);
            }

            trainBoW = reorder(trainBoW);
            //BoW ARFF-AN GORDE
            datuakGorde(trainBoWPath, trainBoW);



        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static Instances reorder(Instances test) throws Exception {
        Reorder filterR = new Reorder();
        filterR.setAttributeIndices("2-"+ test.numAttributes()+",1"); //2-atributu kop, 1.  2-atributu kop bitarteko atributuak goian jarriko dira eta 1 atributua (klasea dena) amaieran.
        filterR.setInputFormat(test);
        test = Filter.useFilter(test,filterR);
        return test;
    }

    private static void datuakGorde(String path, Instances data) throws Exception {

        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        saver.setFile(new File(path));
        saver.writeBatch();
    }

    private static Instances stringToWordVector(Instances train, File hiztegia, int bektorea) throws Exception {

        StringToWordVector stringToWordVector= new StringToWordVector();

        if(bektorea==1){
            stringToWordVector.setOutputWordCounts(true);
            stringToWordVector.setIDFTransform(true);
            stringToWordVector.setTFTransform(true);
        }
        else{
            stringToWordVector.setOutputWordCounts(false);
        }

        stringToWordVector.setAttributeIndices("first-last");
        stringToWordVector.setWordsToKeep(5000);                //defektuz 1000
        stringToWordVector.setPeriodicPruning(-1.0);

        stringToWordVector.setLowerCaseTokens(true); //MAYUS ETA MINUS ARTEKO BEREIZKETARIK EZ TRUE BADAGO
        stringToWordVector.setDictionaryFileToSaveTo(hiztegia); //HIZTEGIA GORDETZEKO FITXATEGIA EZARRI
        stringToWordVector.setInputFormat(train);
        Instances trainBOW= Filter.useFilter(train,stringToWordVector);
        return trainBOW;
    }

    private static Instances SparseToNonSparse(Instances data) throws Exception{

        SparseToNonSparse filterNonSparse = new SparseToNonSparse();
        filterNonSparse.setInputFormat(data);
        Instances nonSparseData = Filter.useFilter(data,filterNonSparse);
        return nonSparseData;
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
            String contentLine = br.readLine();     // @numDocs=numinstances kendu
            contentLine = br.readLine();
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
}