import Aurreprozesamendua.*;
import Ebaluazioa.*;
import Inferentzia.ParametroEkorketa;
import Iragarpena.InputIragarpenak;

import java.util.Scanner;

import static java.lang.System.exit;

public class MenuOsoa {
    public static void main(String[] args) throws Exception {
        int parametroak[] = new int[]{297, 102, 100, 109};
        Scanner scanner = new Scanner(System.in);
        do{
            int aukera;
            System.out.println("Sartu zenbaki bat segun eta zer egin nahi duzun:\n" +
                    ".MODEL LORTZEKO EGIN BEHARREKO PAUSUAK:\n"+
                    "   0 -> IRTEN\n"+
                    "   1 -> .arff nagusia sortu\n"+
                    "   2 -> BoW fitxategia sortu\n"+
                    "   3 -> FSS aplikatu train multzoari\n"+
                    "   4 -> DevFSS egin\n"+
                    "   5 -> TestFSS egin\n"+
                    "   6 -> DataFSS egin\n"+
                    "   7 -> Random Forest modeloa  (ez exekutatu, koste konputazional handia)\n"+
                    "AURREKO 3., 4., ETA 6. PAUSUEN FITXATEGIAK IZANDA: \n"+
                    "   8 -> Baseline atera\n"+
                    "   9 -> Ebaluazioa egin\n"+
                    ".MODEL IZANDA:\n"+
                    "   10 -> Iragarpena egin\n");

            aukera = scanner.nextInt();

            switch (aukera){
                case 0:
                    exit(0);
                    break;
                case 1:
                    System.out.println("---------------------------------------------------");
                    System.out.println("DATUAK KARGATUKO DIRA ARFF FORMATUAN");
                    System.out.println("---------------------------------------------------");
                    System.out.println("Sartu hartzeko instantzien ehunekoa: ");
                    int ehuneko = scanner.nextInt();
                    getArff.main(new String[]{"Suicide_Detection.csv", "dataRAW.arff", Integer.toString(ehuneko),"testRAW.arff"});
                    break;
                case 2:
                    System.out.println("---------------------------------------------------");
                    System.out.println("BoW SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    System.out.println("Sartu 0 --> BoW edo 1 --> TF-IDF");
                    int bektorea = scanner.nextInt();
                    System.out.println("Sartu 0 --> Sparse edo 1 --> NonSparse");
                    int sparse = scanner.nextInt();
                    // hoberena bektorea=0 eta sparse=1 da
                    arff2bow.main(new String[]{"dataRAW.arff", String.valueOf(bektorea), String.valueOf(sparse), "hiztegia.txt", "trainBOW.arff", "devRAW.arff"});
                    break;
                case 3:
                    System.out.println("---------------------------------------------------");
                    System.out.println("BoW TRAIN FSS SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    fssInfoGain.main(new String[]{"trainBOW.arff","trainFSS.arff", "hiztegia.txt","hiztegiaFSS.txt"});
                    break;
                case 4:
                    System.out.println("---------------------------------------------------");
                    System.out.println("BoW DEV FSS SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    MakeComp.main(new String[]{"devRAW.arff","devFSS.arff",Integer.toString(0),Integer.toString(1),"hiztegiaFSS.txt"});
                    break;
                case 5:
                    System.out.println("---------------------------------------------------");
                    System.out.println("BoW TEST FSS SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    MakeComp.main(new String[]{"testRAW.arff","testFSS.arff",Integer.toString(0),Integer.toString(1),"hiztegiaFSS.txt"});
                    break;
                case 6:
                    System.out.println("---------------------------------------------------");
                    System.out.println("BoW DATA FSS SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    MakeComp.main(new String[]{"dataRAW.arff","dataFSS.arff",Integer.toString(0),Integer.toString(1),"hiztegiaFSS.txt"});
                    break;

                case 7:
                    System.out.println("---------------------------------------------------");
                    System.out.println("RANDOM FOREST SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    ParametroEkorketa.main(
                            new String[]{"trainFSS.arff","devFSS.arff", "dataFSS.arff", "param_ekork_ema.txt",
                                         "80", "5", "120", "100", "20", "300", "102", "1", "102", "100", "1", "100"
                            });
                    break;
                case 8:
                    System.out.println("---------------------------------------------------");
                    System.out.println("BASELINE SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    Baseline.main(new String[]{"dataFSS.arff",  "BaselineEmaitzak.txt", "baseline.model"});
                    break;

                case 9:
                    System.out.println("---------------------------------------------------");
                    System.out.println("EBALUAZIOA BURUTUKO DA");
                    System.out.println("---------------------------------------------------");
                    Ebaluazioa.main(new String[]{"dataFSS.arff", Integer.toString(parametroak[0]), Integer.toString(parametroak[1]), Integer.toString(parametroak[2]), Integer.toString(parametroak[3]), "textPred.txt", "RF.model"});
                    break;
                case 10:
                    System.out.println("---------------------------------------------------");
                    System.out.println("IRAGARPENAK BURUTUKO DIRA");
                    System.out.println("---------------------------------------------------");
                    System.out.println("Modeloaren path-a sartu (.model): ");
                    String pathModel = scanner.next();
                    System.out.println("Zure instantziak dituen CSV-a sartu nahi duzu?\n" +
                            "Bai -> 0\n" +
                            "Ez (defektuz menuaren 1. aukeran sortzen dena erabiliko da) -> 1\n");
                    int berria=scanner.nextInt();
                    if (berria==0){
                        InputIragarpenak.main(new String[]{"Predictions.csv", "test_blind.arff", "hiztegiaFSS.txt"});
                        Iragarpena.Iragarpenak.main(new String[]{pathModel,"test_input.arff","iragarpenak.txt"});
                    }else{
                        Iragarpena.Iragarpenak.main(new String[]{pathModel,"testFSS.arff","iragarpenak.txt"});
                    }
                    break;
                default:
                    System.out.println("Sartu baliozko zenbakia");
                    break;
            }
        }while(true);
    }
}

