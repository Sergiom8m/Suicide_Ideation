import Aurreprozesamendua.fssInfoGain;
import Aurreprozesamendua.getARFF;
import Aurreprozesamendua.getBowArff;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.FixedDictionaryStringToWordVector;
import weka.filters.unsupervised.attribute.Reorder;
import weka.filters.unsupervised.instance.SparseToNonSparse;

import java.io.File;

public class MainProgram {
    public static void main(String[] args) throws Exception {

        System.out.println("---------------------------------------------------");
        System.out.println("DATUAK KARGATUKO DIRA ARFF FORMATUAN");
        System.out.println("---------------------------------------------------");
        getARFF.getArff("Suicide_Detection.csv", "cleanData.arff");

        System.out.println("---------------------------------------------------");
        System.out.println("BoW SORTUKO DA");
        System.out.println("---------------------------------------------------");
        getBowArff.main("cleanData.arff", "hiztegia.txt", "trainBoW.arff", "test.arff");

        System.out.println("---------------------------------------------------");
        System.out.println("BoW SORTUKO DA");
        System.out.println("---------------------------------------------------");
        Baseline.baseline("trainBoW.arff");

        System.out.println("---------------------------------------------------");
        System.out.println("BoW SORTUKO DA");
        System.out.println("---------------------------------------------------");
        fssInfoGain.fssInfoGain("trainBOW.arff","FSS.arff)");

    }
}