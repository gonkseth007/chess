package chess;

/**
 * Indicates an invalid move was made in a game
 */
public class WrongTurnException extends InvalidMoveException {

    public WrongTurnException() {}

    public WrongTurnException(String message) {
        super(message);
    }
}
