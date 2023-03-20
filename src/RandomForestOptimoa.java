import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
 *     <li>Zuhaitz bakoitza eraikitzeko erabiltzen duen atributu kopurua, <b>setNumFeatures</b>: "Azalpen bideoa" bidoan,
 *     atributu kopuruaren erro karratua zenbaki egoki bat dela adierazten da.</li>
 *     <li><b>setNumDecimalPlaces??</b></li>
 *     <li><b>setBagSizePercent</b> (Bootstraping + Aggregation): Bootstraping egitean hartutako lagin bakoitza zenbatekoa
 *     den adierazteko erabili da. Weka-k eskaintzen duen metodoa </li>
 *     <li><b>setNumIterations</b>: Bootstraping egitean (laginak jaso), hartuko diren lagin kopurua</li>
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

    /**
     *
     * @param args
     * <ul>
     *     <li>args[0]: DataSet</li>
     *     <li>args[1]: emaitzen direktorioa</li>
     * </ul>
     */
    public static void main(String[] args) {
        try{
            // 1. Parametro ekorketa egiteko erabiliko den datu-sorta kargatu
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(args[0]);
            Instances data = source.getDataSet();
            data.setClassIndex(data.numAttributes()-1);
            System.out.println("Parametro ekorketa egiteko eraibliko diren instantzia kopurua: " + data.numInstances());

            // 2. Parametro ekorketa
            // 2.1. Ekorketarako balioak limiteak definitu
            int numfolds = 2;
            String ev_method = numfolds + "-fCV";
            int max_depth = 10;
            int max_num_features = (int) Math.sqrt(data.numAttributes()); // En el video de la descrición de la clase, se menciona que la raiz cuadrada de la cantidad de atributos es un buen número.
            int max_num_iterations = 100;
            // int bagging_percent

            // 2.2. Ekorketa
            RandomForest randomForest = new RandomForest();
            int best_depth = 0;
            int best_num_features = 0;
            double best_fMeasure = 0;
            int iteration = 1;

            long hasiera = System.currentTimeMillis();
            for (int depth = 0; depth <= max_depth; depth++) {
                for (int num_features = 0; num_features <= max_num_features; num_features+=2){
                    for (int num_iterations = 0; num_iterations <= max_num_iterations; num_iterations+=2){
                        System.out.println("------ " + iteration + ". iterazioa ------");
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
                        }
                        iteration++;
                    }
                }
            }
            long amaiera = System.currentTimeMillis();
            double denbora = (double) ((amaiera - hasiera)/1000);
            System.out.println("\nExekuzio denbora: " + denbora + " segundo");

            // 3. Sailkatzaile optimoa
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd-Hms");
            String date = simpleDateFormat.format(new Date());

            RandomForest randomForest_optimo = new RandomForest();
            randomForest_optimo.setMaxDepth(best_depth);
            randomForest_optimo.setNumFeatures(best_num_features);
            randomForest_optimo.buildClassifier(data);
            SerializationHelper.write(args[1] + date +"RF_optimoa.model", randomForest_optimo);

            // 3. Emaitzak
            try{
                FileWriter myWriter = new FileWriter(args[1] + date +"RF_optimo_ema.txt");
                myWriter.write(date + "\n");
                myWriter.write(".arff file " + args[0] + " with " + data.numInstances() + " instances \n");
                myWriter.write("Evaluazio eskema: " + ev_method + "\n");
                myWriter.write("Ebaluazio metrika 'fMeasure' " + best_fMeasure + "\n");
                myWriter.write("Random Forest parameters:\n");
                myWriter.write(" · MaxDepth: " + randomForest_optimo.getMaxDepth() + "\n");
                myWriter.write(" · NumFeatures: " + randomForest_optimo.getNumFeatures() + "\n");
                myWriter.write(" · NumIterations: " + randomForest_optimo.getNumIterations() + "\n");
                myWriter.write(" . BagSizePrecent: " + randomForest_optimo.getBagSizePercent() + "\n");
                myWriter.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
