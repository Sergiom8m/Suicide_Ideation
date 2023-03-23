import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.Utils;
import weka.core.converters.ConverterUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Klase hau, sailkatzailearen parametro ekorketa egiteko eraibliko da.
 * Erabiliko den datu sortan, klase atributua azken atributuan doa.
 * Erabiliko den sailkatzailea 'RandomForest' sailkatzailea da.
 *
 * Hau egiteko, oso komenigarria da RandomForest nola funtzionatzen duen jakitea.
 * RandomForest sailkatzaileak erabiltzen dituen atributuak:
 * <ul>
 *     <li><b>setMaxDepth</b>, zuhaitz bakoitzaren sakonera: RandomForest erabiltzen dituen zuhaitzen sakonera ezarritzen duen parametroa
 *     da. 'int' motako balioak har ditzake. 0 balioa ematen bazaio, limiterik gabeko zuhaitzak egingo ditu.</li>
 *     <li><b>setNumFeatures</b>, zuhaitz bakoitza eraikitzeko erabiltzen duen atributu kopurua: "Azalpen bideoa" bideoan,
 *     atributu kopuruaren erro karratua zenbaki egoki bat dela adierazten da.</li>
 *     <li><b>setNumIterations</b> (ez da eskatzen): Bootstraping egitean (laginak jaso), hartuko diren lagin kopurua. >0 izan behar da
 *     default 100</li>
 *     <li><b>setBagSizePercent</b> (Bootstraping + Aggregation) (ez da eskatzen): Bootstraping egitean hartutako lagin bakoitza zenbatekoa
 *     den adierazteko erabili da. Weka-k eskaintzen duen metodoa. default 100 </li>
 * </ul>
 *
 * <p><a href="https://weka.sourceforge.io/doc.dev/weka/classifiers/trees/RandomForest.html">RandomForest (weka-doc)</a></p>
 * <p><a href="https://youtu.be/v6VJ2RO66Ag">Azalpen bideoa</a></p>
 * <p><a href="https://www.simplilearn.com/tutorials/machine-learning-tutorial/bagging-in-machine-learning#:~:text=Bagging%2C%20also%20known%20as%20Bootstrap,variance%20of%20a%20prediction%20model.">Bagging</a></p>
 * <p><a href="https://en.wikipedia.org/wiki/Bootstrap_aggregating">Bootstrap aggregating (Wikipedia)</a></p>
 *
 * <p>Ebaluaketa eskema: hold-out (stratified motakoa jasotzen diren train eta dev estratifikatuta daudelako)</p>
 *
 * <p>Ebaluaketa metika optimizatzeko: <b>klase minoritarioaren f-measure</b> izango da. </p>
 */
public class RandomForestOptimoa {

    private static String train_sourceArff;
    private static String dev_sourceArff;
    private static String data_sourceArff; // train+test instances
    private static String storeDir;
    private static Instances data;
    private static Instances train_data;
    private static Instances dev_data;
    private static String date;
    private static double exekuzioDenbora;
    private static RandomForest randomForest_optimo;
    private static int min_depth; // probar siempre con 0
    private static int jump_depth;
    private static int max_depth;
    private static int min_num_features; // >20 segun resultados
    private static int jump_num_features;
    private static int max_num_features; // En el video de la descrición de la clase, se menciona que la raiz cuadrada de la cantidad de atributos es un buen número. >25
    private static int min_num_iterations;
    private static int jump_num_iterations;
    private static int max_num_iterations;
    private static int min_bag_size;
    private static int jump_bag_size;
    private static int max_bag_size;
    private static double best_fMeasure;
    private static int minMaizPos;
    private static HashMap<Integer, ArrayList<Double>> depth_values = new HashMap<>();
    private static HashMap<Integer, ArrayList<Double>> num_features_values = new HashMap<>();
    private static HashMap<Integer, ArrayList<Double>> num_iterations_values = new HashMap<>();
    private static HashMap<Integer, ArrayList<Double>> bag_size_values = new HashMap<>();


