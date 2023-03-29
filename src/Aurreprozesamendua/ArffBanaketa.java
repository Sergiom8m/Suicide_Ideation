package Aurreprozesamendua;

import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.Resample;

public class ArffBanaketa {

    public static void main(String[] args) {
        try {

        }catch (Exception e){e.printStackTrace();}
    }

    public static void sortuErabiltzekoArff(String originPath, String newPath, int ehunekoa, String testPath) throws Exception {

        ConverterUtils.DataSource source = new ConverterUtils.DataSource(originPath);
        Instances data = source.getDataSet();
        data.setClassIndex(data.numAttributes() - 1);

        System.out.println("INSTANTZIA GUZTIEN KOPURUA: " + data.numInstances()+ "\n");

        Resample resample = new Resample();
        resample.setRandomSeed(1);
        resample.setNoReplacement(true);
        resample.setInvertSelection(false);
        resample.setSampleSizePercent(ehunekoa);
        resample.setInputFormat(data);
        Instances dataGurea = Filter.useFilter(data, resample);
        dataGurea.setClassIndex(dataGurea.numAttributes() - 1);

        System.out.println("ENTRENAMENDUA EGITEKO ERABILIKO DIREN INSTANTZIA KOPURUA: " + dataGurea.numInstances()+ "\n");

        ConverterUtils.DataSink dataSink = new ConverterUtils.DataSink(newPath);
        dataSink.write(dataGurea);


        //IRAGARPENETARAKO TEST %100-ehunekoa
        resample.setRandomSeed(1);
        resample.setNoReplacement(true);
        resample.setInvertSelection(true);
        resample.setInputFormat(data);
        Instances test = Filter.useFilter(data, resample);
        test.setClassIndex(test.numAttributes() - 1);

        resample = new Resample();
        resample.setRandomSeed(1);
        resample.setNoReplacement(true);
        resample.setInvertSelection(false);
        resample.setSampleSizePercent(30);
        resample.setInputFormat(test);
        test = Filter.useFilter(test, resample);
        test.setClassIndex(test.numAttributes() - 1);
        System.out.println("IRAGARPENAK EGITEKO ERRESERBATUTAKO INSTANTZIA KOPURUA: " + test.numInstances() + "\n");

        dataSink = new ConverterUtils.DataSink(testPath);
        dataSink.write(test);

    }
}
