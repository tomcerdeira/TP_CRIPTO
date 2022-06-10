import exceptions.FileTooLargetException;

import javax.crypto.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Objects;

public class Main {
    private static final String FILE_TO_ENCRYPT = "src/main/resources/testImage.bmp";
    private static final String FILE_ENCRYPTED = "src/main/resources/encrypted.bmp";
    private static final String STEGANOGRAPED_FILE = "src/main/resources/stegenograped.bmp";

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException{

        ArgumentsParser argsParser = new ArgumentsParser(args);
        File fileToEncrypt = new File(Arrays.stream(argsParser.getArgumentValue("in")).findAny().get());
        File fileCarrier = new File(Arrays.stream(argsParser.getArgumentValue("p")).findAny().get());
        File fileSteganographed = new File(Arrays.stream(argsParser.getArgumentValue("out")).findAny().get());
        String stegMode = Arrays.stream(argsParser.getArgumentValue("steg")).findAny().get();
        String encodeMode = "AES/CBC/PKCS5Padding"; // aes128 en modo CBC por default.
        Integer keyLen = 128;





        SecretKey keyForAes = GeneratedSecretKey.getKeyFromPassword("PBKDF2WithHmacSHA256","AES","password", "salt",192);

        Encoder aesEncoder = new AESEncoder("AES/CBC/PKCS5Padding",FILE_TO_ENCRYPT,FILE_ENCRYPTED, keyForAes, AESEncoder.generateIv());

        aesEncoder.encryptFile();
        FileInputStream f1 = new FileInputStream(fileSteganographed);
        BufferedImage bufferedImage = ImageIO.read(Objects.requireNonNull(Main.class.getResource("medianoche1.bmp")));
        byte[] arr = f1.readAllBytes();
        BMPSteganographEncoder steganograph = new BMPSteganographEncoder(bufferedImage, arr);
        try {
            steganograph.LSB1();
            steganograph.getEditor().outputToFile(STEGANOGRAPED_FILE);
        } catch (FileTooLargetException e) {
            e.printStackTrace();
        }

        ////////////////////////// Para DES

//        String passwordForDes = "password";
//        SecretKey keyForDes = SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(passwordForDes.getBytes()));
//
//        DESEncoder desEncoder = new DESEncoder(FILE_TO_ENCRYPT, FILE_ENCRYPTED, keyForDes);
//
//        // Test Encrypt
//        desEncoder.encryptFile();
//
//        // Test Decrypt
//        //desEncoder.decryptFile("src/main/resources/encrypted.bmp", "src/main/resources/inverseDES.bmp");

    }

}
