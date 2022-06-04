package exceptions;

import java.io.IOException;

public class NotVersion3BMPException extends IOException {
    public NotVersion3BMPException() {
        super("File is not a BMP Version 3 file");
    }
}
