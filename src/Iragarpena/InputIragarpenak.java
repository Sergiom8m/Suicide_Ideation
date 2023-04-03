package Iragarpena;

import Aurreprozesamendua.MakeComp;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.vdurmont.emoji.EmojiParser.parseToAliases;

public class InputIragarpenak {

    private static boolean klasea;

    /**
     *<h3>Aurre-baldintzak:</h3>
     * <ol>
     *     <li> parametro bezala instantzia berriak dituen .csv fitxategia</li>
     *     <li> parametro bezala test-blindRAW gordetzeko .arff fitxategiaren izena</li>
     *     <li> parametro bezala FSS hiztegiaren .txt fitxategia</li>
     *</ol>
     *
     * <h3>Ondorengo-baldintzak:</h3>
     * <ol>
     *      <li> fitxategi bezala 2. parametroan adierazitako .arff fitxategia</li>
     *</ol>
     * <h3>Exekuzio-adibidea:</h3>
     *      java -jar InputIragarpenak.jar path/to/Predictions.csv path/to/test_blind.arff path/to/irteerako/hiztegiaFSS.txt
     */
    public static void main(String[] args) {
        try {
            klasea=false;
            datuakPrestatu(args[0], args[1], args[2], args[3]);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("\nZeozer gaizki sartu da. Exekuzio adibidea: \n" +
                    "\t\t\tjava -jar InputIragarpenak.jar path/to/Predictions.csv path/to/test_blind.arff path/to/test_input.arff path/to/irteerako/hiztegiaFSS.txt\n\n");
        }
    }

    /**
     * Iragarpenak egiteko jasoko den CSV, pasatutako hiztegi batera egokitzen da.
     * @param csvPath jasotako instantzien CSV fitxategiaren path-a
     * @param arffPath CSV-a arff formatuan gordetzen den fitxategiaren path-a, hiztegira egokitu gabe
     * @param blindFSSPath hiztegira egokituta gordetzen den arff fitxategiaren path-a
     * @param hiztegiPath instantzien atributuak egokituko diren hiztegiaren path-a
     * @throws Exception
     */
    public static void datuakPrestatu(String csvPath, String arffPath, String blindFSSPath, String hiztegiPath) throws Exception {
        getArff(csvPath, arffPath);
        MakeComp.makeComp(arffPath, blindFSSPath, 0, 1, hiztegiPath);

    }

    /**
     * Instantzien multzoa CSV bezala jasota, ARFF batean transofrmatuko da
     * @param csvPath jasotako instanzien multzoaren CSV-aren path-a
     * @param arffPath gordeko den instantzien multzoaren arff-aren path-a
     * @throws Exception
     */
    public static void getArff(String csvPath, String arffPath) throws Exception {

        System.out.println("CSV BATETIK ABIATUTA ARFF FITXATEGI GARBI BAT SORTUKO DA" + "\n");

        //INSTANTZIEN IRAKURKETA:
        String[] instantzien_lista = getInstantzienLista(csvPath);
        String[][] instantzienMatrizea = getInstantzienMatrizea(instantzien_lista);

        //ARFF-A SORTU
        arffGoiburuaEzarri(instantzien_lista, arffPath);
        instantziakSartuArff(instantzienMatrizea, arffPath);
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
        String[] instantzien_lista = csvEdukia.split("(?<=NaN\r\n)");
        if (instantzien_lista.length==1){instantzien_lista = csvEdukia.split("(?<=NaN\n)");}
        if (instantzien_lista.length==1){instantzien_lista = csvEdukia.split("(?<=suicide\n)");klasea=true;}
        if (instantzien_lista.length==1){instantzien_lista = csvEdukia.split("(?<=suicide\r\n)");}
        return instantzien_lista;
    }

    /**
     * Instantzien lerroak jasota, instantzien atributuengatik banatzen duen metodoa. 'lista'-ren balio bakoitzeko,
     * identifikatzailea, klasea eta testua lortzen da.
     * @param lista instantizen lista
     * @return instantzien lista eta instantzia bakoitzeko bere identifikatzailea, klasea eta testua
     */
    public static String[][] getInstantzienMatrizea(String[] lista) {

        //INSTANZIA KOP X 3 DIMENTSIOKO MATRIZEA SORTU
        String[][] instantziaMat = new String[lista.length][2];
        for (int i = 0; i < lista.length; i++) {
            String unekoInstantzia = lista[i]; //INSTANTZIA LERRO OSOA
            String[] l = unekoInstantzia.split(",");
            if(!klasea){instantziaMat[i][1] = "?";} //KLASEA
            else{instantziaMat[i][1] ="\'" + l[l.length - 1].replace("\n", "").replace("\r", "") + "\'";}
            instantziaMat[i][0] = garbituTestua(l); //TESTUA
        }
        return instantziaMat;
    }

    /**
     * Jasotako mezu bat hartuz, mezu horren garbiketa egingo da.
     * @param l filtratu behar diren instantzia sorta
     * @return
     */
    public static String garbituTestua(String[] l) {

        StringBuilder erdikoa = new StringBuilder();
        boolean sartu = false;
        for (int j = 0; j < l.length-1; j++) {
            sartu=true;
            erdikoa.append(l[j]).append(" ");
        }
        if(!sartu){erdikoa.append(l[0]);}
        return filtratuKaraktereak(erdikoa.toString());
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
        myWriter.append("\n" + "@attribute text string");
        myWriter.append("\n" + "@attribute class {'suicide', 'non-suicide'}");

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
            myWriter.append("\n" + instantzien_matrizea[i][0] + " " + instantzien_matrizea[i][1]);
        }
        myWriter.close();
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
     * Jasotako String bati puntuazio marka guztiak kentzen zaizkio. Puntuazio markak: !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
     * @param testua puntuazio markak kenduko zaizkion testua
     * @return puntuazio markarik gabeko String-a
     */
    public static String puntuazioMarkaKendu(String testua) {

        return testua.replaceAll("\\p{Punct}", " "); //PUNTUAZIO MARKAK EZABATU

    }
}
