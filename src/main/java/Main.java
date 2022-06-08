import exceptions.FileTooLargetException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

public class Main {
    private static final String FILE_TO_ENCRYPT = "src/main/resources/testImage.bmp";
    private static final String FILE_ENCRYPTED = "src/main/resources/encrypted.bmp";
    private static final String STEGANOGRAPED_FILE = "src/main/resources/stegenograped.bmp";

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        SecretKey key = GeneratedSecretKey.getKeyFromPassword("PBKDF2WithHmacSHA256","AES","password", "salt");
        AESEncoder encoder = new AESEncoder("AES/CBC/PKCS5Padding",FILE_TO_ENCRYPT,FILE_ENCRYPTED,key,AESEncoder.generateIv());

        encoder.encryptFile();
        File outputfile = new File(FILE_ENCRYPTED);
        FileInputStream f1 = new FileInputStream(outputfile);
        BufferedImage bufferedImage = ImageIO.read(Objects.requireNonNull(Main.class.getResource("medianoche1.bmp")));
        byte[] arr = f1.readAllBytes();
        BMPSteganographEncoder steganograph = new BMPSteganographEncoder(bufferedImage, arr);
        try {
            steganograph.LSB1();
            steganograph.getEditor().outputToFile(STEGANOGRAPED_FILE);
        } catch (FileTooLargetException e) {
            e.printStackTrace();
        }


    }

}
