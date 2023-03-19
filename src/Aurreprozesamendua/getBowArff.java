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
import weka.filters.unsupervised.instance.SparseToNonSparse;

import java.io.*;
import java.util.HashMap;

public class getBowArff {
    public static void main(String cleanDataArffPath, String hiztegiPath, String trainBoWArffPath, String testPath) {

        try{
            //String[] args = new String[]{"Suicide_Detection.arff", "hiztegia.txt", "trainBOW.arff", "test.arff"};
            /*
                0. parametroa: .arff fitxategia
                1. parametroa: Hiztegia gordetzeko .txt
                2. parametroa: Train-ren BOW gordetzeko. arff
                3. parametroa: test.arff
             */

            //DATUAK LORTU
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(cleanDataArffPath);
            Instances data = source.getDataSet();
            data.setClassIndex(data.numAttributes()-1);


            //IZENA ALDATU BEHAR ZAIO KLASEARI StringToWordVector EGIN AHAL IZATEKO
            data.renameAttribute(data.numAttributes()-1, "klasea");
            data.setClassIndex(data.numAttributes()-1);

            //HOLD OUT

            //DATUAK RANDOMIZATU
            Randomize randomFilter = new Randomize();
            randomFilter.setRandomSeed(42);
            randomFilter.setInputFormat(data);
            Instances randomData = Filter.useFilter(data, randomFilter);

            //TEST MULTZOA LORTU
            RemovePercentage removeFilter = new RemovePercentage();
            removeFilter.setPercentage(66);
            removeFilter.setInputFormat(randomData);
            Instances test = Filter.useFilter(randomData, removeFilter);
            test.setClassIndex(data.numAttributes() - 1);
            System.out.println("Test instantziak: " + test.numInstances());

            //TRAIN MULTZOA LORTU
            removeFilter.setInvertSelection(true);
            removeFilter.setInputFormat(randomData);
            Instances train = Filter.useFilter(randomData, removeFilter);
            train.setClassIndex(data.numAttributes() - 1);
            System.out.println("Train instantziak: " + train.numInstances());

            //TEST GORDE

            datuakGorde(testPath,test);

            //StringToWordVector APLIKATU
            File hiztegia = new File(hiztegiPath); //HIZTEGIAREN FITXATEGIA SORTU
            Instances trainBoW= stringToWordVector(train, hiztegia); //TRAIN MULTZOAREN HIZTEGIA SORTU

            //NonSparse APLIKATU (MATRIZE BITARRA: 0 EZ BADO ETA 1 BALDIN BADAGO)
            trainBoW = nonSparse(trainBoW);

            //TrainBOW GORDE

            trainBoW = reorder(trainBoW);

            //BoW ARFF-AN GORDE
            datuakGorde(trainBoWArffPath, trainBoW);

            ezabatuUselessAttributes(trainBoWArffPath);

            //FSS hiztegia sortu eta gorde
            source = new ConverterUtils.DataSource(trainBoWArffPath);
            Instances trainBow = source.getDataSet();
            trainBow.setClassIndex(trainBow.numAttributes()-1);
            HashMap<String, Integer> hiztegiaFinal = hiztegiaSortu("hiztegia.txt",trainBow);
            hiztegiaGorde(hiztegiaFinal,"hiztegia.txt",trainBoW);

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

    private static Instances stringToWordVector(Instances train, File hiztegia) throws Exception {

        StringToWordVector stringToWordVector= new StringToWordVector();
        stringToWordVector.setLowerCaseTokens(true); //MAYUS ETA MINUS ARTEKO BEREIZKETARIK EZ TRUE BADAGO
        stringToWordVector.setIDFTransform(false);
        stringToWordVector.setTFTransform(false);
        stringToWordVector.setDictionaryFileToSaveTo(hiztegia); //HIZTEGIA GORDETZEKO FITXATEGIA EZARRI
        stringToWordVector.setWordsToKeep(1500);  //ATRIBUTU KOPURUA (BEKTOREAREN DIMENTSIOA), MAIZTASUN HANDIENEKO X ATRIBUTUAK GORDETZEN DITU
        stringToWordVector.setOutputWordCounts(false); //MATRIZE BITARRA: FALSE ETA MATRIZEA MAIZTASUNEKIN: TRUE
        stringToWordVector.setInputFormat(train);
        Instances trainBOW= Filter.useFilter(train,stringToWordVector);
        return trainBOW;
    }

    private static Instances nonSparse(Instances data) throws Exception{

        SparseToNonSparse filterNonSparse = new SparseToNonSparse();
        filterNonSparse.setInputFormat(data);
        Instances nonSparseData = Filter.useFilter(data,filterNonSparse);
        return nonSparseData;
    }

    private static void ezabatuUselessAttributes(String path) throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(path);
        Instances data = source.getDataSet();
        data.setClassIndex(data.numAttributes()-1);

        RemoveByName remove = new RemoveByName();
        remove.setExpression(".*[a-zA-Z0-9]+.*");
        remove.setInvertSelection(true);
        remove.setInputFormat(data);
        data = Filter.useFilter(data, remove);

        ConverterUtils.DataSink ds = new ConverterUtils.DataSink(path);
        ds.write(data);
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