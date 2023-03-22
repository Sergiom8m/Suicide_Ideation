package Aurreprozesamendua;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.attribute.RemoveByName;
import weka.filters.unsupervised.attribute.Reorder;

import java.io.*;
import java.util.HashMap;

public class fssInfoGain {
    public static void fssInfoGain(String trainBowPath, String FSSArffPath) {
        try {
            /*
               0. parametroa: .arff fitxategia (atributu guztiekin)
               1. parametroa: TrainBOW-ren bertsio berria gordetzeko. arff
             */

            //DATUAK LORTU
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(trainBowPath);
            Instances train = source.getDataSet();
            train.setClassIndex(train.numAttributes() - 1);

            System.out.println("ATRIBUTUKO`PURUAAAAAAAAAAAAAA"+train.numAttributes());
            train = ezabatuUselessAttributes(train);

            AttributeSelection filterSelect = new AttributeSelection();
            InfoGainAttributeEval evalInfoGain = new InfoGainAttributeEval();
            Ranker ranker = new Ranker();
            ranker.setNumToSelect(2000);
            ranker.setThreshold(0.001);
            filterSelect.setInputFormat(train);
            filterSelect.setEvaluator(evalInfoGain);
            filterSelect.setSearch(ranker);
            Instances trainFSS = Filter.useFilter(train, filterSelect);



            //DATUAK GORDE
            datuakGorde(FSSArffPath, trainFSS);


            // HIZTEGIA SORTU ETA GORDE
            HashMap<String, Integer> hiztegia = hiztegiaSortu("hiztegia.txt",trainFSS);
            hiztegiaGorde(hiztegia,"hiztegiaFSS.txt",trainFSS);

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

    private static Instances ezabatuUselessAttributes(Instances data) throws Exception {
        RemoveByName remove = new RemoveByName();
        remove.setExpression(".*[a-zA-Z0-9]+.*");
        remove.setInvertSelection(true);
        remove.setInputFormat(data);
        data = Filter.useFilter(data, remove);

        return data;
    }
}