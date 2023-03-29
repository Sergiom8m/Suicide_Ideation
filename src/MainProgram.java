import Aurreprozesamendua.MakeComp;
import Aurreprozesamendua.fssInfoGain;
import Aurreprozesamendua.getArff;
import Aurreprozesamendua.arff2bow;
import Ebaluazioa.*;
import Iragarpena.Iragarpenak;

public class MainProgram {
    public static void main(String[] args) throws Exception {

            System.out.println("---------------------------------------------------");
            System.out.println("DATUAK KARGATUKO DIRA ARFF FORMATUAN");
            System.out.println("---------------------------------------------------");
            System.out.println("Sartu hartzeko instantzien ehunekoa: ");
            getArff.getArff("Suicide_Detection.csv", "dataRAW.arff", 29,"testRAW.arff");

            System.out.println("---------------------------------------------------");
            System.out.println("BoW SORTUKO DA");
            System.out.println("---------------------------------------------------");
            // hoberena bektorea=0 eta sparse=1 da
            arff2bow.main(new String[]{"dataRAW.arff","0","1", "hiztegia.txt", "trainBoW.arff", "devRAW.arff"});

            System.out.println("---------------------------------------------------");
            System.out.println("BoW TRAIN FSS SORTUKO DA");
            System.out.println("---------------------------------------------------");
            fssInfoGain.fssInfoGain("trainBOW.arff","trainFSS.arff", "hiztegia.txt","hiztegiaFSS.txt");

            System.out.println("---------------------------------------------------");
            System.out.println("BoW DEV FSS SORTUKO DA");
            System.out.println("---------------------------------------------------");
            MakeComp.main(new String[]{"devRAW.arff","devFSS.arff",Integer.toString(0),Integer.toString(1),"hiztegiaFSS.txt"});

            System.out.println("---------------------------------------------------");
            System.out.println("BoW TEST FSS SORTUKO DA");
            System.out.println("---------------------------------------------------");
            MakeComp.main(new String[]{"testRAW.arff","testFSS.arff",Integer.toString(0),Integer.toString(1),"hiztegiaFSS.txt"});

            System.out.println("---------------------------------------------------");
            System.out.println("BoW DATA FSS SORTUKO DA");
            System.out.println("---------------------------------------------------");
            MakeComp.main(new String[]{"dataRAW.arff","dataFSS.arff",Integer.toString(0),Integer.toString(1),"hiztegiaFSS.txt"});

            System.out.println("---------------------------------------------------");
            System.out.println("BASELINE SORTUKO DA");
            System.out.println("---------------------------------------------------");
            Baseline.main(new String[]{"dataFSS.arff", "trainFSS.arff", "devFSS.arff", "BaselineEmaitzak.txt", "baseline.model"});

            System.out.println("---------------------------------------------------");
            System.out.println("RANDOM FOREST SORTUKO DA");
            System.out.println("---------------------------------------------------");
            System.out.println("---------------------------------------------------");
            System.out.println("EBALUAZIOA BURUTUKO DA");
            System.out.println("---------------------------------------------------");
            Ebaluazioa.main( new String[]{"dataFSS.arff", Integer.toString(297), Integer.toString(102), Integer.toString(100), Integer.toString(109), "textPred.txt", "RF.model"});

            System.out.println("---------------------------------------------------");
            System.out.println("IRAGARPENAK BURUTUKO DIRA");
            System.out.println("---------------------------------------------------");
            Iragarpenak.main(new String[]{"RF.model","testFSS.arff","iragarpenak.txt"});
            System.out.println("Sartu baliozko zenbakia");

    }
}