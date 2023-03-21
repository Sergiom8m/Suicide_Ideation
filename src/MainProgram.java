import Aurreprozesamendua.fssInfoGain;
import Aurreprozesamendua.getTestFSS;
import Aurreprozesamendua.getARFF;
import Aurreprozesamendua.getBowArff;

import java.util.Scanner;

import static java.lang.System.exit;

public class MainProgram {
    public static void main(String[] args) throws Exception {
        int parametroak[] = new int[]{0, 37, 125};
        System.out.println("Sartu fitxategien path-a (soilik karpeta, adib: /home/lsi/): ");
        Scanner scanner = new Scanner(System.in);
        String path = scanner.next();
        //path="";
        do{
            System.out.println("Sartu zenbaki bat segun eta zer egin nahi duzun:\n" +
                    "0 -> IRTEN\n"+
                    "1 -> .arff nagusia sortu\n"+
                    "2 -> BoW fitxategia sortu\n"+
                    "3 -> Baseline atera\n"+
                    "4 -> FSS aplikatu train multzoari\n"+
                    "5 -> FSS aplikatu dev multzoari\n"+
                    "6 -> Random Forest modeloa sortu\n"+
                    "7 -> Ebaluazioa egin\n"+
                    "8 -> Iragarpenak egin\n"+
                    "9 -> Path aldatu\n");

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
                    getARFF.getArff(path+"Suicide_Detection.csv", path+"cleanData.arff", ehuneko,path+"dev.arff");
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
                    getBowArff.main(path+"cleanData.arff",bektorea,sparse, path+"hiztegia.txt", path+"trainBoW.arff", path+"devBoW.arff");
                    break;
                case 3:
                    System.out.println("---------------------------------------------------");
                    System.out.println("BASELINE SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    Baseline.baseline(path+"trainBoW.arff");
                    break;
                case 4:
                    System.out.println("---------------------------------------------------");
                    System.out.println("BoW TRAIN FSS SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    fssInfoGain.fssInfoGain(path+"trainBOW.arff",path+"trainFSS.arff");
                    break;
                case 5:
                    System.out.println("---------------------------------------------------");
                    System.out.println("BoW DEV FSS SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    getTestFSS.main(path+"dev.arff",path+"devFSS.arff",0,1);
                    break;
                case 6:
                    System.out.println("---------------------------------------------------");
                    System.out.println("RANDOM FOREST SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    parametroak = RandomForestOptimoa.main(new String[]{path+"trainFSS.arff",path+"."});
                    break;

                case 7:
                    System.out.println("---------------------------------------------------");
                    System.out.println("EBALUAZIOA BURUTUKO DA");
                    System.out.println("---------------------------------------------------");
                    Ebaluazioa.main(path+"trainFSS.arff", new int[]{parametroak[1],parametroak[2],16,parametroak[0]}, path+"test_predictions.txt");
                    break;
                case 8:
                    System.out.println("---------------------------------------------------");
                    System.out.println("IRAGARPENAK BURUTUKO DIRA");
                    System.out.println("---------------------------------------------------");
                    System.out.println("Modeloaren path-a sartu (.model): ");
                    String pathModel = scanner.next();
                    Iragarpenak.main(pathModel,path+"devFSS.arff",path+"iragarpenak.txt");
                    break;
                case 9:
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