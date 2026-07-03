package dec.context.parse.yaml.exception;

public class YAMLParseException extends Exception {

    public YAMLParseException() {
    }

    public YAMLParseException(String message) {
        super(message);
    }

    public YAMLParseException(Throwable cause) {
        super(cause);
    }

    public YAMLParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
