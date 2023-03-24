import Aurreprozesamendua.*;

import java.util.Scanner;

import static java.lang.System.exit;
/*
public class MainProgram {
    public static void main(String[] args) throws Exception {
        int parametroak[] = new int[]{40, 125, 100,58};
        //System.out.println("Sartu fitxategien path-a (soilik karpeta, adib: /home/lsi/): ");
        Scanner scanner = new Scanner(System.in);
        //String path = scanner.next();
        //path="";

        do{
            int aukera;
            System.out.println("Sartu zenbaki bat segun eta zer egin nahi duzun:\n" +
                    "0 -> IRTEN\n"+
                    "1 -> .arff nagusia sortu\n"+
                    "2 -> BoW fitxategia sortu\n"+
                    "3 -> FSS aplikatu train multzoari\n"+
                    "4 -> DevFSS egin\n"+
                    "5 -> TestFSS egin\n"+
                    "6 -> DataFSS egin\n"+
                    "7 -> Baseline atera\n"+
                    "8 -> Random Forest modeloa sortu\n"+
                    "9 -> Ebaluazioa egin\n"+
                    "10 -> Iragarpenak egin\n");

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
                    getARFF.getArff("Suicide_Detection.csv", "DataRAW.arff", ehuneko,"TestRAW.arff");
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
                    getBowArff.main("DataRAW.arff",bektorea,sparse, "hiztegia.txt", "trainBoW.arff", "devRAW.arff");
                    break;
                case 3:
                    System.out.println("---------------------------------------------------");
                    System.out.println("BoW TRAIN FSS SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    fssInfoGain.fssInfoGain("trainBOW.arff","trainFSS.arff", "hiztegia.txt","hiztegiaFSS.txt");
                    break;
                case 4:
                    System.out.println("---------------------------------------------------");
                    System.out.println("BoW DEV FSS SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    MakeComp.main("devRAW.arff","devFSS.arff",0,1);
                    break;
                case 5:
                    System.out.println("---------------------------------------------------");
                    System.out.println("BoW TEST FSS SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    MakeComp.main("TestRAW.arff","testFSS.arff",0,1);
                    break;
                case 6:
                    System.out.println("---------------------------------------------------");
                    System.out.println("BoW DATA FSS SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    MakeComp.main("DataRAW.arff","dataFSS.arff",0,1);
                    break;
                case 7:
                    System.out.println("---------------------------------------------------");
                    System.out.println("BASELINE SORTUKO DA");      //TODO
                    System.out.println("---------------------------------------------------");
                    Baseline.baseline("dataFSS.arff", "trainFSS.arff", "devFSS.arff", "BaselineEmaitzak.txt");
                    break;
                case 8:
                    System.out.println("---------------------------------------------------");
                    System.out.println("RANDOM FOREST SORTUKO DA"); //TODO
                    System.out.println("---------------------------------------------------");
                    parametroak = RandomForestOptimoa.main(new String[]{"trainFSS.arff","."});
                    break;

                case 9:
                    System.out.println("---------------------------------------------------");
                    System.out.println("EBALUAZIOA BURUTUKO DA");
                    System.out.println("---------------------------------------------------");

                    break;
                case 10:
                    System.out.println("---------------------------------------------------");
                    System.out.println("IRAGARPENAK BURUTUKO DIRA");
                    System.out.println("---------------------------------------------------");
                    System.out.println("Modeloaren path-a sartu (.model): ");
                    String pathModel = scanner.next();
                    Iragarpenak.main(pathModel,"devFSS.arff","iragarpenak.txt");
                    break;
                default:
                    System.out.println("Sartu baliozko zenbakia");
                    break;
            }
        }while(true);
    }
}

 */