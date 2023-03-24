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

        iragarpenak(args[0], args[1], args[2]);

    }
    public static void iragarpenak(String modelPath,String testPath, String iragarpenakPath){
        try{

            System.out.println("IRAGARPENAK EGINGO DIRA" + "\n");

            //PARAMETRO EKORKETAN LORTUTAKO SAILKATZAILE OPTIMOA KARGATU
            Classifier randomForest= (Classifier) SerializationHelper.read(modelPath);

            //IRAGARPENAK EGITEKO ERRESERBATUTAKO TEST KARGATU (ERABILTZEN DEN LEHENENGO ALDIA)
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(testPath);
            Instances test = source.getDataSet();
            test.setClassIndex(test.numAttributes() - 1);

            FileWriter file = new FileWriter(iragarpenakPath);
            PrintWriter pw = new PrintWriter(file);

            Double asmatze_tasa_BB = 0.0;

            int i = 1;
            while (i<=50){

                int kont = 0; //ASMATUTAKO KOP

                //IRAGARPENAK EGITEKO INSTANTZIEN ARTEAN BATZUK HARTUKO DIRA
                Resample r = new Resample();
                r.setNoReplacement(true);
                r.setRandomSeed(i);
                r.setSampleSizePercent(30);
                r.setInputFormat(test);
                Instances partition = Filter.useFilter(test, r);

                //LORTUTAKO PARTIKETAREN INSTANTZIAK IRAGARRI
                for (int j = 0; i<partition.numInstances(); j++){

                    double pred = randomForest.classifyInstance(partition.instance(j));
                    pw.println((j+1)+"INSTANTZIAREN IRAGARPENA:"+ test.classAttribute().value((int) pred));

                    if(partition.instance(j).classValue()==pred){
                        kont=kont+1;
                    }

                }
                System.out.println("ITERAZIOA: " + i + "\n");
                System.out.println("INSTANTZIA TOTALAK: " + partition.numInstances() + "\n");
                System.out.println("ASMATUTAKOAK: " + kont);
                System.out.println("ASMATZE TASA: " + (kont/partition.numInstances()) + "\n");


                asmatze_tasa_BB = asmatze_tasa_BB + kont/partition.numInstances();
                i++;

            }
            System.out.println("ASMATZE TASA BATEZ BESTE:" + asmatze_tasa_BB/i + "\n");


        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
