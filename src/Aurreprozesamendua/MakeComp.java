package Aurreprozesamendua;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.FixedDictionaryStringToWordVector;
import weka.filters.unsupervised.attribute.Reorder;
import weka.filters.unsupervised.instance.SparseToNonSparse;

import java.io.*;

public class MakeComp {

    public static void main (String[] args){

        main(args[0], args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));

    }
    public static void main(String inputPath, String outputFSSPath,int errepresentazioBektoriala,int sparse) {
        try {

            //TEST FSS EGIN
            ConverterUtils.DataSource source= new ConverterUtils.DataSource(inputPath);
            Instances data = source.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);

            //IZENA ALDATU BEHAR ZAIO KLASEARI StringToWordVector EGIN AHAL IZATEKO
            data.renameAttribute(data.numAttributes()-1, "klasea");
            data.setClassIndex(data.numAttributes()-1);

            Instances testFSS= fixedDictionaryStringToWordVector("hiztegiaFSS.txt",data,errepresentazioBektoriala);


            // SPARSE/NONSPARSE
            if(sparse==1){
                testFSS = SparseToNonSparse(testFSS);
            }

            testFSS=reorder(testFSS);

            datuakGorde(outputFSSPath,testFSS);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private static Instances fixedDictionaryStringToWordVector(String hiztegia, Instances data, int bektorea) throws Exception {
        FixedDictionaryStringToWordVector fixedDict= new FixedDictionaryStringToWordVector();
        if(bektorea==1){
            fixedDict.setOutputWordCounts(true);
            fixedDict.setIDFTransform(true);
            fixedDict.setTFTransform(true);
        }
        else{
            fixedDict.setOutputWordCounts(false);
        }
        fixedDict.setAttributeIndices("first-last");
        fixedDict.setLowerCaseTokens(true);
        fixedDict.setDictionaryFile(new File(hiztegia));
        fixedDict.setInputFormat(data);
        Instances dataFSS= Filter.useFilter(data,fixedDict);
        return dataFSS;
    }

    private static Instances reorder(Instances test) throws Exception {
        Reorder filterR = new Reorder();
        filterR.setAttributeIndices("2-"+ test.numAttributes()+",1"); //2-atributu kop, 1.  2-atributu kop bitarteko atributuak goian jarriko dira eta 1 atributua (klasea dena) amaieran.
        filterR.setInputFormat(test);
        test = Filter.useFilter(test,filterR);
        return test;
    }

    private static void datuakGorde(String path, Instances data) throws Exception {
        ArffSaver s = new ArffSaver();
        s.setInstances(data);
        s.setFile(new File(path));
        s.writeBatch();
    }
    private static Instances SparseToNonSparse(Instances data) throws Exception{
        SparseToNonSparse filterNonSparse = new SparseToNonSparse();
        filterNonSparse.setInputFormat(data);
        Instances nonSparseData = Filter.useFilter(data,filterNonSparse);
        return nonSparseData;
    }


}