import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.FixedDictionaryStringToWordVector;
import weka.filters.unsupervised.attribute.Reorder;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.filters.unsupervised.instance.SparseToNonSparse;

import java.io.File;

public class bateragarriaEgin {
    public static void main(String[] args) {
        try {
            args = new String[]{"dev.arff", "hiztegia.txt", "devBow.arff"};
            /** TEST MULTZOA BATERAGARRIA EGIN
             *  0. parametroa: dev.arff fitxategia
             *  1. parametroa: Hiztegia.txt
             *  2. parametroa: devBow. arff
             *  3. parametroa: dev.arff
             */

            //1. DEV LORTU
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(args[0]);
            Instances dev = source.getDataSet();
            dev.setClassIndex(dev.numAttributes() - 1);

            FixedDictionaryStringToWordVector fixedDict= new FixedDictionaryStringToWordVector();
            fixedDict.setLowerCaseTokens(true);
            fixedDict.setIDFTransform(false);
            fixedDict.setTFTransform(false);
            fixedDict.setDictionaryFile(new File(args[1]));
            //fixedDict.setWordsToKeep(100);
            fixedDict.setOutputWordCounts(false);
            fixedDict.setInputFormat(dev);
            Instances devBow= Filter.useFilter(dev,fixedDict);

            devBow = nonSparse(devBow);

            Reorder reorder = new Reorder();
            reorder.setAttributeIndices("2-last,1");        //klasea azken atributua izateko
            reorder.setInputFormat(devBow);
            devBow = Filter.useFilter(devBow, reorder);
            datuakGorde(args[2],devBow);


        }catch (Exception e){
            e.printStackTrace();
        }
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