package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn = TeamColor.WHITE;
    private ChessBoard board = new ChessBoard();
    private boolean gameEnded = false;
    public ChessGame() {
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * @return If game has ended
     */
    public boolean getGameEnded() {
        return gameEnded;
    }

    /**
     * Ends the game
     */
    public void endGame() {
        gameEnded = true;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        List<ChessMove> newMoves = new ArrayList<>();
        for (ChessMove move : moves) {
            ChessPiece takenPiece = movePiece(move);
            if (!isInCheck(piece.getTeamColor())) {
                newMoves.add(move);
            }
            undoMove(move, takenPiece);
        }
        return newMoves;
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new MissingPieceException("there is no piece there!");
        }
        Collection<ChessMove> allValidMoves = validMoves(move.getStartPosition());
        TeamColor team = piece.getTeamColor();
        if (!allValidMoves.contains(move)) {
            throw new InvalidMoveException("Move not valid!");
        } else if (team != getTeamTurn()) {
            throw new WrongTurnException("It's not your turn!");
        }
        movePiece(move);
        if (move.getPromotionPiece() != null) {
            ChessPiece promotionPiece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
            board.addPiece(move.getEndPosition(), promotionPiece);
        }
        changeTeamTurn();
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        int[] kingP = getKingPiece(teamColor);
        TeamColor opponentTeam;
        if (teamColor == TeamColor.BLACK) {
            opponentTeam = TeamColor.WHITE;
        } else {
            opponentTeam = TeamColor.BLACK;
        }
        if (kingP != null) {
            ChessPosition kingPos = new ChessPosition(kingP[0], kingP[1]);
            List<Collection<ChessMove>> moves = getTeamMoves(opponentTeam);
            for (Collection<ChessMove> pieceMoves : moves) {
                for (ChessMove move : pieceMoves) {
                    int row = move.getEndPosition().getRow();
                    int col = move.getEndPosition().getColumn();
                    if (row == kingPos.getRow() && col == kingPos.getColumn()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && noValidMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && noValidMoves(teamColor);
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }

    public void changeTeamTurn() {
        if (teamTurn == TeamColor.WHITE) {
            teamTurn = TeamColor.BLACK;
        } else {
            teamTurn = TeamColor.WHITE;
        }
    }

    private boolean noValidMoves(TeamColor teamColor) {
        List<ChessPosition> positions = board.getAllTeamStartPositions(teamColor);

        for (ChessPosition position : positions) {
            if (!validMoves(position).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private List<Collection<ChessMove>> getTeamMoves(TeamColor teamColor) {
        List<ChessPiece> pieces = board.getAllTeamPieces(teamColor);
        List<ChessPosition> positions = board.getAllTeamStartPositions(teamColor);
        List<Collection<ChessMove>> moves = new ArrayList<>();
        for (int i = 0; i < pieces.size(); i++) {
            ChessPiece piece = pieces.get(i);
            ChessPosition position = positions.get(i);
            moves.add(piece.pieceMoves(board, position));
        }
        return moves;
    }

    private int[] getKingPiece(TeamColor teamColor) {
        ChessPiece kingPiece;
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                kingPiece = board.getPiece(new ChessPosition(
                        i,
                        j
                ));
                if (kingPiece != null && kingPiece.getPieceType() == ChessPiece.PieceType.KING && kingPiece.getTeamColor() == teamColor) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    private ChessPiece movePiece(ChessMove move) {
        ChessPiece movedPiece = board.getPiece(move.getStartPosition());
        ChessPiece takenPiece = board.getPiece(move.getEndPosition());
        board.addPiece(move.getEndPosition(),movedPiece);
        board.removePiece(move.getStartPosition());
        return takenPiece;
    }

    private void undoMove(ChessMove move, ChessPiece takenPiece) {
        ChessPiece movedPiece = board.getPiece(move.getEndPosition());
        board.addPiece(move.getStartPosition(),movedPiece);
        board.addPiece(move.getEndPosition(),takenPiece);
    }
}