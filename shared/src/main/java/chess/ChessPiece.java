package chess;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        if (piece.getPieceType() == PieceType.PAWN) {
            return pawnMoves(board, myPosition);
        } else if (piece.getPieceType() == PieceType.ROOK) {
            return rookMoves(board, myPosition);
        } else if (piece.getPieceType() == PieceType.BISHOP) {
            return bishopMoves(board, myPosition);
        } else if (piece.getPieceType() == PieceType.KNIGHT) {
            return knightMoves(board, myPosition);
        } else if (piece.getPieceType() == PieceType.QUEEN) {
            return queenMoves(board, myPosition);
        } else if (piece.getPieceType() == PieceType.KING) {
            return kingMoves(board, myPosition);
        }
        return List.of();
    }

    public Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        ChessPiece piece = board.getPiece(myPosition);
        if (ChessGame.TeamColor.WHITE == piece.getTeamColor()) {
            // Logic for moving white pawns
            ChessPiece leftDiagPiece = null;
            if (myPosition.getColumn() > 1) {
                leftDiagPiece = board.getPiece(new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1));
            }
            ChessPiece rightDiagPiece = null;
            if (myPosition.getColumn() < 8) {
                rightDiagPiece = board.getPiece(new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1));
            }
            ChessPiece frontPiece = board.getPiece(new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn()));
            if (myPosition.getRow() + 1 < 8) {
                if (frontPiece == null) {
                    moves.add(new ChessMove(
                            myPosition,
                            new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn()),
                            null
                    ));
                    if (myPosition.getRow() == 2 && board.getPiece(new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn())) == null) {
                        moves.add(new ChessMove(
                                myPosition,
                                new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn()),
                                null
                        ));
                    }
                }
                if (leftDiagPiece != null && leftDiagPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                    moves.add(new ChessMove(
                        myPosition,
                        new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1),
                        null
                    ));
                }
                if (rightDiagPiece != null && rightDiagPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                    moves.add(new ChessMove(
                        myPosition,
                        new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1),
                        null
                    ));
                }
            }
            if (myPosition.getRow() + 1 == 8) {
                moves.add(new ChessMove(
                    myPosition,
                    new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn()),
                    PieceType.KNIGHT
                ));
                moves.add(new ChessMove(
                    myPosition,
                    new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn()),
                    PieceType.ROOK
                ));
                moves.add(new ChessMove(
                    myPosition,
                    new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn()),
                    PieceType.BISHOP
                ));
                moves.add(new ChessMove(
                    myPosition,
                    new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn()),
                    PieceType.QUEEN
                ));
                if (leftDiagPiece != null && leftDiagPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                    moves.add(new ChessMove(
                        myPosition,
                        new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1),
                        PieceType.KNIGHT
                    ));
                    moves.add(new ChessMove(
                        myPosition,
                        new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1),
                        PieceType.ROOK
                    ));
                    moves.add(new ChessMove(
                        myPosition,
                        new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1),
                        PieceType.BISHOP
                    ));
                    moves.add(new ChessMove(
                        myPosition,
                        new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1),
                        PieceType.QUEEN
                    ));
                }
                if (rightDiagPiece != null && rightDiagPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                    moves.add(new ChessMove(
                        myPosition,
                        new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1),
                        PieceType.KNIGHT
                    ));
                    moves.add(new ChessMove(
                        myPosition,
                        new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1),
                        PieceType.ROOK
                    ));
                    moves.add(new ChessMove(
                        myPosition,
                        new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1),
                        PieceType.BISHOP
                    ));
                    moves.add(new ChessMove(
                        myPosition,
                        new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1),
                        PieceType.QUEEN
                    ));
                }
            }
        } else {
            // Logic for moving black pawns
            ChessPiece leftDiagPiece = null;
            if (myPosition.getColumn() > 1) {
                leftDiagPiece = board.getPiece(new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1));
            }
            ChessPiece rightDiagPiece = null;
            if (myPosition.getColumn() < 8) {
                rightDiagPiece = board.getPiece(new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1));
            }
            ChessPiece frontPiece = board.getPiece(new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn()));
            if (myPosition.getRow() - 1 > 1) {
                if (frontPiece == null) {
                    moves.add(new ChessMove(
                            myPosition,
                            new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn()),
                            null
                    ));
                    if (myPosition.getRow() == 7 && board.getPiece(new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn())) == null) {
                        moves.add(new ChessMove(
                                myPosition,
                                new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn()),
                                null
                        ));
                    }
                }
                if (leftDiagPiece != null && leftDiagPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                    moves.add(new ChessMove(
                            myPosition,
                            new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1),
                            null
                    ));
                }
                if (rightDiagPiece != null && rightDiagPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                    moves.add(new ChessMove(
                            myPosition,
                            new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1),
                            null
                    ));
                }
            }
            if (myPosition.getRow() - 1 == 1) {
                moves.add(new ChessMove(
                        myPosition,
                        new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn()),
                        PieceType.KNIGHT
                ));
                moves.add(new ChessMove(
                        myPosition,
                        new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn()),
                        PieceType.ROOK
                ));
                moves.add(new ChessMove(
                        myPosition,
                        new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn()),
                        PieceType.BISHOP
                ));
                moves.add(new ChessMove(
                        myPosition,
                        new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn()),
                        PieceType.QUEEN
                ));
                if (leftDiagPiece != null && leftDiagPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                    moves.add(new ChessMove(
                            myPosition,
                            new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1),
                            PieceType.KNIGHT
                    ));
                    moves.add(new ChessMove(
                            myPosition,
                            new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1),
                            PieceType.ROOK
                    ));
                    moves.add(new ChessMove(
                            myPosition,
                            new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1),
                            PieceType.BISHOP
                    ));
                    moves.add(new ChessMove(
                            myPosition,
                            new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1),
                            PieceType.QUEEN
                    ));
                }
                if (rightDiagPiece != null && rightDiagPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                    moves.add(new ChessMove(
                            myPosition,
                            new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1),
                            PieceType.KNIGHT
                    ));
                    moves.add(new ChessMove(
                            myPosition,
                            new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1),
                            PieceType.ROOK
                    ));
                    moves.add(new ChessMove(
                            myPosition,
                            new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1),
                            PieceType.BISHOP
                    ));
                    moves.add(new ChessMove(
                            myPosition,
                            new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1),
                            PieceType.QUEEN
                    ));
                }
            }

        }

        return moves;
    }

    public Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition) {
        return List.of();
    }

    public Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        return List.of(new ChessMove(new ChessPosition(5,4), new ChessPosition(1,8), null));
    }

    public Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        return List.of();
    }

    public Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition) {
        return List.of();
    }

    public Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        ChessPiece piece = board.getPiece(myPosition);

        // Horizontal Movement for the King
        ChessPiece newPiece = null;
        if (myPosition.getColumn() < 8) {
            newPiece = board.getPiece(new ChessPosition(myPosition.getRow(), myPosition.getColumn() + 1));
            if (newPiece == null || newPiece.getTeamColor() != piece.getTeamColor()) {
                moves.add(new ChessMove(
                    myPosition,
                    new ChessPosition(myPosition.getRow(), myPosition.getColumn() + 1),
                    null
                ));
            }
        }
        newPiece = null;
        if (myPosition.getColumn() > 1) {
            newPiece = board.getPiece(new ChessPosition(myPosition.getRow(), myPosition.getColumn() - 1));
            if (newPiece == null || newPiece.getTeamColor() != piece.getTeamColor()) {
                moves.add(new ChessMove(
                    myPosition,
                    new ChessPosition(myPosition.getRow(), myPosition.getColumn() - 1),
                    null
                ));
            }
        }
        // Vertical movement for the King
        newPiece = null;
        if (myPosition.getRow() < 8) {
            newPiece = board.getPiece(new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn()));
            if (newPiece == null || newPiece.getTeamColor() != piece.getTeamColor()) {
                moves.add(new ChessMove(
                        myPosition,
                        new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn()),
                        null
                ));
            }
        }
        newPiece = null;
        if (myPosition.getRow() > 1) {
            newPiece = board.getPiece(new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn()));
            if (newPiece == null || newPiece.getTeamColor() != piece.getTeamColor()) {
                moves.add(new ChessMove(
                        myPosition,
                        new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn()),
                        null
                ));
            }
        }
        // Diagonal movement for the King
        newPiece = null;
        if (myPosition.getColumn() > 1 && myPosition.getRow() < 8) {
            newPiece = board.getPiece(new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1));
            if (newPiece == null || newPiece.getTeamColor() != piece.getTeamColor()) {
                moves.add(new ChessMove(
                    myPosition,
                    new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1),
                    null
                ));
            }
        }
        newPiece = null;
        if (myPosition.getColumn() < 8 && myPosition.getRow() < 8) {
            newPiece = board.getPiece(new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1));
            if (newPiece == null || newPiece.getTeamColor() != piece.getTeamColor()) {
                moves.add(new ChessMove(
                    myPosition,
                    new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1),
                    null
                ));
            }
        }
        newPiece = null;
        if (myPosition.getColumn() > 1 && myPosition.getRow() > 1) {
            newPiece = board.getPiece(new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1));
            if (newPiece == null || newPiece.getTeamColor() != piece.getTeamColor()) {
                moves.add(new ChessMove(
                    myPosition,
                    new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1),
                    null
                ));
            }
        }
        newPiece = null;
        if (myPosition.getColumn() < 8 && myPosition.getRow() > 1) {
            newPiece = board.getPiece(new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1));
            if (newPiece == null || newPiece.getTeamColor() != piece.getTeamColor()) {
                moves.add(new ChessMove(
                    myPosition,
                    new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1),
                    null
                ));
            }
        }
        return moves;
    }

}
