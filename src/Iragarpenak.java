import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceWithMissingValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Iragarpenak {
    public static void main(String modelPath,String testPath, String iragarpenakPath){
        try{
            Classifier randomForest= (Classifier) SerializationHelper.read(modelPath);

            ConverterUtils.DataSource source = new ConverterUtils.DataSource(testPath);
            Instances test = source.getDataSet();
            test.setClassIndex(test.numAttributes() - 1);


            //TEST BLIND EGIN
            ReplaceWithMissingValue replace=new ReplaceWithMissingValue();
            replace.setIgnoreClass(true);
            replace.setProbability(1);
            replace.setAttributeIndicesArray(new int[]{test.classIndex()});
            replace.setInputFormat(test);
            Instances testBlind= Filter.useFilter(test, replace);

            ArffSaver saver = new ArffSaver();
            saver.setInstances(testBlind);
            saver.setFile(new File("test_blind.arff"));
            saver.writeBatch();


            FileWriter file = new FileWriter(iragarpenakPath);
            PrintWriter pw = new PrintWriter(file);

            for(int i=0; i<testBlind.numInstances(); i++){
                double pred = randomForest.classifyInstance(testBlind.instance(i));
                pw.println((i+1)+".instantziaren iragarpena:"+ testBlind.classAttribute().value((int) pred));
            }

            //IRAGARPENEN ASMATZEAK
            int kont=0;
            for(int i=0; i<test.numInstances(); i++){
                //System.out.println((i+1)+".instantziaren klasea:"+ test.instance(i).classValue());
                double pred = randomForest.classifyInstance(testBlind.instance(i));
                //System.out.println((i+1)+".instantziaren iragarpena:"+ pred);
                if(test.instance(i).classValue()==pred){
                    kont=kont+1;
                }
            }
            System.out.println("INSTANTIZIA TOTALAK: "+test.numInstances());
            System.out.println("BAT DATOZ: "+kont);

            pw.append("\n\n\n///////////////////////////////////////////////////////////////////////////////////");
            pw.append("INSTANTIZIA TOTALAK: "+test.numInstances());
            pw.append("BAT DATOZ: "+kont);

            pw.close();

        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
