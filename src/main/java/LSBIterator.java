import java.util.NoSuchElementException;

public class LSBIterator {

    private final boolean[] pattern;
    private final byte[] data;
    int i;

    public LSBIterator(byte[] data, boolean[] pattern, int start) {
        this.data = data;
        this.pattern = pattern;
        this.i = start;
    }

    public int NextLSB() {
        if(i > data.length)
            throw new NoSuchElementException();

        byte b = data[i];

        int type = (b >> 1) & 0b11;
        int bit = (b & 1);

        if(pattern[type])
            bit = 1 - bit;
        i++;

        return bit;
    }

}
