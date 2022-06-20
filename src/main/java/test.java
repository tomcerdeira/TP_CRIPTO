import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class test {

    public static void main(String[] args) throws IOException {

        BufferedImage bufferedImage = ImageIO.read(new File(" C:\\Users\\ignac\\Desktop\\test\\kings.bmp"));
//        FileInputStream f3 = new FileInputStream(System.getProperty("user.dir") + "\\src\\main\\resources\\ladoLSBI.bmp");
//
//        BMPSteganographDecoder decoder = new BMPSteganographDecoder(f3.readAllBytes(),false);
//
//        byte[] decoded = decoder.LSBI();
//
//        File file = new File("C:\\Users\\ignac\\Desktop\\test\\image.png");
//        OutputStream os = new FileOutputStream(file);
//        os.write(decoded);
//        os.close();
    }

}


//	00000000000010101111010111111110

