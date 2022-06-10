import java.io.File;

public interface Encoder {
    void encryptFile();
    void decryptFile(String inputFilePath, String outPutFilePath);
    File getEncryptedFile();
}
