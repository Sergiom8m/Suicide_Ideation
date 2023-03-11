import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class prueba {
    /**
     * This program needs two arguments:
     * <ol>
     *     <li>CSV file to transform</li>
     *     <li>Transformed arff file</li>
     * </ol>
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String csv_source_path = args[0];
        String arff_origin_path = args[1];

        String[] instantzien_lista = getInstantzienLista(csv_source_path);
        // printLista(instantzien_lista);

        String[][] instantzienMatrizea = getInstantzienMatrizea(instantzien_lista);

        gordeInstantzienMatrizea(instantzienMatrizea, arff_origin_path);
    }

    public static void gordeInstantzienMatrizea(String[][] instantzien_matrizea, String path) throws IOException{
        FileWriter myWriter = new FileWriter(path);
        myWriter.write("@RELATION new_realtion"); // TODO ajustarlo a un nombre o parametro


        System.out.println(instantzien_matrizea[0][0]);
        myWriter.close();
    }

    public static String[][] getInstantzienMatrizea(String[] lista) {
        String[][] ema = new String[lista.length][3];
        for (int i = 0; i < lista.length; i++) {
            String unekoa = lista[i];
            String[] l = unekoa.split(",");
            ema[i][0] = l[0];
            ema[i][2] = l[l.length - 1];
            StringBuilder erdikoa = new StringBuilder();
            for (int j = 1; j <= l.length - 2; j++) {
                erdikoa.append(l[j]).append(",");
            }
            ema[i][1] = erdikoa.toString();
            System.out.println("\nID: " + ema[i][0] + "\nTestua: " + ema[i][1] + "\nKlasea: " + ema[i][2] + "\n-------------------------------------------------------------------------------");
        }
        return ema;
    }

    public static String[] getInstantzienLista(String path) throws IOException {
        // TODO hay que tener en cuenta que la primera linea es el nombre de los atributos
        Path csvPath = Path.of(path);
        String csvEdukia = Files.readString(csvPath);
        String[] instantzien_lista = csvEdukia.split("(?<=suicide\n)");
        return instantzien_lista;
    }


    public static void printLista(String[] lista){
        for (int i = 0; i < lista.length; i++) {
            System.out.println("----------------------------------------------------------------------");
            System.out.println(lista[i]);
        }
        System.out.println("LISTAREN LUZEERA: " + lista.length);
    }
}