package Ebaluazioa;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;

import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.Utils;
import weka.core.converters.ConverterUtils;

import weka.filters.Filter;
import weka.filters.unsupervised.instance.Resample;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Random;

public class Ebaluazioa {


    /**
     *<h3>Aurre-baldintzak:</h3>
     * <ol>
     *     <li> parametro bezala trainFSS datu multzoaren .arff fitxategia</li>
     *     <li> parametro bezala devFSS datu multzoaren .arff fitxategia</li>
     *     <li> parametro bezala trainFSS + devFSS datu multzoaren .arff fitxategia</li>
     *     <li> parametro bezala RandomForest-aren NumFeatures parametroa</li>
     *     <li> parametro bezala RandomForest-aren NumIterations parametroa</li>
     *     <li> parametro bezala RandomForest-aren BagSizePercent parametroa</li>
     *     <li> parametro bezala RandomForest-aren MaxDepth parametroa</li>
     *     <li> parametro bezala emaitzak gordetzeko .txt fitxategia</li>
     *</ol>
     *
     * <h3>Ondorengo-baldintzak:</h3>
     * <ol>
     *      <li> fitxategi bezala 8. parametroan adierazitako .txt fitxategia</li>
     *</ol>
     * <h3>Exekuzio-adibidea:</h3>
     *      java -jar ParametroEkorketa.jar path/to/trainFSS.arff path/to/devFSS.arff path/to/dataFSS.arff 	"NumFeatures" "NumIterations" "BagSizePercent" "MaxDepth" path/to/irteerako/EvaluationAlgorithm.txt
     */

    public static void main(String[] args) {

        try {
            ebaluazioa( args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), args[5], args[6]);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("\nZeozer gaizki sartu da. Exekuzio adibidea: \n" +
                    "\t\t\tjava -jar ParametroEkorketa.jar path/to/trainFSS.arff path/to/devFSS.arff path/to/dataFSS.arff \t\"NumFeatures\" \"NumIterations\" \"BagSizePercent\" \"MaxDepth\" path/to/irteerako/EvaluationAlgorithm.txt\n\n");
        }

    }

    public static void ebaluazioa(String dataPath, int p1, int p2, int p3, int p4, String emaitzak, String modeloPath) throws Exception { //TODO blind test gehitu?

        int iterazioKop=5;

        System.out.println("TRAIN ETA DEV MULTZOAK ERABILITA SAILKATZAILEA EBALUATUKO DA" + "\n");

        //1. DATUAK KARGATU
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(dataPath);
        Instances data = source.getDataSet();
        data.setClassIndex(data.numAttributes() - 1);

        //2. RANDOM FOREST PARAMETROAK KARGATU
        RandomForest randomForest = new RandomForest();
        randomForest.setNumExecutionSlots(Runtime.getRuntime().availableProcessors());
        randomForest.setNumFeatures(p1);
        randomForest.setNumIterations(p2);
        randomForest.setBagSizePercent(p3);
        randomForest.setMaxDepth(p4);
        randomForest.buildClassifier(data);

        //.MODEL GORDE
        SerializationHelper.write(modeloPath, randomForest);

        FileWriter f = new FileWriter(emaitzak);
        BufferedWriter bf = new BufferedWriter(f);

        //3. EBALUAZIO EZ ZINTZOA
        System.out.println("EBALUAZIO EZ ZINTZOA BURUTZEN..." + "\n");
        bf.append("\n=============================================================\n");
        bf.append("EBALUAZIO EZ ZINTZOA:\n");

        Evaluation evaluation = new Evaluation(data);
        evaluation.evaluateModel(randomForest, data);

        bf.append(evaluation.toSummaryString() + "\n");
        bf.append(evaluation.toClassDetailsString() + "\n");
        bf.append(evaluation.toMatrixString());

        //4. K-FOLD CROSS EBALUAZIOA
        System.out.println("K-FOLD CROSS VALIDATION BURUTZEN..." + "\n");
        bf.append("\n=============================================================\n");
        bf.append("K-FOLD CROSS EBALUAZIOA:\n");

        evaluation = new Evaluation(data);
        evaluation.crossValidateModel(randomForest, data, 10, new Random(1));

        bf.append(evaluation.toSummaryString() + "\n");
        bf.append(evaluation.toClassDetailsString() + "\n");
        bf.append(evaluation.toMatrixString());

        //5. STRATIFIED REPEATED HOLD OUT
        System.out.println("HOLD OUT BURUTZEN..." + "\n");
        bf.append("\n=============================================================\n");
        bf.append("HOLD OUT EBALUAZIOA:\n");

        int klaseMin = Utils.minIndex(data.attributeStats(data.classIndex()).nominalCounts);
        double fMeasureMin = 1;
        String summary = "";
        String classDet = "";
        String matrix = "";

        double[] fmeasureZerrenda = new double[iterazioKop];
        double fmeasureBatu=0;

        for (int i = 0; i < iterazioKop; i++) {

            Resample r = new Resample();
            r.setRandomSeed(i);
            r.setSampleSizePercent(70);
            r.setNoReplacement(true);
            r.setInvertSelection(false);
            r.setInputFormat(data);
            Instances train = Filter.useFilter(data, r);

            r.setRandomSeed(i);
            r.setSampleSizePercent(70);
            r.setNoReplacement(true);
            r.setInvertSelection(true);
            r.setInputFormat(data);
            Instances test = Filter.useFilter(data, r);

            evaluation = new Evaluation(train);

            randomForest = new RandomForest();
            randomForest.setNumExecutionSlots(Runtime.getRuntime().availableProcessors());
            randomForest.setNumFeatures(p1);
            randomForest.setNumIterations(p2);
            randomForest.setBagSizePercent(p3);
            randomForest.setMaxDepth(p4);
            randomForest.buildClassifier(train);

            evaluation.evaluateModel(randomForest, test);

            if (evaluation.fMeasure(klaseMin) < fMeasureMin) {

                summary = evaluation.toSummaryString() + "\n";
                classDet = evaluation.toClassDetailsString() + "\n";
                matrix = evaluation.toMatrixString();

            }
            double fMeasure = evaluation.fMeasure(klaseMin);
            fmeasureZerrenda[i]=fMeasure;
            fmeasureBatu=fmeasureBatu+fMeasure;

            System.out.println(i+".iterazioa fmeasure:"+fMeasure);

        }
        bf.append(summary);
        bf.append(classDet);
        bf.append(matrix);

        bf.append("\n Klase minoritarioaren f-measure batez bestekoa: "+fmeasureBatu/5);

        double desb=0;

        for (int i=0; i<fmeasureZerrenda.length; i++){
            desb = desb+Math.pow((fmeasureZerrenda[i]-fmeasureBatu/iterazioKop), 2);
        }

        desb = desb/(iterazioKop-1);
        desb= Math.sqrt(desb);
        bf.append("\n F-measure desbiderapen: " + desb);

        bf.close();
    }
}
