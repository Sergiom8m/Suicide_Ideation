package Aurreprozesamendua;

import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.Resample;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.vdurmont.emoji.EmojiParser.parseToAliases;

public class getArff {

    /**
     *<h3>Aurre-baldintzak:</h3>
     * <ol>
     *     <li> parametro bezala datuen csv originala</li>
     *     <li> parametro bezala train gordetzeko .arff fitxategiaren izena</li>
     *     <li> parametro bezala csv horretatik train-erako erabili nahi den ehunekoa</li>
     *     <li> parametro bezala test gordetzeko .arff fitxategiaren izena</li>
     *</ol>
     *
     * <h3>Ondorengo-baldintzak:</h3>
     * <ol>
     *      <li> fitxategi bezala 2. parametroan adierazitako .arff fitxategia</li>
     *      <li> fitxategi bezala 4. parametroan adierazitako .arff fitxategia</li>
     *</ol>
     * <h3>Exekuzio-adibidea:</h3>
     *      java -jar getArff.jar path/to/Suicide_Detection.csv path/to/irteerako/dataRAW.arff "ehunekoa" path/to/irteerako/testRAW.arff
     *
     *
     */
    public static void main(String[] args) throws Exception {

        try{
            getArff(args[0], args[1], Integer.parseInt(args[2]), args[3]);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("""

                    Zeozer gaizki sartu da. Exekuzio-adibidea:
                    \t\t\tjava -jar getArff.jar path/to/Suicide_Detection.csv path/to/irteerako/dataRAW.arff "ehunekoa" path/to/irteerako/testRAW.arff

                    """);
        }
    }

    /**
     * Instantzia multzo bat CSV formatuan jasota eta ehuneko bat jasota, CSV-aren instnantzien garbiketa egingo da. Ondoren,
     * zatikatea bat egingo da; ehunekoak adierazten duen portzentaia instantzien multzotik entrenamendu faserako
     * geratuko diren instantzi kopurua adierazten du; beste instantzi guztiak, irgarpen faserako gordeko da.
     * @param csvPath jasoko diren instantzien zerrendaren path-a
     * @param arffPath gordeko den arff-aren path-a
     * @param ehuneko instantzia guztietatik entrenamendurako erabiliko diren instantzien ehunekoa
     * @param testPath iragarpenetarako erabiliko den test multzoaren output path-a
     * @throws Exception
     */
    public static void getArff(String csvPath, String arffPath, int ehuneko, String testPath) throws Exception {

        System.out.println("CSV BATETIK ABIATUTA ARFF FITXATEGI GARBI BAT SORTUKO DA" + "\n");

        String pathOsoa = arffPath.split("\\.arff")[0] + "_Osoa.arff"; //Hemendik aterako da csv-ko instantzia guztien arff-a

        //INSTANTZIEN IRAKURKETA:
        String[] instantzien_lista = getInstantzienLista(csvPath);
        String[][] instantzienMatrizea = getInstantzienMatrizea(instantzien_lista);

        //ARFF-A SORTU
        arffGoiburuaEzarri(instantzien_lista, pathOsoa);
        instantziakSartuArff(instantzienMatrizea, pathOsoa);

        sortuErabiltzekoArff(pathOsoa, arffPath, ehuneko, testPath);
    }

    /**
     *
     * @param originPath
     * @param newPath
     * @param ehunekoa
     * @param testPath
     * @throws Exception
     */
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


        //IRAGARPENETARAKO TEST SOBERAN DAUDEN INSTANTZIETATIK
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

