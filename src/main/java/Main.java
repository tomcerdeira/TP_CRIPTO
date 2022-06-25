import exceptions.FileTooLargetException;

import javax.crypto.NoSuchPaddingException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeyException, FileTooLargetException {

        ArgumentsParser argsParser = new ArgumentsParser(args);

        if (argsParser.revertMode) {
            FileInputStream f3 = new FileInputStream(argsParser.fileCarrier);

            BMPSteganographDecoder decoder = new BMPSteganographDecoder(f3.readAllBytes(), argsParser.encodeMode, true);

            FileData decodedFile;

            switch (argsParser.stegMode) {
                case "LSB1" -> decodedFile = decoder.LSB1();
                case "LSB4" -> decodedFile = decoder.LSB4();
                case "LSBI" -> decodedFile = decoder.LSBI();
                default -> throw new IllegalStateException("Unexpected value: " + argsParser.stegMode);
            }

            if (argsParser.encodeMode) {

                byte [] decodedBytes = argsParser.encoder.decrypt(decodedFile.data);

                int len = new BigInteger(Arrays.copyOfRange(decodedBytes, 0, 4)).intValue();
                byte[] data = Arrays.copyOfRange(decodedBytes, 4, 4 + len);
                String ext = new String(Arrays.copyOfRange(decodedBytes, 4 + len, decodedBytes.length-1), StandardCharsets.UTF_8);

                generateFileFromByteArray(data, argsParser.outputFile.getAbsolutePath() + ext);
            } else {
                generateFileFromByteArray(decodedFile.data, argsParser.outputFile.getAbsolutePath() + decodedFile.ext);
            }
        } else{
            byte[] arr = getByteArrayDataToHide(argsParser) ;
            String extension = "";
            int extensionIndex = argsParser.fileToEncrypt.toString().lastIndexOf('.');
            extension = "." + argsParser.fileToEncrypt.toString().substring(extensionIndex+1) + "\0";
            byte[] bmpBytes = new FileInputStream(argsParser.fileCarrier.getAbsolutePath()).readAllBytes();
            BMPSteganographEncoder steganograph = new BMPSteganographEncoder(bmpBytes, arr, extension, argsParser);
            switch (argsParser.stegMode) {
                case "LSB1" -> steganograph.LSB1();
                case "LSB4" -> steganograph.LSB4();
                case "LSBI" -> steganograph.LSBImproved();
            }

            generateFileFromByteArray(steganograph.getEditor().getCoverImageBytes(),argsParser.outputFile.getAbsolutePath());
        }

    }
    private static byte[] getByteArrayDataToHide(ArgumentsParser argumentsParser) throws IOException {
        return new FileInputStream(argumentsParser.fileToEncrypt.getAbsolutePath()).readAllBytes();
    }

    private static void generateFileFromByteArray(byte[] array, String fileName) throws IOException {
        FileOutputStream outputStreamLSB1 = new FileOutputStream(fileName);
        outputStreamLSB1.write(array);
        outputStreamLSB1.close();
    }
}

    // -embed -in C:/Users/Tomas/Documents/ITBA/Cripto/TP/TP_CRIPTO/src/main/resources/kings.bmp -p kings.bmp -out out -steg LSB1 -pass hola