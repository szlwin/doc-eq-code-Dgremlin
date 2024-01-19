package dec.expand.declare.business.exception;

public class ExecuteException extends RuntimeException {

    public ExecuteException(String message) {
        super(message);
    }

    public ExecuteException(Exception e) {
        super(e);
    }

    public ExecuteException(String message, Exception e) {
        super(message,e);
    }
}
