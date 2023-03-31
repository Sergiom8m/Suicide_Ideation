package Aurreprozesamendua;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.FixedDictionaryStringToWordVector;
import weka.filters.unsupervised.attribute.Reorder;
import weka.filters.unsupervised.instance.SparseToNonSparse;

import java.io.*;

public class MakeComp {

    /**
     *<h3>Aurre-baldintzak:</h3>
     * <ol>
     *     <li> parametro bezala bateragarri egin nahi den .arff fitxategia</li>
     *     <li> parametro bezala bateragarria den .arff fitxategia</li>
     *     <li> parametro bezala bektorearen errepresentazioa: BoW (0) edo TF-IDF(1)</li>
     *     <li> parametro bezala Sparse edo NonSparse: Sparse (0) edo NonSparse(1)</li>
     *     <li> parametro bezala FSS hiztegiaren .txt fitxategia</li>
     *</ol>
     *
     * <h3>Ondorengo-baldintzak:</h3>
     * <ol>
     *      <li> fitxategi bezala 2. parametroan adierazitako .arff fitxategia</li>
     *</ol>
     * <h3>Exekuzio-adibidea:</h3>
     *      java -jar MakeComp.jar path/to/inputData.arff path/to/irteerako/dataFSS.arff "0/1" "0/1" path/to/hiztegiaFSS.txt
     */
    public static void main (String[] args){
        try{
            makeComp(args[0], args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]), args[4]);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("\nZeozer gaizki sartu da. Exekuzio adibidea: \n" +
                    "\t\t\tjava -jar MakeComp.jar path/to/inputData.arff path/to/irteerako/dataFSS.arff \"0/1\" \"0/1\" path/to/hiztegiaFSS.txt\n\n");
        }

    }

    /**
     * Jasotako instantzia multzo baten arff fitxategiaren path-a, hiztegi bati egokituko zaio. Instantzia multzoa,
     * errepresentazio bektoriala aplikatu gabe dago. Beraz, sartutako aukeren arabera, errepresentazio bektoriala
     * sortuko da eta adierazitako hiztegi bati egokitiko da.
     * @param inputPath instantzia multzoaren arff fitxategiaren path-a
     * @param outputFSSPath egokitutako instantzia multzoaren arff fitxategiaren path-a
     * @param errepresentazioBektoriala errepresentazio bektorial mota. 0 --> BoW; 1 --> TFIDF
     * @param sparse 0 --> Sparse; 1 --> Non-sparse
     * @param hiztegiFSS instantzia multzoa egokituko den hiztegiaren path-a
     * @throws Exception
     */
    public static void makeComp(String inputPath, String outputFSSPath,int errepresentazioBektoriala,int sparse, String hiztegiFSS) throws Exception {

            System.out.println("ONDORENGO FITXATEGIA KONPATIBLEA BIHURTUKO DA (FSS): " + inputPath + "\n");

            //TEST FSS EGIN
            ConverterUtils.DataSource source= new ConverterUtils.DataSource(inputPath);
            Instances data = source.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);

            //IZENA ALDATU BEHAR ZAIO KLASEARI StringToWordVector EGIN AHAL IZATEKO
            data.renameAttribute(data.numAttributes()-1, "klasea");
            data.setClassIndex(data.numAttributes()-1);

            Instances testFSS= fixedDictionaryStringToWordVector(hiztegiFSS,data,errepresentazioBektoriala);


            // SPARSE/NONSPARSE
            if(sparse==1){
                testFSS = SparseToNonSparse(testFSS);
            }

            testFSS=reorder(testFSS);

            datuakGorde(outputFSSPath,testFSS);

    }

    /**
     * Instantzia multzo bat jasota, errepresentazio bektorial bat sortuko da hiztegi batekiko.
     * @param hiztegia instantzia multzoa egokituko den hiztegia
     * @param data jasotako instantzia multzoa
     * @param bektorea 0 --> BoW; 1 --> TFIDF
     * @return
     * @throws Exception
     */
    private static Instances fixedDictionaryStringToWordVector(String hiztegia, Instances data, int bektorea) throws Exception {
        FixedDictionaryStringToWordVector fixedDict= new FixedDictionaryStringToWordVector();
        if(bektorea==1){
            fixedDict.setOutputWordCounts(true);
            fixedDict.setIDFTransform(true);
            fixedDict.setTFTransform(true);
        }
        else{
            fixedDict.setOutputWordCounts(false);
        }
        fixedDict.setAttributeIndices("first-last");
        fixedDict.setLowerCaseTokens(true);
        fixedDict.setDictionaryFile(new File(hiztegia));
        fixedDict.setInputFormat(data);
        Instances dataFSS= Filter.useFilter(data,fixedDict);
        return dataFSS;
    }

    /**
     * 'test' instantzia multzoari reorder
     * @param test reorder filtroa aplikatuko zaion parametroa
     * @return reorder aplikatutako instantzia multzoa
     * @throws Exception
     */
    private static Instances reorder(Instances test) throws Exception {
        Reorder filterR = new Reorder();
        filterR.setAttributeIndices("2-"+ test.numAttributes()+",1"); //2-atributu kop, 1.  2-atributu kop bitarteko atributuak goian jarriko dira eta 1 atributua (klasea dena) amaieran.
        filterR.setInputFormat(test);
        test = Filter.useFilter(test,filterR);
        return test;
    }

    /**
     * Jasotako instantzia multzo bat jasota adierazitako path-ean gordeko da arff formatuan
     * @param path arff fitxategia gordeko den path-a
     * @param data instantzia multzoa
     * @throws Exception
     */
    private static void datuakGorde(String path, Instances data) throws Exception {
        ArffSaver s = new ArffSaver();
        s.setInstances(data);
        s.setFile(new File(path));
        s.writeBatch();
    }

    /**
     * Jasotako instantzia multzo bat jasota, Non-Sparse bihutuko da
     * @param data jasotako instantzia multzoa
     * @return non-sparse motako instantzia multzoa
     * @throws Exception
     */
    private static Instances SparseToNonSparse(Instances data) throws Exception{
        SparseToNonSparse filterNonSparse = new SparseToNonSparse();
        filterNonSparse.setInputFormat(data);
        Instances nonSparseData = Filter.useFilter(data,filterNonSparse);
        return nonSparseData;
    }

}