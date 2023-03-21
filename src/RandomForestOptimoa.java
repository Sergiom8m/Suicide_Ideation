import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

/**
 * Klase hau, sailkatzailearen parametro ekorketa egiteko eraibliko da.
 * Erabiliko den datu sortan, klase atributua azken atributuan doa.
 * Erabiliko den sailkatzailea 'RandomForest' sailkatzailea da.
 *
 * Hau egiteko, oso komenigarria da RandomForest nola funtzionatzen duen jakitea.
 * RandomForest sailkatzaileak erabiltzen dituen atributuak:
 * <ul>
 *     <li>Zuhaitz bakoitzaren sakonera, <b>setMaxDepth</b>: RandomForest erabiltzen dituen zuhaitzen sakonera ezarritzen duen parametroa
 *     da. 'int' motako balioak har ditzake. 0 balioa ematen bazaio, limiterik gabeko zuhaitzak egingo ditu</li>
 *     <li>Zuhaitz bakoitza eraikitzeko erabiltzen duen atributu kopurua, <b>setNumFeatures</b>: "Azalpen bideoa" bideoan,
 *     atributu kopuruaren erro karratua zenbaki egoki bat dela adierazten da.</li>
 *     <li><b>setBagSizePercent</b> (Bootstraping + Aggregation): Bootstraping egitean hartutako lagin bakoitza zenbatekoa
 *     den adierazteko erabili da. Weka-k eskaintzen duen metodoa </li>
 *     <li><b>setNumIterations</b>: Bootstraping egitean (laginak jaso), hartuko diren lagin kopurua. >0 izan behar da</li>
 *     <li><b>setNumDecimalPlaces??</b></li>
 * </ul>
 *
 * <p><a href="https://weka.sourceforge.io/doc.dev/weka/classifiers/trees/RandomForest.html">RandomForest (weka-doc)</a></p>
 * <p><a href="https://youtu.be/v6VJ2RO66Ag">Azalpen bideoa</a></p>
 * <p><a href="https://www.simplilearn.com/tutorials/machine-learning-tutorial/bagging-in-machine-learning#:~:text=Bagging%2C%20also%20known%20as%20Bootstrap,variance%20of%20a%20prediction%20model.">Bagging</a></p>
 * <p><a href="https://en.wikipedia.org/wiki/Bootstrap_aggregating">Bootstrap aggregating (Wikipedia)</a></p>
 *
 * <p>Ebaluaketa metodoa: k-fCV</p>
 *
 * <p>Optimizatzen saiatuko garen metrika <b>f-measure</b> izango da. </p>
 */
public class RandomForestOptimoa {

    private static String sourceArff;
    private static String storeDir;
    private static Instances data;
    private static String date;
    private static RandomForest randomForest_optimo;
    private static int numfolds;
    private static String ev_method;
    private static int min_depth; // probar siempre con 0
    private static int max_depth;
    private static int min_num_features; // >20 segun resultados
    private static int max_num_features; // En el video de la descrición de la clase, se menciona que la raiz cuadrada de la cantidad de atributos es un buen número. >25
    private static int min_num_iterations;
    private static int max_num_iterations;
    private static boolean bukle_mota; // true --> harabiatua; false --> serializatua
    private static double best_fMeasure;
    // private static int bagging_percent; //TODO
    //private static HashMap<Integer, int[]> depth_values;
    //private static HashMap<Integer, int[]> num_features_values;
    //private static HashMap<Integer, int[]> num_iterations_values;


