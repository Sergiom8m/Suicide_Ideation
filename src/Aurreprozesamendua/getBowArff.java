package Aurreprozesamendua;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Reorder;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.filters.unsupervised.instance.Randomize;
import weka.filters.unsupervised.instance.RemovePercentage;
import weka.filters.unsupervised.instance.SparseToNonSparse;

import java.io.File;

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
            Reorder reorder = new Reorder();
            reorder.setAttributeIndices("2-last,1"); //KLASEA AZKEN POSIZIOAN AGER DADIN
            reorder.setInputFormat(trainBoW);
            trainBoW = Filter.useFilter(trainBoW, reorder);

            //BoW ARFF-AN GORDE
            datuakGorde(trainBoWArffPath, trainBoW);

        }catch (Exception e){
            e.printStackTrace();
        }
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


}