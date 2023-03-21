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
    public static void main(String cleanDataArffPath,int errepresentazioBektoriala,int sparse, String hiztegiPath, String trainBoWArffPath) {

        try{
            //String[] args = new String[]{"Suicide_Detection.arff", "hiztegia.txt", "trainBOW.arff", "test.arff"};
            /*
                0. parametroa: .arff fitxategia
                1. parametroa: 0 --> bow, 1 --> tfidf
                2. parametroa: 0 --> sparse, 1 --> nonsparse
                3. parametroa: Hiztegia gordetzeko .txt
                4. parametroa: Train-ren BOW gordetzeko. arff
                5. parametroa: test.arff
             */


            //DATUAK LORTU
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(cleanDataArffPath);
            Instances train = source.getDataSet();
            train.setClassIndex(train.numAttributes()-1);


            //IZENA ALDATU BEHAR ZAIO KLASEARI StringToWordVector EGIN AHAL IZATEKO
            train.renameAttribute(train.numAttributes()-1, "klasea");
            train.setClassIndex(train.numAttributes()-1);


            //StringToWordVector APLIKATU
            File hiztegia = new File(hiztegiPath); //HIZTEGIAREN FITXATEGIA SORTU
            Instances trainBoW= stringToWordVector(train, hiztegia,errepresentazioBektoriala); //TRAIN MULTZOAREN HIZTEGIA SORTU

            // SPARSE/NONSPARSE
            if(sparse==1){
                trainBoW = SparseToNonSparse(trainBoW);
            }

            //TrainBOW GORDE
            trainBoW = reorder(trainBoW);

            trainBoW = ezabatuUselessAttributes(trainBoW);

            //BoW ARFF-AN GORDE
            datuakGorde(trainBoWArffPath, trainBoW);


            //HIZTEGIA EGOKITU
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
        stringToWordVector.setWordsToKeep(3500);    //defektuz 1000 TODO 2000
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

    private static Instances ezabatuUselessAttributes(Instances data) throws Exception {
        RemoveByName remove = new RemoveByName();
        remove.setExpression(".*[a-zA-Z0-9]+.*");
        remove.setInvertSelection(true);
        remove.setInputFormat(data);
        data = Filter.useFilter(data, remove);

        return data;
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