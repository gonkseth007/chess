package client;

/**
 * Indicates there was an error connecting to the database
 */
public class AlreadyTakenException extends ResponseException {
    public AlreadyTakenException() {
        super();
    }
}
