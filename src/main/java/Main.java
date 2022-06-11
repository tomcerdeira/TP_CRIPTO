import exceptions.FileTooLargetException;

import javax.crypto.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
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
        FileInputStream f1 = new FileInputStream(argsParser.fileToEncrypt);
        byte[] arr = f1.readAllBytes();
        if(argsParser.encodeMode) {
            argsParser.encoder.encryptFile();
            FileInputStream f2 = new FileInputStream(argsParser.encoder.getEncryptedFile());
            arr = f2.readAllBytes();
        }
        BufferedImage bufferedImage = ImageIO.read(Objects.requireNonNull(Main.class.getResource(argsParser.fileCarrier.getPath())));
        BMPSteganographEncoder steganograph = new BMPSteganographEncoder(bufferedImage, arr,"bmp");
        try {
            switch (argsParser.stegMode){
                case "LSB1":
                    steganograph.LSB1();
                    byte[] desencoded = steganograph.revertLSB1();
                    FileOutputStream outputStream = new FileOutputStream("desencodedLSB1.bmp");
                    outputStream.write(desencoded);
                    outputStream.close();
                    argsParser.encoder.decryptFile("desencodedLSB1.bmp","aesDesLSB1.bmp");
                    break;
                case "LSB4":
                    steganograph.LSB4();
                    byte[] desencodedLSB4 = steganograph.revertLSB4();
                    FileOutputStream outputStreamLSB4 = new FileOutputStream("desencodedLSB4.bmp");
                    outputStreamLSB4.write(desencodedLSB4);
                    outputStreamLSB4.close();
                    argsParser.encoder.decryptFile("desencodedLSB4.bmp","aesDesLSB4.bmp");
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
