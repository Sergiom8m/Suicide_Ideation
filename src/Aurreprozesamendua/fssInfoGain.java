package Aurreprozesamendua;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;

import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.RemoveByName;

import java.io.*;
import java.util.HashMap;

public class fssInfoGain {

    /**
     *<h3>Aurre-baldintzak:</h3>
     * <ol>
     *     <li> parametro bezala train-aren bektore errepresentazio fitxategia</li>
     *     <li> parametro bezala atributuen hautapena egin ondoren lortutako .arff fitxategia</li>
     *     <li> parametro bezala hasierako hiztegiaren .txt fitxategia</li>
     *     <li> parametro bezala atributuen hautapenean lortutako atributuak gordetzeko .txt fitxategia</li>
     *</ol>
     *
     * <h3>Ondorengo-baldintzak:</h3>
     * <ol>
     *      <li> fitxategi bezala 2. parametroan adierazitako .arff fitxategia</li>
     *      <li> fitxategi bezala 4. parametroan adierazitako .txt fitxategia</li>
     *</ol>
     * <h3>Exekuzio-adibidea:</h3>
     *      java -jar fssInfoGain.jar path/to/trainBOW.arff path/to/irteerako/trainFSS.arf path/to/hiztegia.txt path/to/irteerako/hiztegiaFSS.txt
     */
    public static void main (String[] args){

        try{
            fssInfoGain(args[0], args[1], args[2], args[3]);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("\nZeozer gaizki sartu da. Exekuzio adibidea: \n" +
                    "\t\t\tjava -jar fssInfoGain.jar path/to/trainBOW.arff path/to/irteerako/trainFSS.arf path/to/hiztegia.txt path/to/irteerako/hiztegiaFSS.txt\n\n");
        }

    }

    /**
     * Errepresentazio bektoriala aplikatuta daukan instantzia multzo bat jasota, atributu hautapena aplikatzen zaio.
     * Behin aplikatuta, lortutako instantzia multzo berria eta hiztegi berria gordeko da.
     * @param trainBowPath errepresentazio bektorial moduan dagoen instantzia multzoa, atributu aukeraketa aplikatuko
     *                     zaiona
     * @param FSSArffPath FSS aplikatutako instantzia multzoa gordeko den direktorioa
     * @param hiztegiPath trainBow fitxategiaren erabiltzen duen hiztegia
     * @param hiztegiFSSpath behin atributu hautaketa aplikatuta, geratzen den hiztegiaren direktorioa. Bertan gordeko
     *                       da hiztegi berria
     */
    public static void fssInfoGain(String trainBowPath, String FSSArffPath, String hiztegiPath, String hiztegiFSSpath) {
        try {

            System.out.println("TRAIN MULTZOAREN GAINEAN ATRIBUTU HAUTAPENA GARATZEN ARI DA..." + "\n");

            //DATUAK LORTU
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(trainBowPath);
            Instances train = source.getDataSet();
            train.setClassIndex(train.numAttributes() - 1);

            train = ezabatuUselessAttributes(train);

            //ATRIBUTUEN HAUTAPENA EGIN
            AttributeSelection filterSelect = new AttributeSelection();
            InfoGainAttributeEval evalInfoGain = new InfoGainAttributeEval();
            Ranker ranker = new Ranker();
            ranker.setNumToSelect(2000);
            ranker.setThreshold(0.001);
            filterSelect.setInputFormat(train);
            filterSelect.setEvaluator(evalInfoGain);
            filterSelect.setSearch(ranker);
            Instances trainFSS = Filter.useFilter(train, filterSelect);

            //DATUAK GORDE
            datuakGorde(FSSArffPath, trainFSS);

            // HIZTEGIA SORTU ETA GORDE
            HashMap<String, Integer> hiztegia = hiztegiaSortu(hiztegiPath,trainFSS);
            hiztegiaGorde(hiztegia,hiztegiFSSpath,trainFSS);

        }catch (Exception e){e.printStackTrace();}
    }

    /**
     * Instantzia multzo bat jasota, erabilgarriak ez diren atributuk kenduko dira. Letrak eta zenbakiak dituzten atributuekin
     * bakarrik geratuko da.
     * @param data jasotako instantzi multzoa
     * @return erabilgarriak ez diren atributuak ezabatuta daukan instantzia multzoa
     * @throws Exception
     */
    private static Instances ezabatuUselessAttributes(Instances data) throws Exception {
        RemoveByName remove = new RemoveByName();
        remove.setExpression(".*[a-zA-Z0-9]+.*");
        remove.setInvertSelection(true);
        remove.setInputFormat(data);
        data = Filter.useFilter(data, remove);

        return data;
    }

    /**
     *
     * @param hiztegia
     * @param path
     * @param data
     * @throws IOException
     */
    public static void hiztegiaGorde(HashMap<String, Integer> hiztegia, String path, Instances data) throws IOException {
        FileWriter fw = new FileWriter(path);
        fw.write("@@@numDocs="+data.numInstances()+"@@@\n"); //Beharrezkoa TF·IDF bihurketa egiteko

        for(int i=0; i<data.numAttributes()-1;i++){
            String atributua = data.attribute(i).name();
            if(hiztegia.containsKey(atributua)){
                fw.write(atributua+","+hiztegia.get(atributua)+"\n");
            }
        }
        fw.close();
    }

    /**
     * Jasotako instantzi multzo batetik, instantzia multzo horrek duen dituen atributuak hiztegia osatuko dute.
     * @param pathRaw
     * @param data
     * @return sortutako hiztegia
     * @throws IOException
     */
    public static HashMap<String,Integer> hiztegiaSortu(String pathRaw, Instances data) throws IOException {
        HashMap<String, Integer> hiztegia = new HashMap();

        for(int i=0;i<data.numAttributes()-1;i++) {
            Attribute attrib = data.attribute(i);
            hiztegia.put(attrib.name(),1);
        }

        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(pathRaw));
            String contentLine = br.readLine();
            contentLine = br.readLine();            //pa que se salte el @numDocs=numinstances
            while (contentLine != null) {
                String[] lerroa = contentLine.split(",");
                String atributua = lerroa[0];
                Integer maiztasuna = Integer.parseInt(lerroa[1]);

                if(hiztegia.containsKey(atributua)){
                    hiztegia.put(atributua,maiztasuna);
                }
                contentLine = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();

        }

        return hiztegia;
    }

    /**
     * Jasotako instantzia multzo bat jasota adierazitako path-ean gordeko da arff formatuan
     * @param path arff fitxategia gordeko den path-a
     * @param data instantzia multzoa
     * @throws Exception
     */
    private static void datuakGorde(String path, Instances data) throws Exception {
        //INSTANTZIAK GORDE
        ArffSaver s = new ArffSaver();
        s.setInstances(data);
        s.setFile(new File(path));
        s.writeBatch();
    }

}