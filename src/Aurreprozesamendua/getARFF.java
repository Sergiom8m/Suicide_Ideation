package Aurreprozesamendua;


import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.supervised.instance.StratifiedRemoveFolds;
import weka.filters.unsupervised.instance.Resample;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.vdurmont.emoji.EmojiParser.parseToAliases;

public class getARFF {

    public static void getArff(String csvPath, String arffPath, int ehuneko) throws Exception {

        String pathOsoa = arffPath.split("\\.")[0]+"_Osoa.arff";
        //INSTANTZIEN IRAKURKETA:
        String[] instantzien_lista = getInstantzienLista(csvPath);
        String[][] instantzienMatrizea = getInstantzienMatrizea(instantzien_lista);

        //ARFF-A SORTU
        arffGoiburuaEzarri(instantzien_lista, pathOsoa);
        instantziakSartuArff(instantzienMatrizea, pathOsoa);


        sortuErabiltzekoArff(pathOsoa, arffPath, ehuneko);
    }

    public static void sortuErabiltzekoArff(String dagoenaPath, String berriaPath, int ehuneko) throws Exception {
        /*Resample setSampleSizePercent
        * %50 ->  95426 instantzia
        * %40 ->  76340 instantzia
        * %25 ->  47713 instantzia
        * %10 ->  19085 instantzia
        * %6  ->  11451 instantzia
        * %5  ->  9542  instantzia*/
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(dagoenaPath);
        Instances data = source.getDataSet();
        data.setClassIndex(data.numAttributes()-1);

        System.out.println(".arff osoaren instantzia kopurua: "+data.numInstances());

        Resample resample = new Resample();
        resample.setRandomSeed(42);
        resample.setNoReplacement(true);
        resample.setInvertSelection(false);
        resample.setSampleSizePercent(ehuneko);
        resample.setInputFormat(data);
        Instances dataGurea = Filter.useFilter(data, resample);

        System.out.println("Gure .arff instantzia kopurua: "+dataGurea.numInstances());

        ConverterUtils.DataSink dataSink = new ConverterUtils.DataSink(berriaPath);
        dataSink.write(dataGurea);


    }

    public static void arffGoiburuaEzarri (String[] instantzien_lista, String path) throws IOException {

        FileWriter myWriter = new FileWriter(path);
        myWriter.append("@relation Suicide_Detection");

        //ATRIBUTUAK LORTU
        String [] atributuen_lista = instantzien_lista[0].split("\r?\n|\r");
        atributuen_lista = atributuen_lista[0].split(",");

        //ATRIBUTUAK ARFF-AN GEHITU
        myWriter.append("\n" +"@attribute " + atributuen_lista[1] + " string");
        myWriter.append("\n" +"@attribute " + atributuen_lista[2] + " {'suicide', 'non-suicide'}");

        myWriter.close();
    }

    public static void instantziakSartuArff(String[][] instantzien_matrizea, String path) throws IOException {

        FileWriter myWriter = new FileWriter(path, true);
        myWriter.append("\n");
        myWriter.append("@data");
        for (int i = 0; i < instantzien_matrizea.length; i++){
            //LERRO BAKOITZEKO TESTUA ETA KLASEA IDATZI
            myWriter.append("\n" + instantzien_matrizea[i][1] + " " + instantzien_matrizea[i][2]);
        }
        myWriter.close();
    }

    public static String[][] getInstantzienMatrizea(String[] lista) {

        //INSTANZIA KOP X 3 DIMENTSIOKO MATRIZEA SORTU
        String[][] instantziaMat = new String[lista.length][3];
        for (int i = 0; i < lista.length; i++) {
            String unekoInstantzia = lista[i]; //INSTANTZIA LERRO OSOA
            String[] l = unekoInstantzia.split(",");
            instantziaMat[i][0] = l[0]; //IDENTIFIKADOREA
            instantziaMat[i][2] = "\'" + l[l.length - 1].replace("\n", "")+"\'"; //KLASEA
            instantziaMat[i][1] = garbituTestua(l); //TESTUA
        }
        return garbituInstantzienMatrizea(instantziaMat);
    }

    public static String garbituTestua(String[] l) {

        StringBuilder erdikoa = new StringBuilder();
        for (int j = 1; j <= l.length - 2; j++) {
            erdikoa.append(l[j]).append(" ");
        }
        return filtratuKaraktereak(erdikoa.toString());
    }

    public static String filtratuKaraktereak(String testua) {

        testua = testua.replaceAll("[\\u2800-\\u28FF]", ""); //BRAILLE TESTUA EZABATU

        testua = parseToAliases(testua); //EZABATU EMOJIAK
        testua = puntuazioMarkaKendu(testua); //PUNTUAZIO MARKAK EZABATU

        testua = testua.replaceAll("[0-9]", ""); //ZENBAKIAK EZABATU
        testua = testua.replaceAll("’", "").replace("\"", "").replace("-", ""); //KOMILLAK EZABATU
        testua = testua.replace("\r\n", " ").replace("\n", " ").replace("\r", " ");//ENTER-AK EZABATU

        //20 KARAKTERE BAINO GEHIAGOKO HITZAK KENDU
        String regex = "\\b\\w{20,}\\b";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(testua);
        if (matcher.find()) {
            testua = testua.replace( matcher.group(), "");
        }

        testua = testua.replaceAll("\\b\\w{1}\\b", ""); //KARAKTERE BAKARREKO HITZAK EZABATU

        //ZENBAKIAK EDO LETRAK EZ DIREN ELEMENTU DENAK KENDU
        if(!testua.matches(".*[a-zA-Z0-9]+.*")){
            testua = " ";
        }
        //TESTUA KOMILLEN ARTEAN BUELTATU
        return "\"" + testua + "\",";
    }

    public static String[][] garbituInstantzienMatrizea(String[][] mat) {

        System.out.println("GARBITU AURRETIK INSTANTZIA KOPURUA: " + mat.length);
        String[][] aux = new String[mat.length][mat[0].length];
        int i = 0;
        for(String[] UnekoInstantzia : mat){
            if(UnekoInstantzia[2].equals("\'suicide\'") || UnekoInstantzia[2].equals("\'non-suicide\'")){
                if(70<UnekoInstantzia[1].length() && UnekoInstantzia[1].length()<1500){ //TESTUAREN LUZEERA FILTRATU
                    if(!UnekoInstantzia[1].matches("[^a-zA-Z0-9 ]")){ //KARAKTERE BEREZIAK SOILIK DITUZEN INSTANTZIAK EZABATU
                        aux[i] = UnekoInstantzia;
                        i++;
                    }
                }
            }
        }
        String[][] matrizeGarb = new String[i][aux[0].length];
        for(int j = 0; j < i; j++){
            matrizeGarb[j] = aux[j];
        }
        System.out.println("KARGATUTAKO INSTANTZIA KOPURUA: " + matrizeGarb.length);
        return matrizeGarb;

    }

    public static String[] getInstantzienLista(String path) throws IOException {

        //CSV FITXATEGIA IRAKURRI
        Path csvPath = Path.of(path);
        String csvEdukia = Files.readString(csvPath);

        //INSTANTZIAK BANATU
        String[] instantzien_lista = csvEdukia.split("(?<=suicide\n)");
        return instantzien_lista;
    }

    public static String puntuazioMarkaKendu(String testua) {

        return testua.replaceAll("\\p{Punct}", " "); //PUNTUAZIO MARKAK EZABATU

    }
}