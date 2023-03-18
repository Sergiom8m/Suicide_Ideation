import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.Randomize;
import weka.filters.unsupervised.instance.RemovePercentage;
import weka.filters.unsupervised.instance.Resample;

import java.util.Random;

public class Baseline {

    public static void main(String[] args) throws Exception {

        //Datuak dituen fitxategia kargatu:
        ConverterUtils.DataSource source = new ConverterUtils.DataSource("trainBOW.arff");
        Instances data = source.getDataSet();

        data.setClassIndex(data.numAttributes() - 1);


        //Datuak randomizatu:
        Randomize r = new Randomize();
        r.setInputFormat(data);
        r.setRandomSeed(42);
        Instances rData = Filter.useFilter(data, r);


        //Train eta test lortu:
        Resample resample=new Resample();
        resample.setInputFormat(rData);
        resample.setSampleSizePercent(80);
        resample.setInvertSelection(true);
        resample.setNoReplacement(true);
        Instances test= Filter.useFilter(rData, resample);

        resample.setInputFormat(rData);
        resample.setNoReplacement(true);
        resample.setSampleSizePercent(80);
        Instances train=Filter.useFilter(rData, resample);


        train.setClassIndex(data.numAttributes() - 1);
        test.setClassIndex(data.numAttributes() - 1);


        //NaiveBayes classifier eta honekin datuen ebaluaketa burutu
        NaiveBayes klasifikadore = new NaiveBayes();
        klasifikadore.buildClassifier(train);


        //Ebaluatzailea:
        Evaluation evaluator = new Evaluation(train);
        evaluator.evaluateModel(klasifikadore,test);


        System.out.println(evaluator.weightedFMeasure());
    }
}
