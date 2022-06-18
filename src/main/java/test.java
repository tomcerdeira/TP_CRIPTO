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

//unsigned char ucDataBlock[36] = {
//	// Offset 0x0000000A to 0x0000000D
//	0x360x000x000x00,
//
//	// Offset 0x00000036 to 0x00000055
//	111111111111,
//	11110xFE10xFE10xFE0xFE0xFE0xFE,
//	10xFE10xFE0xFE0xFE0xFE0xFE
//}
