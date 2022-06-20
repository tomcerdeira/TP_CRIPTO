import java.util.NoSuchElementException;

public class ByteIterator {

    private final byte[] data;
    int i = 0;

    public ByteIterator(byte[] data) {
        this.data = data;
    }

    public int NextLSB() {
        if(i > data.length)
            throw new NoSuchElementException();

        int bit = data[i] & 1;
        i++;

        return bit;
    }

    public int NextNLSB(int n) {
        if(i > data.length)
            throw new NoSuchElementException();

        int bits = data[i] & firstNBit(n);
        i++;

        return bits;
    }

    private static int firstNBit(int n){
        if(n <= 0)
            return 0;

        return firstNBit(n - 1) << 1 | 1;
    }
}
