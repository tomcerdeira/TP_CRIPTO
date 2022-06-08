import exceptions.FileTooLargetException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

public class Main {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
//        BufferedImage bufferedImage = ImageIO.read(Objects.requireNonNull(Main.class.getResource("testImage.bmp")));
//        byte[] arr = new String("HOla").getBytes(StandardCharsets.UTF_8);
//        BMPSteganographEncoder steganograph = new BMPSteganographEncoder(bufferedImage, arr);
//        try {
//            steganograph.LSB1();
//            steganograph.getEditor().outputToFile("escondeHola.bmp");
//        } catch (FileTooLargetException e) {
//            e.printStackTrace();
//        }


        AESEncoder encoder = new AESEncoder();
        SecretKey key = AESEncoder.getKeyFromPassword("password", "salt");
        String algo = "AES/CBC/PKCS5Padding";
        IvParameterSpec ivParameterSpec = AESEncoder.generateIv();
        File file = new File("/home/santiago/Desktop/TP_CRIPTO/src/main/resources/testImage.bmp");
        File outputFile = new File("/home/santiago/Desktop/TP_CRIPTO/src/main/resources/testImageEncry.bmp");
        AESEncoder.encryptFile(algo,key,ivParameterSpec,file,outputFile);
        AESEncoder.decryptFile(algo,key,ivParameterSpec,outputFile,file);


    }

}
