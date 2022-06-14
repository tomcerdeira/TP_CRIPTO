import exceptions.FileTooLargetException;

import javax.crypto.NoSuchPaddingException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Main {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeyException, FileTooLargetException {

        ArgumentsParser argsParser = new ArgumentsParser(args);

        byte[] arr = getByteArrayDataToHide(argsParser) ;
        String extension = "";

        if (argsParser.revertMode) {
            byte desencoded[] = new byte[0];
            FileInputStream f3 = new FileInputStream(argsParser.fileCarrier);
            switch (argsParser.stegMode) {
                case "LSB1":
                    desencoded = BMPSteganographEncoder.revertLSB1(f3.readAllBytes());
                    break;
                case "LSB4":
                    desencoded = BMPSteganographEncoder.revertLSB4(f3.readAllBytes());
                    break;
                case "LSBI":
//                        desencoded = BMPSteganographEncoder.revertLSBI(f3.readAllBytes());///TODO
                    break;
            }
            String auxFileToDescrypt = "auxiliarFileToDesencrypt";
            String extensionOfFile = getExtension(desencoded);
            if (argsParser.encodeMode) {
                generateFileFromByteArray(desencoded, auxFileToDescrypt);
                argsParser.encoder.decryptFile(auxFileToDescrypt, argsParser.outputFile.getAbsolutePath() + extensionOfFile);
            } else {
                generateFileFromByteArray(desencoded, argsParser.outputFile.getAbsolutePath() + extensionOfFile);
            }
        } else{
            int extensionIndex = argsParser.fileToEncrypt.toString().lastIndexOf('.');
            extension = "." + argsParser.fileToEncrypt.toString().substring(extensionIndex+1) + "\0";
            BufferedImage bufferedImage = ImageIO.read(new File(argsParser.fileCarrier.getAbsolutePath()));
            BMPSteganographEncoder steganograph = new BMPSteganographEncoder(bufferedImage, arr, extension);
            switch (argsParser.stegMode) {
                case "LSB1":
                    steganograph.LSB1();
                    break;
                case "LSB4":
                    steganograph.LSB4();
                    break;
                case "LSBI":
                    steganograph.LSBImproved();
                    break;
            }
            generateFileFromByteArray(steganograph.getEditor().getCoverImageBytes(),argsParser.outputFile.getAbsolutePath());
        }

    }
    private static byte[] getByteArrayDataToHide(ArgumentsParser argumentsParser) throws IOException {
        if (argumentsParser.encodeMode && !argumentsParser.revertMode) {
            argumentsParser.encoder.encryptFile(argumentsParser.fileToEncrypt.getAbsolutePath(), "encryptedFile");
            FileInputStream f2 = new FileInputStream("encryptedFile");
            return f2.readAllBytes();
        }

        if (!argumentsParser.revertMode) {
            FileInputStream f1 = new FileInputStream(argumentsParser.fileToEncrypt);
            return f1.readAllBytes();
        }
        return null;
    }

    private static String getExtension(byte[] desencoded){
        byte[] forExtension = desencoded;
        StringBuilder fileExtension = new StringBuilder();
        int i = forExtension.length-1;
        for (; forExtension[i]!='.'; i--){
            fileExtension.append((char)forExtension[i]);
        }
        return fileExtension.append('.').reverse().toString();
    }

    private static void generateFileFromByteArray(byte[] array, String fileName) throws IOException {
        System.out.println("GENERATE"+ fileName);
        FileOutputStream outputStreamLSB1 = new FileOutputStream(fileName);
        outputStreamLSB1.write(array);
        outputStreamLSB1.close();
    }
}

// -embed -in C:/Users/Tomas/Documents/ITBA/Cripto/TP/TP_CRIPTO/src/main/resources/kings.bmp -p kings.bmp -out out -steg LSB1 -pass hola