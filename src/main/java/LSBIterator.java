import java.util.NoSuchElementException;

public class LSBIterator {

    private boolean[] pattern;
    private BMPEditor editor;
    int i;

    public LSBIterator(BMPEditor editor, boolean[] pattern, int start) {
        this.editor = editor;
        this.pattern = pattern;
        this.i = start;
    }

    public LSBIterator(BMPEditor editor, boolean[] pattern){
        this(editor, pattern, 0);
    }

    public int NextLSB() {
        if(i > editor.getBitArraySize())
            throw new NoSuchElementException();

        byte b = editor.getBitArrayByte(i);

        int type = (b >> 1) & 0b11;
        int bit = (b & 1);

        if(pattern[type])
            bit = 1 - bit;
        i++;

        return bit;
    }

}
