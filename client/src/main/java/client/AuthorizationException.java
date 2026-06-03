package client;

/**
 * Indicates there was an error connecting to the database
 */
public class AuthorizationException extends ResponseException {
    public AuthorizationException() {
        super();
    }
    public AuthorizationException(String message, Throwable ex) {
        super(message, ex);
    }
}
