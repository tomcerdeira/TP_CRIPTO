import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Main {

    public static void main(String[] args) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(Objects.requireNonNull(Main.class.getResource("testImage.bmp")));

        BMPSteganographEncoder steganograph = new BMPSteganographEncoder(bufferedImage, null);

    }

}
