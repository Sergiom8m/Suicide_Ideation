import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

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
    public static void main(String[] args) {
        long hasiera = System.currentTimeMillis();
        try{
            // 1. Parametro ekorketa egiteko erabiliko den datu-sorta kargatu
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(args[0]);
            Instances data = source.getDataSet();
            data.setClassIndex(data.numAttributes()-1);
            System.out.println("Parametro ekorketa egiteko eraibliko diren instantzia kopurua: " + data.numInstances());

            // 2. Parametro ekorketa
            // 2.1. Ekorketarako balioak definitu
            int numfolds = 2;
            int max_depth = 10;
            // int max_num_features = (int) Math.sqrt(data.numAttributes()); // En el video de la descrición de la clase, se menciona que la raiz cuadrada de la cantidad de atributos es un buen número.
            // int bagging_percent

            // 2.2. Ekorketa
            RandomForest randomForest = new RandomForest();
            int best_depth = 0;
            double best_fMeasure = 0;
            for (int depth = 0; depth <= max_depth; depth++) {
                System.out.println("------ " + depth + ". iterazioa ------");
                randomForest.setMaxDepth(depth);
                //randomForest.setNumFeatures(0); // TODO
                //randomForest.setNumDecimalPlaces();
                //randomForest.setBagSizePercent();
                //randomForest.setNumIterations();
                randomForest.buildClassifier(data);

                Evaluation evaluation = new Evaluation(data);
                evaluation.crossValidateModel(randomForest, data, numfolds, new Random(1));

                double fMeasure = evaluation.weightedFMeasure();
                System.out.println(depth + ". iterazioko f-measure: " + fMeasure);
                if (fMeasure > best_fMeasure) {
                    best_fMeasure = fMeasure;
                    best_depth = depth;
                }
            }

            // 3. Emaitzak
            System.out.println(" ------- EMAITZAK --------");
            System.out.println("Lortu den fMeasure maximoa: " + best_fMeasure);
            System.out.println("RandomForest sailkatzailearen hurrengo parametroekin:");
            System.out.println(" · MaxDepth: "+  best_depth);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        long amaiera = System.currentTimeMillis();
        double denbora = (double) ((amaiera - hasiera)/1000);
        System.out.println("Exekuzio denbora: " + denbora);
    }
}
