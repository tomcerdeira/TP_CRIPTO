import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class BMPSteganographDecoder {

    private final byte[] data;
    private final boolean isEncrypted;

    public BMPSteganographDecoder(byte[] data, boolean isEncrypted) {
        this.data = data;
        this.isEncrypted = isEncrypted;
    }

    public FileData LSBI() throws IOException {
        BMPEditor editor = new BMPEditor(data);
        ByteIterator iter = editor.byteIterator();

        boolean[] pattern = new boolean[4];
        for(int i = 0; i < 4; i ++) {
            pattern[i] = iter.NextLSB() == 1;
        }

        LSBIterator lsbIiter = new LSBIterator(editor, pattern, 4);

        // Extract file size
        int size = 0;
        for(int i = 0; i < 32; i ++) size = (size << 1) + lsbIiter.NextLSB();

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
            fd.setExt(new String(extension, StandardCharsets.UTF_8));
        }

        return fd;
    }

    public FileData LSB1() throws IOException {
        ByteIterator iter = new BMPEditor(data).byteIterator();

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
            fd.setExt(new String(extension, StandardCharsets.UTF_8));
        }

        return fd;
    }

    public FileData LSB4() throws IOException {
        BMPEditor bmpEditor = new BMPEditor(data);
        ByteIterator iter = bmpEditor.byteIterator();

        int size = 0;
        for(int i = 0; i < 8; i ++) {
            int b = iter.NextNLSB(4) & 0xF;
            size = (size << 4) + b;
        }

        byte[] file = new byte[size];
        for (int i = 0; i < size; i++) {
            int b = iter.NextNLSB(4) & 0xF;
            b = (b << 4) + iter.NextNLSB(4) & 0xF;
            file[i] = (byte) b;
        }

        FileData fd = new FileData(file, size);

        if(!isEncrypted) {
            byte[] extension = new byte[10];
            int b; int i = 0;
            do {
                b = iter.NextNLSB(4) & 0xF;
                b = (b << 4) + iter.NextNLSB(4) & 0xF;
                extension[i++] = (byte) b;
            } while (b != 0);
            fd.setExt(new String(extension, StandardCharsets.UTF_8));
        }

        return fd;
    }
}