    /**
     *
     * @param args
     * <ul>
     *     <li>args[0]: DataSet</li>
     *     <li>args[1]: emaitzen direktorioa</li>
     * </ul>
     */
    public static int[] main(String[] args) {
        sourceArff = args[0];
        storeDir = args[1];
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd-Hms");
        date = simpleDateFormat.format(new Date());

        datuakKargatu(args);
        ezarpenak();
        int parametroak[]=ekorketa();
        emaitzak();
        return parametroak;
    }
    private static void datuakKargatu(String[] args){
        try{
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(sourceArff);
            data = source.getDataSet();
            data.setClassIndex(data.numAttributes()-1);
            System.out.println("Parametro ekorketa egiteko eraibliko diren instantzia kopurua: " + data.numInstances());
        }
        catch (Exception e){
            System.out.println("Errorea datuak kargatzean");
        }
    }

    private static void ezarpenak(){
        numfolds = 2;
        ev_method = numfolds + "-fCV";
        min_depth = 10;
        max_depth = 15;
        min_num_features = 25;
        max_num_features = (int) Math.sqrt(data.numAttributes());
        min_num_iterations = 25;
        max_num_iterations = 125;
        bukle_mota = true;
    }

    private static int[] ekorketa(){
        int ema[] = new int[3];
        try{
            long hasiera = System.currentTimeMillis();

            //if (bukle_mota) ekoreketaHarabiatua();
            //else ekorketaSerializatua();
            ema = ekoreketaHarabiatua();
            long amaiera = System.currentTimeMillis();
            double denbora = (double) ((amaiera - hasiera)/1000);
            System.out.println("\nExekuzio denbora: " + denbora + " segundo");
        } catch (Exception e){
            System.out.println("Errorea ekorketa egiterakoan");
        }
        return ema;
    }

    private static int[] ekoreketaHarabiatua(){
        int best_depth = 0;
        int best_num_features = 0;
        best_fMeasure = 0;
        int best_num_iterations = 0;
        int iteration = 1;
        try{
            RandomForest randomForest = new RandomForest();

            randomForest.setNumExecutionSlots(Runtime.getRuntime().availableProcessors()); // prozesadore gehiago erabili dezan (buklerak azkarrago)
            for (int depth = 0; depth <= max_depth; depth++) {
                for (int num_features = min_num_features; num_features <= max_num_features; num_features+=2){
                    for (int num_iterations = min_num_iterations; num_iterations <= max_num_iterations; num_iterations+=25){
                        System.out.println("------ " + iteration + ". iterazioa ------");
                        System.out.println("depth: " + depth);
                        System.out.println("num_features: " + num_features);
                        System.out.println("num_iterations: " + num_iterations);
                        randomForest.setMaxDepth(depth);
                        randomForest.setNumFeatures(num_features);
                        randomForest.setNumIterations(num_iterations);
                        //randomForest.setNumDecimalPlaces();
                        //randomForest.setBagSizePercent();
                        randomForest.buildClassifier(data);

                        Evaluation evaluation = new Evaluation(data);
                        evaluation.crossValidateModel(randomForest, data, numfolds, new Random(1));

                        double fMeasure = evaluation.weightedFMeasure();
                        System.out.println(iteration + ". iterazioko f-measure: " + fMeasure);
                        if (fMeasure > best_fMeasure) {
                            best_fMeasure = fMeasure;
                            best_depth = depth;
                            best_num_features = num_features;
                            best_num_iterations = num_iterations;
                        }
                        iteration++;
                    }
                }
                if (depth == 0) depth = min_depth-1;
            }
            optimoaGorde(best_depth, best_num_features, best_num_iterations);
        } catch (Exception e){
            e.printStackTrace();
        }
        return (new int[]{best_depth, best_num_features, best_num_iterations});
    }

