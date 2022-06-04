package exceptions;

public class FileTooLargetException extends Exception {
    public FileTooLargetException(String message) {
        super("File too large. Max file size: " + message + " Bytes.");
    }
}
