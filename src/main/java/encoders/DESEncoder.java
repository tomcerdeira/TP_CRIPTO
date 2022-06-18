package encoders;

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


public class DESEncoder implements Encoder {

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
    public void encrypt(String inputFilePath, String outPutFilePath){
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
    public byte[] decrypt(byte[] data){
        try {

            byte[] fileBytes = Base64.getDecoder().decode(data);

            return dcipher.doFinal(fileBytes);

        }catch( BadPaddingException|IllegalBlockSizeException e){
            e.printStackTrace();
            throw  new RuntimeException();
        }
    }

    @Override
    public File getEncryptedFile(){
        return this.encryptedFile;
    }

}
