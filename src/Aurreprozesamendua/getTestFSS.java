package Aurreprozesamendua;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.FixedDictionaryStringToWordVector;
import weka.filters.unsupervised.attribute.Reorder;
import weka.filters.unsupervised.instance.SparseToNonSparse;

import java.io.*;

public class getTestFSS {
    public static void main(String devPath, String devFSSPath) {
        try {

            //TEST FSS EGIN
            ConverterUtils.DataSource source= new ConverterUtils.DataSource(devPath);
            Instances dev = source.getDataSet();
            dev.setClassIndex(dev.numAttributes() - 1);

            Instances devFSS= fixedDictionaryStringToWordVector("hiztegiaFSS.txt",dev);

            devFSS=nonSparse(devFSS);

            devFSS=reorder(devFSS);

            datuakGorde(devFSSPath,devFSS);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private static Instances fixedDictionaryStringToWordVector(String hiztegia, Instances data) throws Exception {
        FixedDictionaryStringToWordVector fixedDict= new FixedDictionaryStringToWordVector();
        fixedDict.setLowerCaseTokens(true);
        fixedDict.setIDFTransform(false);
        fixedDict.setTFTransform(false);
        fixedDict.setDictionaryFile(new File(hiztegia));
        //fixedDict.setWordsToKeep(100);
        fixedDict.setOutputWordCounts(false);
        fixedDict.setInputFormat(data);
        Instances testBowFSS= Filter.useFilter(data,fixedDict);
        return testBowFSS;
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
    private static Instances nonSparse(Instances data) throws Exception{
        SparseToNonSparse filterNonSparse = new SparseToNonSparse();
        filterNonSparse.setInputFormat(data);
        Instances nonSparseData = Filter.useFilter(data,filterNonSparse);
        return nonSparseData;
    }


}