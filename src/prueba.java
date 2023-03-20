import javax.xml.crypto.Data;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class prueba {
    public static void main(String[] args) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd-Hms");
        String data = simpleDateFormat.format(new Date());
        System.out.println(data);
    }
}
