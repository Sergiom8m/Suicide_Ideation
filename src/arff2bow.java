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

public class arff2bow {
    public static void main(String[] args) {
        try{
            args= new String[]{"Suicide_Detection.arff","hiztegia.txt","trainBOW.arff", "dev.arff"};
            /** ARRF-TIK --> BOW/TFIDF LORTU
             *  0. parametroa: .arff fitxategia
             *  1. parametroa: Hiztegia gordetzeko .txt
             *  2. parametroa: Train-ren BOW gordetzeko. arff
             *  3. parametroa: dev.arff
             */

            //1. DATUAK LORTU
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(args[0]);
            Instances data = source.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);
            //izena aldatu behar zaio class-ari, StringToWordVector egiteko
            data.renameAttribute(data.numAttributes() - 1, "klasea");
            data.setClassIndex(data.numAttributes() - 1);

            //2. HOLD OUT --> TRAIN ETA TEST
            Randomize randomFilter = new Randomize();
            randomFilter.setRandomSeed(42);
            randomFilter.setInputFormat(data);
            Instances randomData = Filter.useFilter(data, randomFilter);

            RemovePercentage removeFilter = new RemovePercentage();
            removeFilter.setPercentage(66);
            removeFilter.setInputFormat(randomData);
            Instances dev = Filter.useFilter(randomData, removeFilter);
            dev.setClassIndex(data.numAttributes() - 1);
            System.out.println("Test instantziak: " + dev.numInstances());

            removeFilter.setInvertSelection(true);
            removeFilter.setInputFormat(randomData);
            Instances train = Filter.useFilter(randomData, removeFilter);
            train.setClassIndex(data.numAttributes() - 1);
            System.out.println("Train instantziak: " + train.numInstances());

            //3. TRAIN ETA DEV GORDE (aunk creo que no hacen falta)
            datuakGorde(args[3],dev);


            //5. STRINGTOWORDVECTOR APLIKATU
            File hiztegia = new File(args[1]); //hiztegia gordetzeko//TODO PONIA 2 PERO ES EL DE ABAJO CREO
            Instances trainBOW= stringToWordVector(train,hiztegia);


            // 7. TRAINBOW GORDE
            datuakGorde(args[2], trainBOW); //TODO PONIA 4 PERO DABA ERROR??




        }catch (Exception e){
            e.printStackTrace();
        }
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