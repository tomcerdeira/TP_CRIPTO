package exceptions;

import java.io.IOException;

public class BitsPerPixelException extends IOException {
    public BitsPerPixelException() {
        super("Invalid BMP Bits per Pixel. BPP should be: 24 bits.");
    }
}
