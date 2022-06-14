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


public class DESEncoder implements Encoder{

    private final SecretKey key;
    private static Cipher ecipher;
    private static Cipher dcipher;
    private IvParameterSpec iv;
    private File encryptedFile;


    public DESEncoder(String algorithm ,SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException {

        this.key = key;

        ecipher = Cipher.getInstance(algorithm);
        dcipher = Cipher.getInstance(algorithm);


        if (algorithm.contains("ECB")){
            ecipher.init(Cipher.ENCRYPT_MODE, key);
            dcipher.init(Cipher.DECRYPT_MODE, key);
        }else{
            this.iv = DESEncoder.generateIv();
            ecipher.init(Cipher.ENCRYPT_MODE, key, iv);
            dcipher.init(Cipher.DECRYPT_MODE, key, iv);
        }

    }

    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[8];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    @Override
    public void encryptFile(String inputFilePath, String outPutFilePath){
        try {
            FileInputStream inputStream = new FileInputStream(inputFilePath);
            FileOutputStream outputStream = new FileOutputStream(outPutFilePath);
            this.encryptedFile = new File(outPutFilePath);

            byte[] fileBytes = inputStream.readAllBytes();
            byte[] transformedBytes = ecipher.doFinal(fileBytes);
            transformedBytes = Base64.getEncoder().encode(transformedBytes);

            if (transformedBytes != null) {
                outputStream.write(transformedBytes);
            }

            inputStream.close();
            outputStream.close();
        }catch(IOException | BadPaddingException | IllegalBlockSizeException e){
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Override
    public void decryptFile(String inputFilePath, String outPutFilePath){
        try {
            File inputFile = new File(inputFilePath);
            File outputFile = new File(outPutFilePath);

            FileInputStream inputStream = new FileInputStream(inputFile);
            FileOutputStream outputStream = new FileOutputStream(outputFile);

            byte[] fileBytes = Base64.getDecoder().decode(inputStream.readAllBytes());
            byte[] transformedBytes = dcipher.doFinal(fileBytes);

            if (transformedBytes != null) {
                outputStream.write(transformedBytes);
            }

        }catch( IOException| BadPaddingException|IllegalBlockSizeException e){
            e.printStackTrace();
            throw  new RuntimeException();
        }
    }

    @Override
    public File getEncryptedFile(){
        return this.encryptedFile;
    }

}
