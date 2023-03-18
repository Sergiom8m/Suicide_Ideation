import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.Resample;

public class Baseline {

    public static void baseline(String bowArff) throws Exception {

        //DATUAK DITUEN FITXATEGIA KARGATU
        ConverterUtils.DataSource source = new ConverterUtils.DataSource("trainBOW.arff");
        Instances data = source.getDataSet();
        data.setClassIndex(data.numAttributes() - 1);


        //TRAIN ETA TEST MULTZOAK LORTU
        Resample resample=new Resample();
        resample.setInputFormat(data);
        resample.setSampleSizePercent(80);
        resample.setInvertSelection(true);
        resample.setNoReplacement(true);
        Instances test= Filter.useFilter(data, resample);

        resample.setInputFormat(data);
        resample.setNoReplacement(true);
        resample.setSampleSizePercent(80);
        Instances train=Filter.useFilter(data, resample);

        train.setClassIndex(data.numAttributes() - 1);
        test.setClassIndex(data.numAttributes() - 1);


        //NAIVE BAYES CLASSIFIER SORTU
        NaiveBayes klasifikadore = new NaiveBayes();
        klasifikadore.buildClassifier(train);


        //EBALUAZIOA EGIN
        Evaluation evaluator = new Evaluation(train);
        evaluator.evaluateModel(klasifikadore,test);

        //F-MEASURE INPRIMATU
        System.out.println(evaluator.weightedFMeasure());
    }
}
