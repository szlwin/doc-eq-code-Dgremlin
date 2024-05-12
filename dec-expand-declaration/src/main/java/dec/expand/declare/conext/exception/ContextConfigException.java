package dec.expand.declare.conext.exception;

public class ContextConfigException extends RuntimeException{

    public ContextConfigException(String message) {
        super(message);
    }

    public ContextConfigException(Exception e) {
        super(e);
    }

    public ContextConfigException(String message, Exception e) {
        super(message,e);
    }
}
