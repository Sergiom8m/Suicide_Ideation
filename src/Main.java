import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws  Exception{
        String dataPath, dataArffPath, dataCSVPath;
        if (args.length==0){
            dataCSVPath = "Suicide_Detection.csv";
            dataArffPath = "Suicide_Detection.arff";
            dataPath = "Suicide_Detection.arff";
        }else{
            dataCSVPath = args[0];
            dataArffPath = args[1];
            dataPath = args[1];
        }
        //Load CSV
        //Parametroak:
        // --> csv_a: .csv fitxategiaren path-a
        // --> j_path: csv_a-ren path berbera, baina .arff bukaerarekin
        // --> h_path: bihurturiko .arff-a gorden nahi den path-a
        Path csvPath = Path.of(dataCSVPath);
        String csvEdukia = Files.readString(csvPath);
        String csvEdukiaOna = csvEdukia.replace("'", " ");

        File csvFile = new File(dataPath);
        FileWriter fw = new FileWriter(dataPath);
        fw.write(csvEdukiaOna);
        fw.close();

        File csvOna = new File(dataPath);


        CSVLoader loader = new CSVLoader();
        loader.setSource(csvOna);


        Instances data = loader.getDataSet();

        // save as an  ARFF (output file)
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        saver.setFile(new File(dataArffPath));
        // saver.setDestination(new File(j_path));
        saver.writeBatch();
        System.out.println("Arff fitxategia sortu da");
    }
}