package chess;

/**
 * Indicates an invalid move was made in a game
 */
public class InvalidMovePositionsException extends InvalidMoveException {

    public InvalidMovePositionsException() {}

    public InvalidMovePositionsException(String message) {
        super(message);
    }
}
