import exceptions.FileTooLargetException;

import javax.crypto.NoSuchPaddingException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeyException {

        ArgumentsParser argsParser = new ArgumentsParser(args);

        byte[] arr = new byte[1];
        String extension = "";

        if(!argsParser.revertMode) {
            FileInputStream f1 = new FileInputStream(argsParser.fileToEncrypt);
            arr = f1.readAllBytes();
            int extensionIndex = argsParser.fileToEncrypt.toString().lastIndexOf('.');
            extension = "." + argsParser.fileToEncrypt.toString().substring(extensionIndex+1) + "\0";
        }

        if(argsParser.encodeMode) {
            argsParser.encoder.encryptFile();
            FileInputStream f2 = new FileInputStream(argsParser.encoder.getEncryptedFile());
            arr = f2.readAllBytes();
        }

        BufferedImage bufferedImage = ImageIO.read(new File(argsParser.fileCarrier.getAbsolutePath()));
        BMPSteganographEncoder steganograph = new BMPSteganographEncoder(bufferedImage, arr, extension);

        try {
            switch (argsParser.stegMode){
                case "LSB1":
                    if(argsParser.revertMode){

                        System.out.println("-extract");
                        FileInputStream f3 = new FileInputStream(argsParser.fileCarrier);
                        byte[] desencoded = BMPSteganographEncoder.revertLSB1(f3.readAllBytes());
                        if(argsParser.encodeMode) {
                            argsParser.encoder.decryptFile(argsParser.outputFile.getPath(), "SALIDA_LSB1_REVERT_MODE.bmp");
                        }

                        byte[] forExtension = desencoded;
                        StringBuilder fileExtension = new StringBuilder();
                        int i = forExtension.length-1;
                        for (; forExtension[i]!='.'; i--){
                            fileExtension.append((char)forExtension[i]);
                        }

                        String extensionOfFile = fileExtension.append('.').reverse().toString();
                        System.out.println(argsParser.outputFile + extensionOfFile);
                        FileOutputStream outputStreamLSB1 = new FileOutputStream(argsParser.outputFile + extensionOfFile);
                        outputStreamLSB1.write(desencoded);
                        outputStreamLSB1.close();

                    }else {
                        System.out.println("-embed");

                        steganograph.LSB1();
                        FileOutputStream outputStream = new FileOutputStream(argsParser.outputFile);
                        byte[] aux = steganograph.getEditor().getCoverImageBytes();
                        outputStream.write(aux);
                        outputStream.close();

                    }
                    break;
                case "LSB4":
                    if(argsParser.revertMode) {
                        System.out.println("-extract");
                        FileInputStream f3 = new FileInputStream(argsParser.fileCarrier);
                        byte[] desencodedLSB4 = BMPSteganographEncoder.revertLSB4(f3.readAllBytes());

                        if(argsParser.encodeMode) {
                            argsParser.encoder.decryptFile("desencodedLSB4.bmp", "aesDesLSB4.bmp");
                        }

                        byte[] forExtension = new FileInputStream(argsParser.fileCarrier).readAllBytes();
                        StringBuilder fileExtension = new StringBuilder();
                        for (int i=forExtension.length-2; forExtension[i]!='.'; i--){
                            fileExtension.append((char)forExtension[i]);
                        }
                        String extensionOfFile = fileExtension.append('.').reverse().toString();

                        System.out.println(argsParser.outputFile + extensionOfFile);
                        FileOutputStream outputStreamLSB4 = new FileOutputStream(argsParser.outputFile + extensionOfFile);
                        outputStreamLSB4.write(desencodedLSB4);
                        outputStreamLSB4.close();

                    }else{
                        System.out.println("-embed");
                        steganograph.LSB4();
                        FileOutputStream outputStream = new FileOutputStream(argsParser.outputFile);
                        byte[] aux = steganograph.getEditor().getCoverImageBytes();
                        outputStream.write(aux);
                        outputStream.close();
                    }
                    break;
                case "LSBI":
                    steganograph.LSBImproved();
                    break;
                default:
                    System.out.println("SIN STEGMODE");
            }
//            steganograph.getEditor().outputToFile(argsParser.outputFile.getPath());
        } catch (FileTooLargetException e) {
            e.printStackTrace();
        }

        ////////////////////////// Para DES

//        String passwordForDes = "password";
//        SecretKey keyForDes = SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(passwordForDes.getBytes()));
//
//        DESEncoder desEncoder = new DESEncoder("DES/CFB/PKCS5Padding" ,"src/main/resources/medianoche1.bmp", "src/main/resources/SALIDA.bmp", keyForDes);
//
//        // Test Encrypt
//        desEncoder.encryptFile();
//
//        // Test Decrypt
//        desEncoder.decryptFile("src/main/resources/SALIDA.bmp", "src/main/resources/medianoche2.bmp");

    }

}

// -embed -in C:/Users/Tomas/Documents/ITBA/Cripto/TP/TP_CRIPTO/src/main/resources/kings.bmp -p kings.bmp -out out -steg LSB1 -pass hola