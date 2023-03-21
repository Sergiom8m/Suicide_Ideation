import Aurreprozesamendua.fssInfoGain;
import Aurreprozesamendua.getTestFSS;
import Aurreprozesamendua.getARFF;
import Aurreprozesamendua.getBowArff;

import java.util.Scanner;

import static java.lang.System.exit;

public class MainProgram {
    public static void main(String[] args) throws Exception {
        int parametroak[] = new int[]{0, 37, 125};
        do{
            System.out.println("Sartu zenbaki bat segun eta zer egin nahi duzun:\n" +
                    "0 -> IRTEN\n"+
                    "1 -> .arff nagusia sortu\n"+
                    "2 -> BoW fitxategia sortu\n"+
                    "3 -> Baseline atera\n"+
                    "4 -> FSS aplikatu train multzoari\n"+
                    "5 -> FSS aplikatu test multzoari\n"+
                    "6 -> Random Forest modeloa sortu\n"+
                    "7 -> Ebaluazioa egin\n"+
                    "8 -> Iragarpenak egin\n");

            Scanner scanner = new Scanner(System.in);
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
                    getARFF.getArff("Suicide_Detection.csv", "cleanData.arff", ehuneko);
                    break;
                case 2:
                    System.out.println("---------------------------------------------------");
                    System.out.println("BoW SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    getBowArff.main("cleanData.arff", "hiztegia.txt", "trainBoW.arff", "test.arff");
                    break;
                case 3:
                    System.out.println("---------------------------------------------------");
                    System.out.println("BASELINE SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    Baseline.baseline("trainBoW.arff");
                    break;
                case 4:
                    System.out.println("---------------------------------------------------");
                    System.out.println("BoW TRAIN FSS SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    fssInfoGain.fssInfoGain("trainBOW.arff","trainFSS.arff");
                    break;
                case 5:
                    System.out.println("---------------------------------------------------");
                    System.out.println("BoW TEST FSS SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    getTestFSS.main("test.arff","testFSS.arff");
                    break;
                case 6:
                    System.out.println("---------------------------------------------------");
                    System.out.println("RANDOM FOREST SORTUKO DA");
                    System.out.println("---------------------------------------------------");
                    parametroak = RandomForestOptimoa.main(new String[]{"trainFSS.arff","."});
                    break;

                case 7:
                    System.out.println("---------------------------------------------------");
                    System.out.println("EBALUAZIOA BURUTUKO DA");
                    System.out.println("---------------------------------------------------");
                    Ebaluazioa.main("trainFSS.arff", new int[]{parametroak[1],parametroak[2],16,parametroak[0]}, "test_predictions.txt");
                    break;
                case 8:
                    System.out.println("---------------------------------------------------");
                    System.out.println("IRAGARPENAK BURUTUKO DIRA");
                    System.out.println("---------------------------------------------------");
                    Iragarpenak.main("RF.model","testFSS.arff","iragarpenak.txt");
                    break;
                default:
                    System.out.println("Sartu baliozko zenbakia");
                    break;
            }
        }while(true);


    }
}