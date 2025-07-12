package common.models;

public class CorruptedFileException extends RuntimeException {
    public CorruptedFileException(String message) {
        super(message);
    }
}