    /**
     *
     * @param args
     * <ul>
     *     <li>args[0]: train DataSet</li>
     *     <li>args[1]: dev DataSet</li>
     *     <li>args[2]: data (train+dev) DataSet</li>
     *     <li>args[3]: emaitzak gordetzeko direktorioa</li>
     *     <li>args[4]: min_depth</li>
     *     <li>args[5]: jump_depth</li>
     *     <li>args[6]: max_depth</li>
     *     <li>args[7]: min_num_features</li>
     *     <li>args[8]: jump_num_features</li>
     *     <li>args[9]: max_num_features</li>
     *     <li>args[10]: min_num_iterations</li>
     *     <li>args[11]: jump_num_iterations</li>
     *     <li>args[12]: max_num_iterations</li>
     *     <li>args[13]: min_bag_size</li>
     *     <li>args[14]: jump_bag_size</li>
     *     <li>args[15]: max_bag_size</li>
     * </ul>
     */
    public static void main(String[] args) {
        if(args.length == 16) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd-HHmmss");
            date = simpleDateFormat.format(new Date());

            ezarpenak(args);
            datuakKargatu();
            ekorketa();
            emaitzak();
            buildCSV();
        }
        else{
            System.out.println("Programak 16 argumentu behar ditu:");
            System.out.println("1. train.arff fitxategiaren path-a");
            System.out.println("2. dev.arff fitxategiaren path-a");
            System.out.println("3. data.arff (train+test) fitxategiaren patha-a");
            System.out.println("4. emaitzak gordeko den direktorioaren path (azkenena \\ barik)");
            System.out.println("5. min_depth (int)");
            System.out.println("6. jump_depth (int)");
            System.out.println("7. max_depth (int)");
            System.out.println("8. min_num_features (int)");
            System.out.println("9. jump_num_features (int)");
            System.out.println("10. max_num_features (int)");
            System.out.println("11. min_num_iterations (int)");
            System.out.println("12. jump_num_iterations (int)");
            System.out.println("13. max_num_iterations (int)");
            System.out.println("14. min_bag_size (int)");
            System.out.println("15. jump_bag_size (int)");
            System.out.println("16. max_bag_size (int)");
        }
    }

    private static void ezarpenak(String[] args){
        train_sourceArff = args[0];
        dev_sourceArff = args[1];
        data_sourceArff = args[2];
        storeDir = args[3];
        min_depth = Integer.parseInt(args[4]);
        jump_depth = Integer.parseInt(args[5]);
        max_depth = Integer.parseInt(args[6]);
        min_num_features = Integer.parseInt(args[7]);
        jump_num_features = Integer.parseInt(args[8]);
        max_num_features = Integer.parseInt(args[9]); // sqrt(data.numAttributes()) ???
        min_num_iterations = Integer.parseInt(args[10]);
        jump_num_iterations = Integer.parseInt(args[11]);
        max_num_iterations = Integer.parseInt(args[12]);
        min_bag_size = Integer.parseInt(args[13]);
        jump_bag_size = Integer.parseInt(args[14]);
        max_bag_size = Integer.parseInt(args[15]);
    }

    private static void datuakKargatu(){
        try{
            System.out.println("Parametro ekorketa egiteko erabiliko diren fitxategiak: ");
            ConverterUtils.DataSource train_source = new ConverterUtils.DataSource(train_sourceArff);
            train_data = train_source.getDataSet();
            train_data.setClassIndex(train_data.numAttributes()-1);
            System.out.println(" · train: " + train_data.numInstances() + " instances, " + train_data.numAttributes() + " attributes");

            ConverterUtils.DataSource dev_source = new ConverterUtils.DataSource(dev_sourceArff);
            dev_data = dev_source.getDataSet();
            dev_data.setClassIndex(dev_data.numAttributes()-1);
            System.out.println(" · test: " + dev_data.numInstances() + " instances, " + dev_data.numAttributes() + " attributes");

            ConverterUtils.DataSource data_source = new ConverterUtils.DataSource(data_sourceArff);
            data = data_source.getDataSet();
            data.setClassIndex(data.numAttributes()-1);
            System.out.println(" · data: " + data.numInstances() + " instances, " + data.numAttributes() + " attributes");
            minMaizPos = Utils.minIndex(data.attributeStats(data.numAttributes()-1).nominalCounts);
        }
        catch (Exception e){
            System.out.println("Errorea datuak kargatzean");
        }
    }

    private static void ekorketa(){
        try{
            long hasiera = System.currentTimeMillis();
            RandomForest randomForest = new RandomForest();
            best_fMeasure = 0;
            int best_depth = 0;
            int best_num_features = 0;
            int best_num_iterations = 0;
            int best_bag_size = 0;
            int iteration = 1;

            int sum_iterations = (((max_depth-min_depth)/jump_depth)+2) *
                    (((max_num_features-min_num_features)/jump_num_features)+1) *
                    (((max_num_iterations-min_num_iterations)/jump_num_iterations)+1) *
                    (((max_bag_size-min_bag_size)/jump_bag_size)+1);
            System.out.println("\nExekuzioak izango dituen iterazio kopurua: " + sum_iterations);
            randomForest.setNumExecutionSlots(Runtime.getRuntime().availableProcessors()); // prozesadore gehiago erabili dezan (bukleak azkarrago)
            for (int depth = 0; depth <= max_depth; depth+=jump_depth) {
                for (int num_features = min_num_features; num_features <= max_num_features; num_features+=jump_num_features){
                    for (int num_iterations = min_num_iterations; num_iterations <= max_num_iterations; num_iterations+=jump_num_iterations){
                        for (int bag_size = min_bag_size; bag_size <= max_bag_size; bag_size+=jump_bag_size){
                            System.out.println("\n------ " + iteration + ". iterazioa ------");
                            System.out.println("depth: " + depth);
                            System.out.println("num_features: " + num_features);
                            System.out.println("num_iterations: " + num_iterations);
                            System.out.println("bag_size: " + bag_size);
                            randomForest.setMaxDepth(depth);
                            randomForest.setNumFeatures(num_features);
                            randomForest.setNumIterations(num_iterations);
                            randomForest.setBagSizePercent(bag_size);
                            randomForest.buildClassifier(train_data);

                            Evaluation evaluation = new Evaluation(train_data);
                            evaluation.evaluateModel(randomForest, dev_data);

                            double fMeasure = evaluation.fMeasure(minMaizPos);
                            System.out.println(iteration + ". iterazioko klase minoritarioaren f-measure: " + fMeasure);
                            balioakGorde(depth, num_features, num_iterations, bag_size, fMeasure);
                            if (fMeasure > best_fMeasure) {
                                best_fMeasure = fMeasure;
                                best_depth = depth;
                                best_num_features = num_features;
                                best_num_iterations = num_iterations;
                                best_bag_size = bag_size;
                            }
                            iteration++;
                        }
                    }
                }
                if (depth == 0) depth = min_depth-jump_depth;
            }
            optimoaGorde(best_depth, best_num_features, best_num_iterations, best_bag_size);
            long amaiera = System.currentTimeMillis();
            exekuzioDenbora = (double) ((amaiera - hasiera)/1000);
        } catch (Exception e){
            System.out.println("Errorea ekorketa egiterakoan");
        }
    }

    private static void balioakGorde(int depth, int num_features, int num_iterations, int bag_size, double fmeas){
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

        if (bag_size_values.get(bag_size) != null){
            bag_size_values.get(bag_size).add(fmeas);
        } else {
            ArrayList<Double> list = new ArrayList<>();
            list.add(fmeas);
            bag_size_values.put(bag_size, list);
        }
    }

    private static void optimoaGorde(int best_depth, int best_num_features, int best_num_iterations, int best_bag_size){
        try{
            randomForest_optimo = new RandomForest();
            randomForest_optimo.setMaxDepth(best_depth);
            randomForest_optimo.setNumFeatures(best_num_features);
            randomForest_optimo.setNumIterations(best_num_iterations);
            randomForest_optimo.setBagSizePercent(best_bag_size);
            randomForest_optimo.buildClassifier(data); // datu guztiekin entrenatzen da (train+test)
            SerializationHelper.write(storeDir + File.separator + date +"RF_optimoa.model", randomForest_optimo);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void emaitzak(){
        try{
            FileWriter myWriter = new FileWriter(storeDir + File.separator + date +"RF_optimo_ema.txt");
            myWriter.write(date + "\n\n");
            myWriter.write("\t--- FILES ---\n");
            myWriter.write("train.arff file " + train_sourceArff + " with " + train_data.numInstances() + " instances and " + train_data.numAttributes() + "\n" );
            myWriter.write("dev.arff file " + dev_sourceArff + " with " + dev_data.numInstances() + " instances and " + dev_data.numAttributes() + "\n" );
            myWriter.write("data.arff file " + data_sourceArff + " with " + data.numInstances() + " instances and " + data.numAttributes() + "\n" );
            myWriter.write("\n\t--- EVALUATION ---\n");
            myWriter.write("Ebaluazio eskema: Hold-out\n");
            myWriter.write("Ebaluazio metrika " + data.classAttribute().value(minMaizPos) + " (klase minoritarioa) f-measure: " + best_fMeasure + "\n");
            myWriter.write("\n\t--- PARAMETRO OPTIMOAK ---\n");
            myWriter.write(" · MaxDepth: " + randomForest_optimo.getMaxDepth() + " (" + min_depth + " --> " + max_depth + " +" + jump_depth + ")" + "\n");
            myWriter.write(" · NumFeatures: " + randomForest_optimo.getNumFeatures() + " (" + min_num_features + " --> " + max_num_features + " +" + jump_num_features + ")" + "\n");
            myWriter.write(" · NumIterations: " + randomForest_optimo.getNumIterations() + " (" + min_num_iterations + " --> " + max_num_iterations + " +" + jump_num_iterations + ")" + "\n");
            myWriter.write(" . BagSizePrecent: " + randomForest_optimo.getBagSizePercent() + " (" + min_bag_size + " --> " + max_bag_size + " +" + jump_bag_size + ")" + "\n");
            myWriter.write("\nExekuzio denbora: " + exekuzioDenbora + " segundo\n");
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

            // 4. bagSize values
            FileWriter myWriter3 = new FileWriter(storeDir + File.separator + date + "RF_bagSize.csv");
            myWriter3.write("bagSize,value\n");
            bag_size_values.forEach(
                    (k,v) -> writeInCSV(myWriter3, k + "," + v + "\n")
            );
            myWriter3.close();
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
