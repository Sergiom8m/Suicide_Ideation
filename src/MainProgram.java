import Aurreprozesamendua.*;

import java.util.Scanner;

import static java.lang.System.exit;

public class MainProgram {
    public static void main(String[] args) throws Exception {

            System.out.println("---------------------------------------------------");
            System.out.println("DATUAK KARGATUKO DIRA ARFF FORMATUAN");
            System.out.println("---------------------------------------------------");
            System.out.println("Sartu hartzeko instantzien ehunekoa: ");
            getARFF.getArff("Suicide_Detection.csv", "dataRAW.arff", 29,"testRAW.arff");

            System.out.println("---------------------------------------------------");
            System.out.println("BoW SORTUKO DA");
            System.out.println("---------------------------------------------------");
            // hoberena bektorea=0 eta sparse=1 da
            getBowArff.main(new String[]{"dataRAW.arff","0","1", "hiztegia.txt", "trainBoW.arff", "devRAW.arff"});

            System.out.println("---------------------------------------------------");
            System.out.println("BoW TRAIN FSS SORTUKO DA");
            System.out.println("---------------------------------------------------");
            fssInfoGain.fssInfoGain("trainBOW.arff","trainFSS.arff", "hiztegia.txt","hiztegiaFSS.txt");

            System.out.println("---------------------------------------------------");
            System.out.println("BoW DEV FSS SORTUKO DA");
            System.out.println("---------------------------------------------------");
            MakeComp.main("devRAW.arff","devFSS.arff",0,1);

            System.out.println("---------------------------------------------------");
            System.out.println("BoW TEST FSS SORTUKO DA");
            System.out.println("---------------------------------------------------");
            MakeComp.main("testRAW.arff","testFSS.arff",0,1);

            System.out.println("---------------------------------------------------");
            System.out.println("BoW DATA FSS SORTUKO DA");
            System.out.println("---------------------------------------------------");
            MakeComp.main("dataRAW.arff","dataFSS.arff",0,1);

            System.out.println("---------------------------------------------------");
            System.out.println("BASELINE SORTUKO DA");      //TODO
            System.out.println("---------------------------------------------------");
            Baseline.baseline("dataFSS.arff", "trainFSS.arff", "devFSS.arff", "BaselineEmaitzak.txt");

            System.out.println("---------------------------------------------------");
            System.out.println("RANDOM FOREST SORTUKO DA"); //TODO
            System.out.println("---------------------------------------------------");
            System.out.println("---------------------------------------------------");
            System.out.println("EBALUAZIOA BURUTUKO DA");
            System.out.println("---------------------------------------------------");
            Ebaluazioa.ebaluazioa("trainFSS.arff", "devFSS.arff", "dataFSS.arff", 297, 100, 100, 109, "textPred.txt");

            System.out.println("---------------------------------------------------");
            System.out.println("IRAGARPENAK BURUTUKO DIRA");
            System.out.println("---------------------------------------------------");
            Iragarpenak.main(new String[]{"RF.model","testFSS.arff","iragarpenak.txt"});
            System.out.println("Sartu baliozko zenbakia");

    }
}