    /**
     * .arff fitxategiaren goiburua lortzen duen metodoa. Instantzien lista bat jasota, lehenengo lerroa irakurtzen da.
     * Lerro hau lerro berezia da eta instantziak eduki ez ezik, atributuen izena adierazten da. Metodo honek,
     * atributuen izena hartuko du. Metodoak, fitxategi berri bat sortuko du, lehehengo lerroan @relation etiqueta
     * erabiliko du eta hurrengo lerroetan atributuen izenak gordeko ditu.
     * @param instantzien_lista jasoko den instantzien lista
     * @param path sortuko den fitxategiaren path-a
     * @throws IOException fitxategia ezin bada sortu sortuko den salbuespena
     */
    public static void arffGoiburuaEzarri(String[] instantzien_lista, String path) throws IOException {

        FileWriter myWriter = new FileWriter(path);
        myWriter.append("@relation Suicide_Detection");

        //ATRIBUTUAK LORTU
        String[] atributuen_lista = instantzien_lista[0].split("\r?\n|\r");
        atributuen_lista = atributuen_lista[0].split(",");

        //ATRIBUTUAK ARFF-AN GEHITU
        myWriter.append("\n" + "@attribute " + atributuen_lista[1] + " string");
        myWriter.append("\n" + "@attribute " + atributuen_lista[2] + " {'suicide', 'non-suicide'}");

        myWriter.close();

    }

    /**
     * arff fitxategi baten path-a jasota, fitxategi horretan instantzia sortan jasotako instantziak eta bere
     * atributuak arff horretan gordeko dira
     * @param instantzien_matrizea jasoko den instantza sorta
     * @param path arff fitxategiaren path-a
     * @throws IOException
     */
    public static void instantziakSartuArff(String[][] instantzien_matrizea, String path) throws IOException {

        FileWriter myWriter = new FileWriter(path, true);
        myWriter.append("\n");
        myWriter.append("@data");
        for (int i = 0; i < instantzien_matrizea.length; i++) {
            //LERRO BAKOITZEKO TESTUA ETA KLASEA IDATZI
            myWriter.append("\n" + instantzien_matrizea[i][1] + " " + instantzien_matrizea[i][2]);
        }
        myWriter.close();

    }

    /**
     * Instantzien lerroak jasota, instantzien atributuengatik banatzen duen metodoa. 'lista'-ren balio bakoitzeko,
     * identifikatzailea, klasea eta testua lortzen da.
     * @param lista instantizen lista
     * @return instantzien lista eta instantzia bakoitzeko bere identifikatzailea, klasea eta testua
     */
    public static String[][] getInstantzienMatrizea(String[] lista) {

        //INSTANZIA KOP X 3 DIMENTSIOKO MATRIZEA SORTU
        String[][] instantziaMat = new String[lista.length][3];
        for (int i = 0; i < lista.length; i++) {
            String unekoInstantzia = lista[i]; //INSTANTZIA LERRO OSOA
            String[] l = unekoInstantzia.split(",");
            instantziaMat[i][0] = l[0];                                                              //IDENTIFIKADOREA
            instantziaMat[i][2] = "\'" + l[l.length - 1].replace("\n", "") + "\'";  //KLASEA
            instantziaMat[i][1] = garbituTestua(l);                                                 //TESTUA
        }
        return garbituInstantzienMatrizea(instantziaMat);
    }

    /**
     * Jasotako mezu bat hartuz, mezu horren garbiketa egingo da.
     * @param l filtratu behar diren instantzia sorta
     * @return
     */
    public static String garbituTestua(String[] l) {

        StringBuilder erdikoa = new StringBuilder();
        for (int j = 1; j <= l.length - 2; j++) {
            erdikoa.append(l[j]).append(" ");
        }
        return filtratuKaraktereak(erdikoa.toString());

    }

