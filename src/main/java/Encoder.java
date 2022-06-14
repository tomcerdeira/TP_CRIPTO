import java.io.File;

public interface Encoder {
    void encryptFile( String inputFilePath, String outPutFilePath);
    void decryptFile(String inputFilePath, String outPutFilePath);
    File getEncryptedFile();
}
