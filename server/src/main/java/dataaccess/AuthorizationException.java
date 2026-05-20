package dataaccess;

/**
 * Indicates there was an error connecting to the database
 */
public class AuthorizationException extends DataAccessException {
    public AuthorizationException() {
        super();
    }
    public AuthorizationException(String message, Throwable ex) {
        super(message, ex);
    }
}
