package dec.bean.exception;

public class ConvertException extends RuntimeException {

    public ConvertException(String message) {
        super(message);
    }

    public ConvertException(Exception e) {
        super(e);
    }

    public ConvertException(String message, Exception e) {
        super(message,e);
    }
}
