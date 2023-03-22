import Aurreprozesamendua.fssInfoGain;
import Aurreprozesamendua.MakeComp;
import Aurreprozesamendua.getARFF;
import Aurreprozesamendua.getBowArff;

import java.util.Scanner;

import static java.lang.System.exit;

public class MainProgram {
    public static void main(String[] args) throws Exception {
        int parametroak[] = new int[]{40, 125, 100,58};
        System.out.println("Sartu fitxategien path-a (soilik karpeta, adib: /home/lsi/): ");
        Scanner scanner = new Scanner(System.in);
        String path = scanner.next();
        path="";
        do{
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
                    "10 -> Iragarpenak egin\n"+
                    "11 -> Path aldatu\n");

            int aukera = scanner.nextInt();

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
                    getARFF.getArff(path+"Suicide_Detection.csv", path+"DataRAW.arff", ehuneko,path+"TestRAW.arff");
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
                    getBowArff.main(path+"DataRAW.arff",bektorea,sparse, path+"hiztegia.txt", path+"trainBoW.arff", path+"devRAW.arff");
                    break;
                case 3:
                    System.out.println("---------------------------------------------------");
                    System.out.println("BoW TRAIN FSS SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    fssInfoGain.fssInfoGain(path+"trainBOW.arff",path+"trainFSS.arff");
                    break;
                case 4:
                    System.out.println("---------------------------------------------------");
                    System.out.println("BoW DEV FSS SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    MakeComp.main(path+"devRAW.arff",path+"devFSS.arff",0,1);
                    break;
                case 5:
                    System.out.println("---------------------------------------------------");
                    System.out.println("BoW TEST FSS SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    MakeComp.main(path+"TestRAW.arff",path+"testFSS.arff",0,1);
                    break;
                case 6:
                    System.out.println("---------------------------------------------------");
                    System.out.println("BoW DATA FSS SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    MakeComp.main(path+"DataRAW.arff",path+"dataFSS.arff",0,1);
                    break;
                case 7:
                    System.out.println("---------------------------------------------------");
                    System.out.println("BASELINE SORTUKO DA");      //TODO
                    System.out.println("---------------------------------------------------");
                    Baseline.baseline(path+"trainFSS.arff","BaselineEmaitzak.txt");
                    break;
                case 8:
                    System.out.println("---------------------------------------------------");
                    System.out.println("RANDOM FOREST SORTUKO DA"); //TODO
                    System.out.println("---------------------------------------------------");
                    parametroak = RandomForestOptimoa.main(new String[]{path+"trainFSS.arff",path+"."});
                    break;

                case 9:
                    System.out.println("---------------------------------------------------");
                    System.out.println("EBALUAZIOA BURUTUKO DA");
                    System.out.println("---------------------------------------------------");
                    Ebaluazioa.main(path+"trainFSS.arff","devFSS.arff","dataFSS.arff", parametroak, path+"test_predictions.txt");
                    break;
                case 10:
                    System.out.println("---------------------------------------------------");
                    System.out.println("IRAGARPENAK BURUTUKO DIRA");
                    System.out.println("---------------------------------------------------");
                    System.out.println("Modeloaren path-a sartu (.model): ");
                    String pathModel = scanner.next();
                    Iragarpenak.main(pathModel,path+"devFSS.arff",path+"iragarpenak.txt");
                    break;
                case 11:
                    System.out.println("Sartu fitxategien path-a (soilik karpeta, adib: /home/lsi/): ");
                    path = scanner.next();
                    break;
                default:
                    System.out.println("Sartu baliozko zenbakia");
                    break;
            }
        }while(true);
    }
}