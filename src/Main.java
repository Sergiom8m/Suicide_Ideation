import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

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
        HashSet a = new HashSet<String>();
        Path csvPath = Path.of(dataCSVPath);
        String csvEdukia = Files.readString(csvPath);
        String lista[] = csvEdukia.split("(?<=suicide\n)");

        for (int i=0 ; i<lista.length; i++){
            System.out.println("----------------------------------------------------------------------");
            System.out.println(lista[i]);
        }
    }
}