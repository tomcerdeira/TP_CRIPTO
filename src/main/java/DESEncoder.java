import javax.crypto.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


public class DESEncoder implements Encoder{

    private  File inputFile;
    private  File outputFile;
    private final SecretKey key;
    private static Cipher ecipher;
    private static Cipher dcipher;


    public DESEncoder(String inputFilePath, String outPutFilePath, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        this.inputFile = new File(inputFilePath);
        this.outputFile = new File(outPutFilePath);
        this.key = key;

        ecipher = Cipher.getInstance("DES");
        dcipher = Cipher.getInstance("DES");
        ecipher.init(Cipher.ENCRYPT_MODE, key);
        dcipher.init(Cipher.DECRYPT_MODE, key);

    }

    @Override
    public void encryptFile(){
        try {
            FileInputStream inputStream = new FileInputStream(this.inputFile);
            FileOutputStream outputStream = new FileOutputStream(this.outputFile);

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
            this.inputFile = new File(inputFilePath);
            this.outputFile = new File(outPutFilePath);

            FileInputStream inputStream = new FileInputStream(this.inputFile);
            FileOutputStream outputStream = new FileOutputStream(this.outputFile);

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
        return this.outputFile;
    }

}
