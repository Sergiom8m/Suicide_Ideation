import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.FixedDictionaryStringToWordVector;
import weka.filters.unsupervised.attribute.Reorder;
import weka.filters.unsupervised.instance.SparseToNonSparse;

import java.io.*;
import java.util.HashMap;

public class getDevFSS {
    public static void main(String trainFSSPath, String devPath, String devFSSPath) {
        try {




            //TODO : ESTO IRIA EN INFO GAIN

            ConverterUtils.DataSource source = new ConverterUtils.DataSource(trainFSSPath);
            Instances trainFSS = source.getDataSet();
            trainFSS.setClassIndex(trainFSS.numAttributes() - 1);


            //FSS hiztegia sortu eta gorde
            HashMap<String, Integer> hiztegia = hiztegiaSortu("hiztegia.txt",trainFSS);
            hiztegiaGorde(hiztegia,"hiztegiaFSS.txt",trainFSS);

            // TODO : HASTA AQUI

            //TEST FSS EGIN
            source= new ConverterUtils.DataSource(devPath);
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
}