    private static void ekorketaSerializatua(){
        try{
            RandomForest randomForest = new RandomForest();
            int best_depth = 0;
            int best_num_features = 0;
            best_fMeasure = 0;
            int best_num_iterations = 0;
            int iteration = 1;

            for (int depth = 0; depth <= max_depth; depth++) {
                System.out.println("--- depth " + depth);
                randomForest.setMaxDepth(depth);
                randomForest.buildClassifier(data);

                Evaluation evaluation = new Evaluation(data);
                evaluation.crossValidateModel(randomForest, data, numfolds, new Random());

                double fMeasure = evaluation.weightedFMeasure();
                System.out.println("fMeasure " + fMeasure);
                if (fMeasure > best_fMeasure){
                    best_fMeasure = fMeasure;
                    best_depth = depth;
                }
            }

            best_fMeasure = 0;

            for (int num_features = 0; num_features <= max_num_features; num_features+=2) {
                System.out.println("--- numFeatures " + num_features);
                randomForest.setNumFeatures(num_features);
                randomForest.buildClassifier(data);

                Evaluation evaluation = new Evaluation(data);
                evaluation.crossValidateModel(randomForest, data, numfolds, new Random(1));

                double fMeasure = evaluation.weightedFMeasure();
                System.out.println("fMeasure " + fMeasure);
                if (fMeasure > best_fMeasure) {
                    best_fMeasure = fMeasure;
                    best_num_features = num_features;
                }
            }

            best_fMeasure = 0;

            for (int num_iterations = 25; num_iterations <= max_num_iterations; num_iterations+=25){
                System.out.println("--- numItertions " + num_iterations);
                randomForest.setNumIterations(num_iterations);
                randomForest.buildClassifier(data);

                Evaluation evaluation = new Evaluation(data);
                evaluation.crossValidateModel(randomForest, data, numfolds, new Random(1));

                double fMeasure = evaluation.weightedFMeasure();
                System.out.println("fMeasure " + fMeasure);
                if (fMeasure > best_fMeasure) {
                    best_fMeasure = fMeasure;
                    best_num_iterations = num_iterations;
                }
            }

            randomForest.setMaxDepth(best_depth);
            randomForest.setNumIterations(best_num_iterations);
            randomForest.setNumFeatures(best_num_features);
            randomForest.buildClassifier(data);
            Evaluation evaluation = new Evaluation(data);
            evaluation.crossValidateModel(randomForest, data, numfolds, new Random(1));
            best_fMeasure = evaluation.weightedFMeasure();
            optimoaGorde(best_depth, best_num_features, best_num_iterations);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void optimoaGorde(int best_depth, int best_num_features, int best_num_iterations){
        try{
            randomForest_optimo = new RandomForest();
            randomForest_optimo.setMaxDepth(best_depth);
            randomForest_optimo.setNumFeatures(best_num_features);
            randomForest_optimo.setNumIterations(best_num_iterations);
            randomForest_optimo.buildClassifier(data);
            SerializationHelper.write(storeDir + "\\" + date +"RF_optimoa.model", randomForest_optimo);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void emaitzak(){
        try{
            FileWriter myWriter = new FileWriter(storeDir + "\\" + date +"RF_optimo_ema.txt");
            myWriter.write(date + "\n");
            myWriter.write(".arff file " + sourceArff + " with " + data.numInstances() + " instances \n");
            myWriter.write("Evaluazio eskema: " + ev_method + "\n");
            myWriter.write("Ebaluazio metrika 'fMeasure': " + best_fMeasure + "\n");
            if (bukle_mota) myWriter.write("Bukle harabiatua\n");
            else myWriter.write("Bukle serializatua\n");
            myWriter.write("Random Forest optimum parameters:\n");
            myWriter.write(" · MaxDepth: " + randomForest_optimo.getMaxDepth() + " (" + min_depth + " --> " + max_depth + ")" + "\n");
            myWriter.write(" · NumFeatures: " + randomForest_optimo.getNumFeatures() + " (" + min_num_features + " --> " + max_num_features + ")" + "\n");
            myWriter.write(" · NumIterations: " + randomForest_optimo.getNumIterations() + " (" + min_num_iterations + " --> " + max_num_iterations + ")" + "\n");
            myWriter.write(" . BagSizePrecent: " + randomForest_optimo.getBagSizePercent() + "\n");
            myWriter.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}