import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;

import java.io.File;
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
    private static double exekuzioDenbora;
    private static RandomForest randomForest_optimo;
    private static int numfolds;
    private static String ev_method;
    private static int min_depth; // probar siempre con 0
    private static int jump_depth;
    private static int max_depth;
    private static int min_num_features; // >20 segun resultados
    private static int jump_num_features;
    private static int max_num_features; // En el video de la descrición de la clase, se menciona que la raiz cuadrada de la cantidad de atributos es un buen número. >25
    private static int min_num_iterations;
    private static int jump_num_iterations;
    private static int max_num_iterations;
    private static double best_fMeasure;
    // private static int bagging_percent; //TODO
    private static HashMap<Integer, ArrayList<Double>> depth_values = new HashMap<>();
    private static HashMap<Integer, ArrayList<Double>> num_features_values = new HashMap<>();
    private static HashMap<Integer, ArrayList<Double>> num_iterations_values = new HashMap<>();


    /**
     *
     * @param args
     * <ul>
     *     <li>args[0]: DataSet</li>
     *     <li>args[1]: emaitzen direktorioa</li>
     * </ul>
     */
    public static void main(String[] args) {
        if(args.length == 11) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd-HHmmss");
            date = simpleDateFormat.format(new Date());

            ezarpenak(args);
            datuakKargatu(args);
            ekorketa();
            emaitzak();
            buildCSV();
        }
        else{
            System.out.println("Programak 11 argumentu behar ditu:");
            System.out.println("1. .arff fitxategiaren path");
            System.out.println("2. emaitzak gordeko den direktorioaren path (azkenena \\ barik)");
            System.out.println("3. min_depth (int)");
            System.out.println("4. jump_depth (int)");
            System.out.println("5. max_depth (int)");
            System.out.println("6. min_num_features (int)");
            System.out.println("7. jump_num_features (int)");
            System.out.println("8. max_num_features (int)");
            System.out.println("9. min_num_iterations (int)");
            System.out.println("10. jump_num_iterations (int)");
            System.out.println("11. max_num_iterations (int)");
        }
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

    private static void ezarpenak(String[] args){
        sourceArff = args[0];
        storeDir = args[1];
        numfolds = 2;
        ev_method = numfolds + "-fCV";
        min_depth = Integer.parseInt(args[2]); // 10
        jump_depth = Integer.parseInt(args[3]);
        max_depth = Integer.parseInt(args[4]); // 15
        min_num_features = Integer.parseInt(args[5]); // 25
        jump_num_features = Integer.parseInt(args[6]);
        max_num_features = Integer.parseInt(args[7]); // sqrt(data.numAttributes()) = 38
        min_num_iterations = Integer.parseInt(args[8]); // 25
        jump_num_iterations = Integer.parseInt(args[9]);
        max_num_iterations = Integer.parseInt(args[10]); // 125
    }

    private static void ekorketa(){
        try{
            long hasiera = System.currentTimeMillis();
            ekoreketaHarabiatua();
            long amaiera = System.currentTimeMillis();
            exekuzioDenbora = (double) ((amaiera - hasiera)/1000);
        } catch (Exception e){
            System.out.println("Errorea ekorketa egiterakoan");
        }
    }

    private static void ekoreketaHarabiatua(){
        try{
            RandomForest randomForest = new RandomForest();
            int best_depth = 0;
            int best_num_features = 0;
            best_fMeasure = 0;
            int best_num_iterations = 0;
            int iteration = 1;

            int sum_iterations = (((max_depth-min_depth)/jump_depth)+2) * (((max_num_features-min_num_features)/jump_num_features)+1) * (((max_num_iterations-min_num_iterations)/jump_num_iterations)+1);
            System.out.println("Exekuzioak izango dituen iterazio kopurua: " + sum_iterations);
            randomForest.setNumExecutionSlots(Runtime.getRuntime().availableProcessors()); // prozesadore gehiago erabili dezan (buklerak azkarrago)
            for (int depth = 0; depth <= max_depth; depth+=jump_depth) {
                for (int num_features = min_num_features; num_features <= max_num_features; num_features+=jump_num_features){
                    for (int num_iterations = min_num_iterations; num_iterations <= max_num_iterations; num_iterations+=jump_num_iterations){
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
                        balioakGorde(depth, num_features, num_iterations, fMeasure);
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
    }

    private static void balioakGorde(int depth, int num_features, int num_iterations, double fmeas){
        if (depth_values.get(depth) != null) {
            depth_values.get(depth).add(fmeas);
        } else{
            ArrayList<Double> list = new ArrayList<>();
            list.add(fmeas);
            depth_values.put(depth, list);
        }

        if (num_features_values.get(num_features) != null){
            num_features_values.get(num_features).add(fmeas);
        } else {
            ArrayList<Double> list = new ArrayList<>();
            list.add(fmeas);
            num_features_values.put(num_features, list);
        }

        if (num_iterations_values.get(num_iterations) != null){
            num_iterations_values.get(num_iterations).add(fmeas);
        } else {
            ArrayList<Double> list = new ArrayList<>();
            list.add(fmeas);
            num_iterations_values.put(num_iterations, list);
        }
    }

    private static void optimoaGorde(int best_depth, int best_num_features, int best_num_iterations){
        try{
            randomForest_optimo = new RandomForest();
            randomForest_optimo.setMaxDepth(best_depth);
            randomForest_optimo.setNumFeatures(best_num_features);
            randomForest_optimo.setNumIterations(best_num_iterations);
            randomForest_optimo.buildClassifier(data);
            SerializationHelper.write(storeDir + File.separator + date +"RF_optimoa.model", randomForest_optimo);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void emaitzak(){
        try{
            FileWriter myWriter = new FileWriter(storeDir + File.separator + date +"RF_optimo_ema.txt");
            myWriter.write(date + "\n");
            myWriter.write(".arff file " + sourceArff + " with " + data.numInstances() + " instances and " + data.numAttributes() + "\n" );
            myWriter.write("Evaluazio eskema: " + ev_method + "\n");
            myWriter.write("Ebaluazio metrika 'fMeasure': " + best_fMeasure + "\n");
            myWriter.write("Random Forest optimum parameters:\n");
            myWriter.write(" · MaxDepth: " + randomForest_optimo.getMaxDepth() + " (" + min_depth + " --> " + max_depth + " +" + jump_depth + ")" + "\n");
            myWriter.write(" · NumFeatures: " + randomForest_optimo.getNumFeatures() + " (" + min_num_features + " --> " + max_num_features + " +" + jump_num_features + ")" + "\n");
            myWriter.write(" · NumIterations: " + randomForest_optimo.getNumIterations() + " (" + min_num_iterations + " --> " + max_num_iterations + " +" + jump_num_iterations + ")" + "\n");
            myWriter.write(" . BagSizePrecent: " + randomForest_optimo.getBagSizePercent() + "\n");
            myWriter.write("Exekuzio denbora: " + exekuzioDenbora + " segundo\n");
            myWriter.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void buildCSV() {
        try{
            // 1. depth values
            FileWriter myWriter = new FileWriter(storeDir + File.separator + date + "RF_depth.csv");
            myWriter.write("depth,value\n");
            depth_values.forEach(
                    (k,v) -> writeInCSV(myWriter, k + "," + v + "\n")
            );
            myWriter.close();

            // 2. numFeatures values
            FileWriter myWriter1 = new FileWriter(storeDir + File.separator + date + "RF_numFeatures.csv");
            myWriter1.write("numFeatures,value\n");
            num_features_values.forEach(
                    (k,v) -> writeInCSV(myWriter1, k + "," + v + "\n")
            );
            myWriter1.close();

            // 3. numIterations values
            FileWriter myWriter2 = new FileWriter(storeDir + File.separator + date + "RF_numIterations.csv");
            myWriter2.write("numIterations,value\n");
            num_iterations_values.forEach(
                    (k,v) -> writeInCSV(myWriter2, k + "," + v + "\n")
            );
            myWriter2.close();
        } catch (Exception e){
            System.out.println("Errorea CSV-a gordetzean");
        }
    }

    public static void writeInCSV(FileWriter myWriter, String toWrite){
        try{
            myWriter.write(toWrite);
        } catch (IOException e){
            System.out.println("Errorea CSV-a gordetzean");
        }
    }
}
