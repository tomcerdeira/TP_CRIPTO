package exceptions;

import java.io.IOException;

public class CompressionMethodException extends IOException {
    public CompressionMethodException() {
        super("Invalid BMP Compression method.");
    }
}
