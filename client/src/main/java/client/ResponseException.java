package client;

/**
 * Indicates there was an error connecting to the database
 */
public class ResponseException extends Exception{
    public ResponseException() {}
    public ResponseException(String message) {
        super(message);
    }
    public ResponseException(String message, Throwable ex) {
        super(message, ex);
    }
}
