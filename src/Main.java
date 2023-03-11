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
        String dataPath, dataArffPath, dataArffPath2, dataCSVPath;
        if (args.length==0){
            dataCSVPath = "mental_health.csv";
            dataArffPath2 = "mental_health.arff";
            //dataCSVPath = "Suicide_Detection.csv";
            //dataArffPath = "Suicide_Detection.arff";
            dataPath = "datu.arff";
        }else{
            dataCSVPath = args[0];
            dataArffPath = args[1];
            dataArffPath2 = args[1];
            dataPath = args[1];
        }

        Path csvPath = Path.of(dataCSVPath);
        String csvEdukia = Files.readString(csvPath);
        String lista[] = csvEdukia.split("(?<=suicide\n)");


        for (int i=0 ; i<lista.length; i++){
            System.out.println("----------------------------------------------------------------------");
            System.out.println(lista[i]);
        }
        System.out.println("LISTAREN LUZEERA: "+lista.length);
        if(lista.length>1){
            KomakBanatu(lista);
        }else{
            KomakBanatu(dataCSVPath, dataArffPath2, dataPath);
        }
    }

    public static void KomakBanatu(String[] lista){
        String[][] ema= new String[lista.length][3];
        for (int i =0; i< lista.length; i++){
            String unekoa = lista[i];
            String l[]=unekoa.split(",");
            ema[i][0]=l[0];
            ema[i][2]=l[l.length-1];
            String erdikoa="";
            for(int j=1; j<l.length-2; j++){
                erdikoa=erdikoa+l[j];
            }
            ema[i][1]=erdikoa;
            //System.out.println("\nID: " + ema[i][0]+"\nTestua: " +ema[i][1]+"\nKlasea: " +ema[i][2]+"\n-------------------------------------------------------------------------------");
        }
    }

    public static void KomakBanatu(String csv, String arff, String datu) throws Exception{
        // load CSV
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(csv));
        Instances data = loader.getDataSet();

        // save ARFF
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        File f = new File(arff);
        saver.setFile(new File(arff));
        saver.setDestination(new File(datu));
        saver.writeBatch();
    }
}