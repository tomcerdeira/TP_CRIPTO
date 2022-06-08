import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;


public class EncryptDecryptStringWithDES {

    private static Cipher ecipher;
    private static Cipher dcipher;

    private static SecretKey key;

    public static void main(String[] args) {

        try {

            // generate secret key using DES algorithm
            //key = KeyGenerator.getInstance("DES").generateKey();

            String password = "password";
            SecretKeyFactory factory = SecretKeyFactory.getInstance("DES");
            DESKeySpec dks = new DESKeySpec(password.getBytes());
            SecretKey desKey = factory.generateSecret(dks);

            ecipher = Cipher.getInstance("DES");
            dcipher = Cipher.getInstance("DES");

            // initialize the ciphers with the given key

            ecipher.init(Cipher.ENCRYPT_MODE, desKey);

            dcipher.init(Cipher.DECRYPT_MODE, desKey);

            FileInputStream inputFileStream = new FileInputStream(new File("src/main/resources/testImage.bmp"));
            FileOutputStream outputStream = new FileOutputStream(new File("src/main/resources/testImageEncryDES.bmp"));
            FileOutputStream outputStreamDec = new FileOutputStream(new File("src/main/resources/testImageDescryDES.bmp"));

            encrypt(inputFileStream, outputStream);

            FileInputStream inputOfOutputStream = new FileInputStream(new File("src/main/resources/testImageEncryDES.bmp"));

            decrypt(inputOfOutputStream, outputStreamDec);

            //String decrypted = decrypt(encrypted);

            //System.out.println("Decrypted: " + decrypted);

        }
        catch (NoSuchAlgorithmException e) {
            System.out.println("No Such Algorithm:" + e.getMessage());
            return;
        }
        catch (NoSuchPaddingException e) {
            System.out.println("No Such Padding:" + e.getMessage());
            return;
        }
        catch (InvalidKeyException e) {
            System.out.println("Invalid Key:" + e.getMessage());
            return;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

    }

    public static void encrypt(FileInputStream fileInputStream, FileOutputStream fileOutputStream) {

        try {

            // encode the string into a sequence of bytes using the named charset

            // storing the result into a new byte array.

            byte[] utf8 = fileInputStream.readAllBytes();

            byte[] enc = ecipher.doFinal(utf8);

            // encode to base64
            enc = Base64.getEncoder().encode(enc);

            if (enc != null) {
                fileOutputStream.write(enc);
            }

        }

        catch (Exception e) {

            e.printStackTrace();

        }

    }

    public static void decrypt(FileInputStream fileInputStream, FileOutputStream fileOutputStream) {

        try {

            // decode with base64 to get bytes

            byte[] dec = Base64.getDecoder().decode(fileInputStream.readAllBytes());

            byte[] utf8 = dcipher.doFinal(dec);

// create new string based on the specified charset

            fileOutputStream.write(utf8);

        }

        catch (Exception e) {

            e.printStackTrace();

        }

    }

}