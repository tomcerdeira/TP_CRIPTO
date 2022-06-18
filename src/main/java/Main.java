import exceptions.FileTooLargetException;

import javax.crypto.NoSuchPaddingException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Main {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeyException, FileTooLargetException {

        ArgumentsParser argsParser = new ArgumentsParser(args);

        byte[] arr = getByteArrayDataToHide(argsParser) ;
        String extension = "";

//        argsParser.encoder.encryptFile(argsParser.fileToEncrypt.getAbsolutePath(), "encryptedFile");
//
//        argsParser.encoder.decryptFile("encryptedFile", "SALIDA_DESENECTIPTADA.bmp");// ENC(TR || DA || EXT) --> TR || DA || EXT
//
//
//        System.exit(1);

        if (argsParser.revertMode) {
            byte desencoded[] = new byte[0];
            FileInputStream f3 = new FileInputStream(argsParser.fileCarrier);
            switch (argsParser.stegMode) {
                case "LSB1":
                    desencoded = BMPSteganographEncoder.revertLSB1(f3.readAllBytes());
                    break;
                case "LSB4":
                    desencoded = BMPSteganographEncoder.revertLSB4(f3.readAllBytes()); // ENC(TR || DA || EXT) + 4bytes
                    break;
                case "LSBI":
                        desencoded = BMPSteganographEncoder.revertLSBI(f3.readAllBytes());
                    break;
            }
            byte [] desencodedWithOutExtension = new byte[desencoded.length-4]; // ENC(TR || DA || EXT)
            System.arraycopy(desencoded, 0, desencodedWithOutExtension, 0, desencoded.length-4);
            String auxFileToDescrypt = "ESTONOEXISTE";
            if (argsParser.encodeMode) {

                /// ESTA BIEN OSEA desencodedWithOutExtension viene bien
                generateFileFromByteArray(desencodedWithOutExtension, auxFileToDescrypt);
                argsParser.encoder.decryptFile(auxFileToDescrypt, "TOMAS");// ENC(TR || DA || EXT) --> TR || DA || EXT
                byte[] realLen = new byte[4];

                FileInputStream outputEncode = new FileInputStream("TOMAS");
                byte [] desencodedBytes = outputEncode.readAllBytes();
                byte [] from4tofinal = new byte[desencodedBytes.length-8];

                System.arraycopy(desencodedBytes, 4, from4tofinal, 0, desencodedBytes.length-8);

                int len1 = new BigInteger(from4tofinal).intValue();
                System.out.println(len1);

                System.arraycopy(desencodedBytes, 0, realLen, 0, 4);

                int len = new BigInteger(realLen).intValue();
                System.out.println(len);

                System.out.println(getExtension(desencodedBytes));

                FileOutputStream outputStream1 = new FileOutputStream("fileOUTPU9delanoche"+getExtension(desencodedBytes));
                outputStream1.write(from4tofinal);

            } else {
                String extensionOfFile = getExtension(desencoded);
                generateFileFromByteArray(desencoded, argsParser.outputFile.getAbsolutePath() + extensionOfFile);
            }
        } else{
            int extensionIndex = argsParser.fileToEncrypt.toString().lastIndexOf('.');
            extension = "." + argsParser.fileToEncrypt.toString().substring(extensionIndex+1) + "\0";
            BufferedImage bufferedImage = ImageIO.read(new File(argsParser.fileCarrier.getAbsolutePath()));
            BMPSteganographEncoder steganograph = new BMPSteganographEncoder(bufferedImage, arr, extension,argsParser);
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
//            argumentsParser.encoder.encryptFile(argumentsParser.fileToEncrypt.getAbsolutePath(), "encryptedFile");
            FileInputStream f2 = new FileInputStream(argumentsParser.fileToEncrypt.getAbsolutePath());
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
        int i = forExtension.length-2; /// TODO CHANGE -2
        for (; forExtension[i]!='.'; i--){
            fileExtension.append((char)forExtension[i]);
        }
        return fileExtension.append('.').reverse().toString();
    }

    private static void generateFileFromByteArray(byte[] array, String fileName) throws IOException {
        FileOutputStream outputStreamLSB1 = new FileOutputStream(fileName);
        outputStreamLSB1.write(array);
        outputStreamLSB1.close();
    }
}

// -embed -in C:/Users/Tomas/Documents/ITBA/Cripto/TP/TP_CRIPTO/src/main/resources/kings.bmp -p kings.bmp -out out -steg LSB1 -pass hola