package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PawnMoves {

    public PawnMoves() {}

    public Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        if (ChessGame.TeamColor.WHITE == piece.getTeamColor()) {
            // Logic for moving white pawns
            return whitePawnMoves(board, myPosition);
        } else {
            // Logic for moving black pawns
            return blackPawnMoves(board, myPosition);
        }
    }

    public Collection<ChessMove> whitePawnMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
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
            moves.addAll(promotePawn(myPosition, myPosition.getRow() + 1, myPosition.getColumn()));
            if (leftDiagPiece != null && leftDiagPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                moves.addAll(promotePawn(myPosition, myPosition.getRow() + 1, myPosition.getColumn() - 1));
            }
            if (rightDiagPiece != null && rightDiagPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                moves.addAll(promotePawn(myPosition, myPosition.getRow() + 1, myPosition.getColumn() + 1));
            }
        }
        return moves;
    }

    public Collection<ChessMove> blackPawnMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
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
            moves.addAll(promotePawn(myPosition, myPosition.getRow() - 1, myPosition.getColumn()));
            if (leftDiagPiece != null && leftDiagPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                moves.addAll(promotePawn(myPosition, myPosition.getRow() - 1, myPosition.getColumn() - 1));
            }
            if (rightDiagPiece != null && rightDiagPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                moves.addAll(promotePawn(myPosition, myPosition.getRow() - 1, myPosition.getColumn() + 1));
            }
        }
        return moves;
    }

    public Collection<ChessMove> promotePawn(ChessPosition myPosition, int row, int col) {
        List<ChessMove> moves = new ArrayList<>();
        moves.add(new ChessMove(
                myPosition,
                new ChessPosition(row, col),
                ChessPiece.PieceType.KNIGHT
        ));
        moves.add(new ChessMove(
                myPosition,
                new ChessPosition(row, col),
                ChessPiece.PieceType.ROOK
        ));
        moves.add(new ChessMove(
                myPosition,
                new ChessPosition(row, col),
                ChessPiece.PieceType.BISHOP
        ));
        moves.add(new ChessMove(
                myPosition,
                new ChessPosition(row, col),
                ChessPiece.PieceType.QUEEN
        ));
        return moves;
    }
}
