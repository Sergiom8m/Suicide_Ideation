package Iragarpena;

import weka.classifiers.Classifier;

import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;

import java.io.*;

public class Iragarpenak {

    /**
     *<h3>Aurre-baldintzak:</h3>
     * <ol>
     *     <li> parametro bezala erabiliko den .model fitxategia</li>
     *     <li> parametro bezala testFSS datu multzoaren .arff fitxategia</li>
     *     <li> parametro bezala emaitzak gordetzeko .txt fitxategia</li>
     *</ol>
     *
     * <h3>Ondorengo-baldintzak:</h3>
     * <ol>
     *      <li> fitxategi bezala 3. parametroan adierazitako .txt fitxategia</li>
     *</ol>
     * <h3>Exekuzio-adibidea:</h3>
     *      java -jar Iragarpenak.jar path/to/Modeloa.model path/to/testFSS.arff path/to/devFSS.arff path/to/irteerako/TestPredictions.txt
     */
    public static void main (String[] args){
        try {
            iragarpen(args[0], args[1], args[2]);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("\nZeozer gaizki sartu da. Exekuzio adibidea: \n" +
                    "\t\t\tjava -jar Iragarpenak.jar path/to/Modeloa.model path/to/testFSS.arff path/to/devFSS.arff path/to/irteerako/TestPredictions.txt\n\n");
        }

    }

    /**
     * test instantiza multzo bat eta eredu sailkatzaile bat jasota, sailkatzaileak test multzoan dauden instantzien iragarpenak
     * egingo ditu
     * @param modelPath eredu sailkatailea dagoen path-a
     * @param testPath test instantzia multzoaren path-a
     * @param iragarpenakPath egindako iragarpenak egingo diren fitxategiaren path-a
     * @throws Exception
     */
    public static void iragarpen(String modelPath,String testPath, String iragarpenakPath) throws Exception{

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
        int inst = test.numInstances();
        //LORTUTAKO PARTIKETAREN INSTANTZIAK IRAGARRI
        for (int j = 0; j<inst; j++){
            double pred = randomForest.classifyInstance(test.instance(j));
            pw.println((j+1)+". INSTANTZIAREN IRAGARPENA    --->   "+ test.classAttribute().value((int) pred));
            if(test.instance(j).classValue()==pred){
                kont=kont+1;
            }

        }

        int numInst = test.numInstances();

        pw.println("\n\n//////////////////////////////////////////////////////////");
        pw.println("INSTANTZIA TOTALAK: " + numInst);
        if(!test.instance(0).classIsMissing()){
            pw.println("ASMATUTAKOAK: " + kont);
            pw.println("ASMATZE TASA: " + (double) kont/numInst);
        }
        pw.println("//////////////////////////////////////////////////////////");

        System.out.println("INSTANTZIA TOTALAK: " + numInst);
        if(!test.instance(0).classIsMissing()) {
            System.out.println("ASMATUTAKOAK: " + kont);
            System.out.println("ASMATZE TASA: " + (double) kont / numInst + "\n");
        }

        pw.close();
    }
}
