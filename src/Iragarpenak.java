import weka.classifiers.Classifier;
import weka.classifiers.rules.PART;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceWithMissingValue;
import weka.filters.unsupervised.instance.Resample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Iragarpenak {

    public static void main (String[] args){

        main(args[0], args[1], args[2]);

    }
    public static void main(String modelPath,String testPath, String iragarpenakPath){
        try{

            Classifier randomForest= (Classifier) SerializationHelper.read(modelPath);

            ConverterUtils.DataSource source = new ConverterUtils.DataSource(testPath);
            Instances test = source.getDataSet();
            test.setClassIndex(test.numAttributes() - 1);

            /*
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
            */

            FileWriter file = new FileWriter(iragarpenakPath);
            PrintWriter pw = new PrintWriter(file);

            Double asm_tas_sum = 0.0;

            for (int i=0 ; i<5; i++){

                int kont = 0; //Aciertos

                Resample r = new Resample();
                r.setNoReplacement(true);
                r.setRandomSeed(i);
                r.setSampleSizePercent(60);
                r.setInputFormat(test);
                Instances partition = Filter.useFilter(test, r);

                for (i = 0; i<partition.numInstances(); i++){
                double pred = randomForest.classifyInstance(partition.instance(i));
                pw.println((i+1)+".instantziaren iragarpena:"+ test.classAttribute().value((int) pred));

                if(partition.instance(i).classValue()==pred){
                    kont=kont+1;
                }
                System.out.println("INSTANTIZIA TOTALAK: "+ partition.numInstances());
                System.out.println("BAT DATOZ: "+ kont);
                System.out.println("Asmatze tasa: " + kont/partition.numInstances());


                asm_tas_sum = asm_tas_sum + kont/partition.numInstances();

            }

                System.out.println("AMATZE TASA BB:" + asm_tas_sum/i);



            }


        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