    /**
     * Instantzia baten mezua jasota, instantzia horren mezuan aldaketak eta ezabaketak egiten ditu. Hurrengo aldaketak
     * egin dezake:
     * <ul>
     *     <li>Braille testua ezabatu</li>
     *     <li>Emojiak testu moduan aldatu</li>
     *     <li>Puntuazio markak ezabatu</li>
     *     <li>Zenbakiak ezabatu</li>
     *     <li>Komillak ezabatu</li>
     *     <li>Enter-ak ezabatu</li>
     *     <li>20 karaktere baino gehiagoko hitzak ezabatu</li>
     *     <li>Karaktere bakarreko hitzak ezabatu</li>
     *     <li>Zenbakiak edo letrak ez diren elementuak kendu</li>
     * </ul>
     * @param testua garbitu nahi den mezua
     * @return mezu garbitua
     */
    public static String filtratuKaraktereak(String testua) {

        testua = testua.replaceAll("[\\u2800-\\u28FF]", ""); //BRAILLE TESTUA EZABATU

        testua = parseToAliases(testua); //EMOJIAK TESTURA PASATU
        testua = puntuazioMarkaKendu(testua); //PUNTUAZIO MARKAK EZABATU

        testua = testua.replaceAll("[0-9]", ""); //ZENBAKIAK EZABATU
        testua = testua.replaceAll("’", "").replace("\"", "").replace("-", ""); //KOMILLAK EZABATU
        testua = testua.replace("\r\n", " ").replace("\n", " ").replace("\r", " ");//ENTER-AK EZABATU

        //20 KARAKTERE BAINO GEHIAGOKO HITZAK KENDU
        String regex = "\\b\\w{20,}\\b";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(testua);
        if (matcher.find()) {
            testua = testua.replace(matcher.group(), "");
        }

        testua = testua.replaceAll("\\b\\w{1}\\b", ""); //KARAKTERE BAKARREKO HITZAK EZABATU

        //ZENBAKIAK EDO LETRAK EZ DIREN ELEMENTU DENAK KENDU
        if (!testua.matches(".*[a-zA-Z0-9]+.*")) {
            testua = " ";
        }
        //TESTUA KOMILLEN ARTEAN BUELTATU
        return "\"" + testua + "\",";

    }

    /**
     * Instantzia sorta bat jasota, mezu oso laburrak eta mezo oso luzeak dituzten mezuak kenduko dira.
     * @param mat jasoko den instantzia sorta atribuekin banatuta, matrize moduan
     * @return garbitutako instantzia sorta (instantzia matrize moduan)
     */
    public static String[][] garbituInstantzienMatrizea(String[][] mat) {

        System.out.println("GARBITU AURRETIK INSTANTZIA KOPURUA: " + mat.length+ "\n");
        String[][] aux = new String[mat.length][mat[0].length];
        int i = 0;
        for (String[] UnekoInstantzia : mat) {
            if (UnekoInstantzia[2].equals("\'suicide\'") || UnekoInstantzia[2].equals("\'non-suicide\'")) {
                if (70 < UnekoInstantzia[1].length() && UnekoInstantzia[1].length() < 1500) { //TESTUAREN LUZEERA FILTRATU
                    aux[i] = UnekoInstantzia;
                    i++;
                }
            }
        }
        String[][] matrizeGarb = new String[i][aux[0].length];
        for (int j = 0; j < i; j++) {
            matrizeGarb[j] = aux[j];
        }
        System.out.println("GARBITU OSTEAN INSTANTZIA KOPURUA: " + matrizeGarb.length + "\n");
        return matrizeGarb;

    }

    /**
     * Jasotako CSV bat hartuta, CSV horretan dauden instantzien lista itzuliko da.
     * @param path CSV fitxategiaren path-a
     * @return itzuliko den instantzien lista
     * @throws IOException
     */
    public static String[] getInstantzienLista(String path) throws IOException {

        //CSV FITXATEGIA IRAKURRI
        Path csvPath = Path.of(path);
        String csvEdukia = Files.readString(csvPath);

        //INSTANTZIAK BANATU
        String[] instantzien_lista = csvEdukia.split("(?<=suicide\n)");
        return instantzien_lista;

    }

    /**
     * Jasotako String bati puntuazio marka guztiak kentzen zaizkio. Puntuazio markak: !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
     * @param testua puntuazio markak kenduko zaizkion testua
     * @return puntuazio markarik gabeko String-a
     */
    public static String puntuazioMarkaKendu(String testua) {
        return testua.replaceAll("\\p{Punct}", " "); //PUNTUAZIO MARKAK EZABATU
    }
}