import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;


public class AESEncoder implements Encoder{
    private final String algorithm;
    private final SecretKey key;
    private final IvParameterSpec iv;
    private File encryptedFile ;


    public AESEncoder(String algorithm, SecretKey key, IvParameterSpec iv) {
        this.algorithm = algorithm;
        this.key = key;
        this.iv = iv;
    }

    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    private void encryptOrDecrypt(Cipher cipher, String inputFilePath, String outPutFilePath) throws IOException, IllegalBlockSizeException, BadPaddingException {
        FileInputStream inputStream = new FileInputStream(inputFilePath);
        FileOutputStream outputStream = new FileOutputStream(outPutFilePath);
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

    @Override
    public void encryptFile( String inputFilePath, String outPutFilePath) {
        try {
            this.encryptedFile = new File(outPutFilePath);
            Cipher cipher = Cipher.getInstance(algorithm); //TODO: Agregar que cuando el modo no toma IV sacarlo

            if(algorithm.contains("ECB")){
                cipher.init(Cipher.ENCRYPT_MODE, key);
            }else{
                cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            }

            FileInputStream inputStream = new FileInputStream(inputFilePath);
            FileOutputStream outputStream = new FileOutputStream(outPutFilePath);

            byte[] fileBytes = inputStream.readAllBytes();
            byte[] transformedBytes = cipher.doFinal(fileBytes);

            if (transformedBytes != null) {
                outputStream.write(transformedBytes);
            }

        } catch (IOException | NoSuchPaddingException |
                NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException |
                BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

    }

    @Override
    public void decryptFile(String inputFilePath, String outPutFilePath) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);

            if(algorithm.contains("ECB")){
                cipher.init(Cipher.DECRYPT_MODE, key);
            }else{
                cipher.init(Cipher.DECRYPT_MODE, key, iv);
            }

            FileInputStream inputStream = new FileInputStream(inputFilePath);
            FileOutputStream outputStream = new FileOutputStream(outPutFilePath);

            byte[] fileBytes = inputStream.readAllBytes();
            byte[] transformedBytes = cipher.doFinal(fileBytes);

            if (transformedBytes != null) {
                outputStream.write(transformedBytes);
            }
        } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Override
    public File getEncryptedFile(){
        return encryptedFile;
    }
}
