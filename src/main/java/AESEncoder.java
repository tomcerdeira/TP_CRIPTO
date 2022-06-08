import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class AESEncoder {

    private  File inputFile;
    private  File outputFile;
    private final String algorithm;
    private final SecretKey key;
    private final IvParameterSpec iv;


    public AESEncoder(String algorithm,String inputFilePath,String outPutFilePath,SecretKey key, IvParameterSpec iv) {
        this.inputFile = new File(inputFilePath);
        this.outputFile = new File(outPutFilePath);
        this.algorithm = algorithm;
        this.key = key;
        this.iv = iv;
    }


    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    private void encryptOrDecrypt(Cipher cipher) throws IOException, IllegalBlockSizeException, BadPaddingException {
        FileInputStream inputStream = new FileInputStream(this.inputFile);
        FileOutputStream outputStream = new FileOutputStream(this.outputFile);
        byte[] buffer = new byte[64];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byte[] output = cipher.update(buffer, 0, bytesRead);
            if (output != null) {
                outputStream.write(output);
            }
        }
        byte[] outputBytes = cipher.doFinal();
        if (outputBytes != null) {
            outputStream.write(outputBytes);
        }
        inputStream.close();
        outputStream.close();
    }

    public  void encryptFile() throws IOException, NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        encryptOrDecrypt(cipher);

    }


    public void decryptFile(String inputFilePath, String outPutFilePath) throws IOException, NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        this.inputFile = new File(inputFilePath);
        this.outputFile = new File(outPutFilePath);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        encryptOrDecrypt(cipher);
    }
}
