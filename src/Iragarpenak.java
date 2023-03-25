import weka.classifiers.Classifier;
import weka.classifiers.rules.PART;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceWithMissingValue;
import weka.filters.unsupervised.instance.Resample;

import java.io.*;

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

            int kont = 0; //ASMATUTAKO KOP

            //LORTUTAKO PARTIKETAREN INSTANTZIAK IRAGARRI
            for (int j = 0; j<test.numInstances(); j++){

                double pred = randomForest.classifyInstance(test.instance(j));
                pw.println((j+1)+"INSTANTZIAREN IRAGARPENA:"+ test.classAttribute().value((int) pred));

                if(test.instance(j).classValue()==pred){
                    kont=kont+1;
                }

            }
            int numInst = test.numInstances();
            System.out.println("INSTANTZIA TOTALAK: " + numInst);
            System.out.println("ASMATUTAKOAK: " + kont);
            System.out.println("ASMATZE TASA: " + (double) kont/numInst + "\n");


        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }
}
