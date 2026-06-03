package client;

import dataaccess.DataAccessException;

/**
 * Indicates there was an error connecting to the database
 */
public class BadRequestException extends ResponseException {
    public BadRequestException() {
        super();
    }
}
