import java.util.NoSuchElementException;

public class ByteIterator {

    private final BMPEditor editor;
    int i = 0;

    public ByteIterator(BMPEditor editor) {
        this.editor = editor;
    }

    public void NextSetLeastSignificantBits(byte b, int n) {
        if(i > editor.getBitArraySize())
            throw new NoSuchElementException();

        byte currentByte = editor.getBitArrayByte(i);

        currentByte = (byte)(currentByte & ~firstNBit(n));
        byte bb = (byte)(b & firstNBit(n));

        editor.setBitArrayByte(i, (byte)(currentByte | bb));
        i++;
    }

    private static int firstNBit(int n){
        if(n <= 0)
            return 0;

        return firstNBit(n - 1) << 1 | 1;
    }
}
