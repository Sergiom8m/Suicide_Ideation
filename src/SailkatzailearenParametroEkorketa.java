import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.Random;

/**
 * Klase hau, sailkatzailearen parametro ekorketa egiteko eraibliko da.
 * Erabiliko den datu sortan, klase atributua azken atributuan doa.
 * Erabiliko den sailkatzailea RandomForest sailkatzailea da.
 * https://weka.sourceforge.io/doc.dev/weka/classifiers/trees/RandomForest.html
 * Hau egiteko, oso komenigarria da RandomForest nola funtzionatzen duen jakitea.
 * RandomForest sailkatzaileak erabiltzen dituen atributuak:
 * <ul>
 *     <li>Zuhaitz bakoitzaren sakonera: RandomForest erabiltzen dituen zuhaitzen sakonera ezarritzen duen parametroa
 *     da. 'int' motako balioak har ditzake. 0 balioa ematen bazaio, limiterik gabeko zuhaitzak egingo ditu</li>
 * </ul>
 *
 * Ebaluaketa metodoa: 10-fCV
 *
 * Optimizatzen saiatuko garen metrika: f-measure
 */
public class SailkatzailearenParametroEkorketa {
    public static void main(String[] args) {
        try{
            // 1. Parametro ekorketa egiteko erabiliko den datu-sorta kargatu
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(args[0]);
            Instances data = source.getDataSet();
            data.setClassIndex(data.numAttributes()-1);
            System.out.println("Parametro ekorketa egiteko eraibliko diren instantziak");

            // 2.
            int numfold = 10;
            RandomForest randomForest = new RandomForest();
            int best_dept = 0;
            for (int depth = 0; depth <= 10; depth++) {
                randomForest.setMaxDepth(depth);
                randomForest.buildClassifier(data);

                Evaluation evaluation = new Evaluation(data);
                evaluation.crossValidateModel(randomForest, data, numfold, new Random(1));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
