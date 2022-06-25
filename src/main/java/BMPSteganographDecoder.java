import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BMPSteganographDecoder {

    private final byte[] data;
    private final boolean isEncrypted;
    private final boolean isBMP;

    public BMPSteganographDecoder(byte[] data, boolean isEncrypted, boolean isBMP) {
        this.data = data;
        this.isEncrypted = isEncrypted;
        this.isBMP = isBMP;
    }

    public FileData LSBI() throws IOException {
        ByteIterator iter = isBMP ? new BMPEditor(data).byteIterator() : new ByteIterator(data);

        boolean[] pattern = new boolean[4];
        for(int i = 0; i < 4; i ++) {
            pattern[i] = iter.NextLSB() == 1;
        }

        LSBIterator lsbIiter = isBMP ? new BMPEditor(data).lsbIterator(pattern, 4) : new LSBIterator(data, pattern, 4);

        // Extract file size
        int size = 0;
        for(int i = 0; i < 32; i ++) {
            int bit  = lsbIiter.NextLSB();
            size = (size << 1) + bit;
        }

        // Extract file data
        byte[] file = new byte[size];
        for (int i = 0; i < size; i++) {
            int b = 0;
            for (int j = 0; j < 8; j++) b = (b << 1) + lsbIiter.NextLSB();
            file[i] = (byte) b;
        }

        FileData fd = new FileData(file, size);

        if(!isEncrypted) { // Get file extension if not encrypted
            byte[] extension = new byte[10];
            int b;
            int i = 0;
            do {
                b = 0;
                for (int j = 0; j < 8; j++) b = (b << 1) + lsbIiter.NextLSB();
                extension[i++] = (byte) b;
            } while (b != 0);
            fd.setExt(new String(Arrays.copyOfRange(extension,0, i-1), StandardCharsets.UTF_8));
        }

        return fd;
    }

    public FileData LSB1() throws IOException {
        ByteIterator iter = isBMP ? new BMPEditor(data).byteIterator() : new ByteIterator(data);

        int size = 0;
        for(int i = 0; i < 32; i ++) {
            int bit = iter.NextLSB();
            size = (size << 1) + bit;
        }

        byte[] file = new byte[size];
        for (int i = 0; i < size; i++) {
            int b = 0;
            for (int j = 0; j < 8; j++) {
                int bit = iter.NextLSB();
                b = (b << 1) + bit;
            }
            file[i] = (byte) b;
        }

        FileData fd = new FileData(file, size);

        if(!isEncrypted) {
            byte[] extension = new byte[10];
            int b; int i = 0;
            do {
                b = 0;
                for (int j = 0; j < 8; j++) {
                    int bit = iter.NextLSB();
                    b = (b << 1) + bit;
                }
                extension[i++] = (byte) b;
            } while (b != 0);
            fd.setExt(new String(Arrays.copyOfRange(extension,0, i-1), StandardCharsets.UTF_8));
        }

        return fd;
    }

    public FileData LSB4() throws IOException {
        ByteIterator iter = isBMP ? new BMPEditor(data).byteIterator() : new ByteIterator(data);

        int size = 0;
        for(int i = 0; i < 8; i ++) {
            int b = iter.NextNLSB(4) & 0xF;
            size = (size << 4) + b;
        }

        byte[] file = new byte[size];
        for (int i = 0; i < size; i++) {
            int b = iter.NextNLSB(4) & 0xF;
            b = (b << 4) + (iter.NextNLSB(4) & 0xF);
            file[i] = (byte) b;
        }

        FileData fd = new FileData(file, size);

        if(!isEncrypted) {
            byte[] extension = new byte[10];
            int b; int i = 0;
            do {
                b = iter.NextNLSB(4) & 0xF;
                b = (b << 4) + (iter.NextNLSB(4) & 0xF);
                extension[i++] = (byte) b;
            } while (b != 0 & i < 10);
            fd.setExt(new String(Arrays.copyOfRange(extension,0, i-1), StandardCharsets.UTF_8));
        }

        return fd;
    }
}
