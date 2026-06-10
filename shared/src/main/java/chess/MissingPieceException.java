package chess;

/**
 * Indicates an invalid move was made in a game
 */
public class MissingPieceException extends InvalidMoveException {

    public MissingPieceException() {}

    public MissingPieceException(String message) {
        super(message);
    }
}
