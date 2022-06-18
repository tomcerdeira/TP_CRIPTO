package encoders;

import java.io.File;

public interface Encoder {
    void encrypt(String inputFilePath, String outPutFilePath);
    byte[] decrypt(byte[] data);
    File getEncryptedFile();
}
