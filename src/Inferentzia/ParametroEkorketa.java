package Inferentzia;

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
 * Hau egiteko, oso komenigarria da RandomForest nola funtzionatzen duen jakitea.
 * <p>RandomForest sailkatzaileak erabiltzen dituen atributuak:</p>
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
public class ParametroEkorketa {

    private static String train_sourceArff;
    private static String dev_sourceArff;
    private static String data_sourceArff; // train + dev instances
    private static String storeDir;
    private static Instances data;
    private static Instances train_data;
    private static Instances dev_data;
    private static String date;
    private static double exekuzioDenbora;
    private static RandomForest randomForest_optimo;
    private static int min_depth;
    private static int jump_depth;
    private static int max_depth;
    private static int min_num_features;
    private static int jump_num_features;
    private static int max_num_features;
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
     *<h3>Aurre-baldintzak:</h3>
     * <ol>
     *     <li> parametro bezala trainFSS datu multzoaren .arff fitxategia</li>
     *     <li> parametro bezala devFSS datu multzoaren .arff fitxategia</li>
     *     <li> parametro bezala trainFSS + devFSS datu multzoaren .arff fitxategia</li>
     *     <li> parametro bezala emaitzak gordetzeko .txt fitxategia</li>
     *</ol>
     *
     * <h3>Ondorengo-baldintzak:</h3>
     * <ol>
     *      <li> fitxategi bezala .model fitxategia</li>
     *      <li> fitxategi bezala 4. parametroan adierazitako .txt fitxategia</li>
     *</ol>
     * <h3>Exekuzio-adibidea:</h3>
     *      java -jar ParametroEkorketa.jar path/to/trainFSS.arff path/to/devFSS.arff path/to/dataFSS.arff path/to/irteerako/ParametroEkorketaEmaitzak.txt
     */
    public static void main(String[] args) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd-HHmmss");
            date = simpleDateFormat.format(new Date());

            ezarpenak(args);
            datuakKargatu();
            ekorketa();
            emaitzak();
            buildCSV();
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("\nZeozer gaizki sartu da. Exekuzio adibidea:\n" +
                    "\t\t\t java -jar ParametroEkorketa.jar path/to/trainFSS.arff path/to/devFSS.arff path/to/dataFSS.arff path/to/irteerako/ParametroEkorketaEmaitzak.txt\n\n");
        }
    }

    /**
     *
     * @param args
     * @throws Exception
     */
    private static void ezarpenak(String[] args) throws Exception{
        train_sourceArff = args[0];
        dev_sourceArff = args[1];
        data_sourceArff = args[2];
        storeDir = args[3];
        min_depth = 80; // 0 balioak 'unlimited' adierazten du
        jump_depth = 50;
        max_depth = 120;
        min_num_features = 100;
        jump_num_features = 20;
        max_num_features = 300; // sqrt(data.numAttributes())
        min_num_iterations = 102;
        jump_num_iterations = 1;
        max_num_iterations = 102;
        min_bag_size = 100;
        jump_bag_size = 1;
        max_bag_size = 100;
    }

    /**
     * Ekorketa egiteko erabiliko diren datuen karga: train, dev eta test
     */
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

    /**
     * Ekorketa prozesua garatuko da
     */
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

            // Parametro ekorketak izango dituen iterazio kopurua
            int sum_iterations = (((max_depth-min_depth)/jump_depth)+1) *
                    (((max_num_features-min_num_features)/jump_num_features)+1) *
                    (((max_num_iterations-min_num_iterations)/jump_num_iterations)+1) *
                    (((max_bag_size-min_bag_size)/jump_bag_size)+1);
            System.out.println("\nExekuzioak izango dituen iterazio kopurua: " + sum_iterations);

            randomForest.setNumExecutionSlots(Runtime.getRuntime().availableProcessors()); // prozesadore gehiago erabili dezan (bukleak azkarrago)
            for (int depth = min_depth; depth <= max_depth; depth+=jump_depth) {
                for (int num_features = min_num_features; num_features <= max_num_features; num_features+=jump_num_features){
                    for (int num_iterations = min_num_iterations; num_iterations <= max_num_iterations; num_iterations+=jump_num_iterations){
                        for (int bag_size = min_bag_size; bag_size <= max_bag_size; bag_size+=jump_bag_size){
                            System.out.println("\n------ " + iteration + ". iterazioa ------");
                            System.out.println("depth: " + depth);
                            System.out.println("num_features: " + num_features);
                            System.out.println("num_iterations: " + num_iterations);
                            System.out.println("bag_size: " + bag_size);
                            // Sailkatzailea sortu
                            randomForest.setMaxDepth(depth);
                            randomForest.setNumFeatures(num_features);
                            randomForest.setNumIterations(num_iterations);
                            randomForest.setBagSizePercent(bag_size);
                            randomForest.buildClassifier(train_data);

                            // Sortutako sailkatzailearen gainean ebaluazioa gauzatu
                            Evaluation evaluation = new Evaluation(train_data);
                            evaluation.evaluateModel(randomForest, dev_data);

                            // Lortutako f-measure aztertu
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
                // if (depth == 0) depth = min_depth-jump_depth;
            }
            optimoaGorde(best_depth, best_num_features, best_num_iterations, best_bag_size);
            long amaiera = System.currentTimeMillis();
            exekuzioDenbora = (double) ((amaiera - hasiera)/1000);
        } catch (Exception e){
            System.out.println("Errorea ekorketa egiterakoan");
        }
    }

    /**
     * Ekorketaren iterazio bakoitzeko, lortutako balioak gordeko dira.
     * @param depth gordeko den depth balioa
     * @param num_features gordeko den num_features balioa
     * @param num_iterations gordeko den num_iterations balioa
     * @param bag_size gordeko den bag_size balioa
     * @param fmeas aurreko parametroen balio bakoitzeko gordeko den f-measure balioa
     */
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

    /**
     * Ekorketa prozesuan lortu diren parametro optimoekin eredu sailkatzaile bat eraiki eta gordeko da.
     * @param best_depth lortu den maxDepth baliorik hoberena
     * @param best_num_features lortu den numFeatures baliorik handiena
     * @param best_num_iterations lortu den numIterations baliorik handiena
     * @param best_bag_size lortu den bagSizePercent baliorik handiena
     */
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

    /**
     * Ekorketa prozesuan eraibli diren aldagaiak eta lortutako balioak gorde.
     */
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

    /**
     * Ekorketa prozesuan iterazio bakoitzeko gorden diren balioak CSV moduan gordetzeko. Horrela, beste programa
     * batzuekin grafiko horiek prozesatu ahal izango dira eta grafiko moduan adierazi, adibidez.
     */
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

    /**
     *
     * @param myWriter
     * @param toWrite
     */
    public static void writeInCSV(FileWriter myWriter, String toWrite){
        try{
            myWriter.write(toWrite);
        } catch (IOException e){
            System.out.println("Errorea CSV-a gordetzean");
        }
    }
}