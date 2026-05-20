package dataaccess;

/**
 * Indicates there was an error connecting to the database
 */
public class AlreadyTakenException extends DataAccessException {
    public AlreadyTakenException() {
        super();
    }
}
