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
        byte[] arr = null;
        if(argsParser.encodeMode) {
            argsParser.encoder.encryptFile();
            FileInputStream f1 = new FileInputStream(argsParser.encoder.getEncryptedFile());
            arr = f1.readAllBytes();
        }
        BufferedImage bufferedImage = ImageIO.read(Objects.requireNonNull(Main.class.getResource(argsParser.fileCarrier.getPath())));
        BMPSteganographEncoder steganograph = new BMPSteganographEncoder(bufferedImage, arr);
        try {
            switch (argsParser.stegMode){
                case "LSB1":
                    steganograph.LSB1();
                    break;
                case "LSB4":
                    steganograph.LSB4();
                    break;
                case "LSBI":
                    steganograph.LSBImproved();
                    break;
                default:
                    System.out.println("SIN STEGMODE");
            }
            steganograph.getEditor().outputToFile(argsParser.fileSteganographed.getPath());
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
