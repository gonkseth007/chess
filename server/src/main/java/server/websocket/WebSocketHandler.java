package server.websocket;

//import exception.ResponseException;
import chess.*;
import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final AuthDataAccess authDAO;
    private final UserDataAccess userDAO;
    private final GameDataAccess gameDAO;

    public WebSocketHandler(AuthDataAccess authDAO, UserDataAccess userDAO, GameDataAccess gameDAO) {
        this.authDAO = authDAO;
        this.userDAO = userDAO;
        this.gameDAO = gameDAO;
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
//            System.out.println("we are in handleMessage of WebSocketHandler!");
            UserGameCommand command = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> connect(command.getAuthToken(), command.getGameID(), ctx.session);
                case MAKE_MOVE -> makeMove(command.getAuthToken(), command.getGameID(), command.getMove(), ctx.session);
                case LEAVE -> leave(command.getAuthToken(), command.getGameID(), ctx.session);
                case RESIGN -> resign(command.getAuthToken(), ctx.session);
            }
        }
        catch (IOException | DataAccessException | InvalidMoveException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connect(String authToken, int gameID, Session session) throws IOException, DataAccessException {
//        System.out.println("we are adding a session");
        String broadcastMessage;
//        String message;
        AuthData auth = authDAO.getAuth(authToken);
        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            String errorMessage = "Error: that game is invalid";
            var error = new ErrorMessage(errorMessage);
//        System.out.println("we are broadcasting the message from leave function of WebSocketHandler!");
            connections.broadcast(session, gameID, error);
            return;
        }
        if (Objects.equals(game.whiteUsername(), auth.username())) {
            broadcastMessage = String.format("%s joined as the white player", auth.username());
//            message = "You joined as the white player";
        } else if (Objects.equals(game.blackUsername(), auth.username())) {
            broadcastMessage = String.format("%s joined as the black player", auth.username());
//            message = "You joined as the black player";
        }  else {
            broadcastMessage = String.format("%s began observing the game", auth.username());
//            message = "You joined as an observer";
        }
        connections.add(session, gameID);
        var notification = new NotificationMessage(broadcastMessage);
//        System.out.println("we are broadcasting the message from leave function of WebSocketHandler!");
        connections.broadcast(session, gameID, notification);

//        notification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, message, game.game());
        var gameMessage = new LoadGameMessage(game.game());
        connections.broadcastBack(session, gameMessage);
    }

    private void makeMove(String authToken, int gameID, ChessMove req, Session session) throws IOException, DataAccessException, InvalidMoveException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            String message = "Oops you aren't authorized to do that! Try logging in again!";
//            var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, message);
//            connections.broadcast(session, gameID, notification);
            connections.broadcastBack(session, new ErrorMessage(message));
        } else {
            GameData gameData = gameDAO.getGame(gameID);
            ChessGame.TeamColor teamColor = null;
            if (Objects.equals(gameData.whiteUsername(), auth.username())) {
                teamColor = ChessGame.TeamColor.BLACK;
            } else if (Objects.equals(gameData.blackUsername(), auth.username())) {
                teamColor = ChessGame.TeamColor.WHITE;
            }
            ChessGame game = gameData.game();
            game.getBoard();
            ChessPiece piece = game.getBoard().getPiece(req.getStartPosition());
            ChessPiece.PieceType promotionPiece = getPromotionPiece(piece, req.getEndPosition().getRow());
            game.makeMove(new ChessMove(
                    req.getStartPosition(),
                    req.getEndPosition(),
                    promotionPiece
            ));
            gameDAO.updateGame(new GameData(
                    gameID,
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    game
            ));
            String pieceType = piece.getPieceType().toString();
            pieceType = pieceType.substring(0, 1).toUpperCase() + pieceType.substring(1).toLowerCase();
            if (game.isInCheckmate(teamColor)) {
                String message = String.format("moved %s from %c%d to %c%d and has won the game!",
                        pieceType,
                        (char) req.getStartPosition().getColumn() + 'a' - 1,
                        req.getStartPosition().getRow(),
                        (char) req.getEndPosition().getColumn() + 'a' - 1,
                        req.getEndPosition().getRow()
                );
                String broadcastMessage = String.format("Checkmate! %s %s", auth.username(), message);
//                message = String.format("Checkmate! %s %s", "You", message);
                connections.broadcast(session, gameID, new LoadGameMessage(game));
                connections.broadcast(session, gameID, new NotificationMessage(broadcastMessage));
                connections.broadcastBack(session, new LoadGameMessage(game));
//                connections.broadcastBack(session, new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, message, game));
                return;
            } else if (game.isInStalemate(teamColor)) {
                String message = String.format("moved %s from %c%d to %c%d",
                        pieceType,
                        (char) req.getStartPosition().getColumn() + 'a' - 1,
                        req.getStartPosition().getRow(),
                        (char) req.getEndPosition().getColumn() + 'a' - 1,
                        req.getEndPosition().getRow()
                );
                String broadcastMessage = String.format("Drats its a stalemate! %s %s", auth.username(), message);
//                message = String.format("Drats its a stalemate! %s %s", "You", message);
                connections.broadcast(session, gameID, new LoadGameMessage(game));
                connections.broadcast(session, gameID, new NotificationMessage(broadcastMessage));
                connections.broadcastBack(session, new LoadGameMessage(game));
//                connections.broadcastBack(session, new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, message, game));
                return;
            } else if (game.isInCheck(teamColor)) {
                String broadcastMessage = String.format("Check! %s moved %s from %c%d to %c%d",
                        auth.username(),
                        pieceType,
                        (char) req.getStartPosition().getColumn() + 'a' - 1,
                        req.getStartPosition().getRow(),
                        (char) req.getEndPosition().getColumn() + 'a' - 1,
                        req.getEndPosition().getRow()

                );
//                String enemyColor;
//                if (teamColor == ChessGame.TeamColor.BLACK) {
//                    enemyColor = "white";
//                } else {
//                    enemyColor = "black";
//                }
//                String message = String.format("You moved %s from %c%d to %c%d and put %s in check!",
//                        pieceType,
//                        (char) req.getStartPosition().getColumn() + 'a' - 1,
//                        req.getStartPosition().getRow(),
//                        (char) req.getEndPosition().getColumn() + 'a' - 1,
//                        req.getEndPosition().getRow(),
//                        enemyColor
//                );
                connections.broadcast(session, gameID, new LoadGameMessage(game));
                connections.broadcast(session, gameID, new NotificationMessage(broadcastMessage));
                connections.broadcastBack(session, new LoadGameMessage(game));
//                connections.broadcastBack(session, new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, message, game));
                return;
            }
            String broadcastMessage = String.format("%s moved %s from %c%d to %c%d",
                    auth.username(),
                    pieceType,
                    (char) req.getStartPosition().getColumn() + 'a' - 1,
                    req.getStartPosition().getRow(),
                    (char) req.getEndPosition().getColumn() + 'a' - 1,
                    req.getEndPosition().getRow()
            );
//            String message = String.format("You moved %s from %c%d to %c%d",
//                    pieceType,
//                    (char) req.getStartPosition().getColumn() + 'a' - 1,
//                    req.getStartPosition().getRow(),
//                    (char) req.getEndPosition().getColumn() + 'a' - 1,
//                    req.getEndPosition().getRow()
//            );
            var gameMessage = new LoadGameMessage(game);
            connections.broadcast(session, gameID, gameMessage);
            var notification = new NotificationMessage(broadcastMessage);
            connections.broadcast(session, gameID, notification);
            gameMessage = new LoadGameMessage(game);
//            notification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, message, game);
            connections.broadcastBack(session, gameMessage);
        }
    }

    private void leave(String authToken, int gameID, /*String username, boolean isPlaying, String playerColor,*/ Session session) throws IOException, DataAccessException {
//        System.out.println("we are in leave function of WebSocketHandler!");
        String message;
        AuthData auth = authDAO.getAuth(authToken);
        GameData game = gameDAO.getGame(gameID);
        if (Objects.equals(game.whiteUsername(), auth.username())) {
            message = String.format("The white player %s left the game", auth.username());
            GameData updatedGame = new GameData(gameID, null, game.blackUsername(), game.gameName(), game.game());
            gameDAO.updateGame(updatedGame);
        } else if (Objects.equals(game.blackUsername(), auth.username())) {
            message = String.format("The black player %s left the game", auth.username());
            GameData updatedGame = new GameData(gameID, game.whiteUsername(), null, game.gameName(), game.game());
            gameDAO.updateGame(updatedGame);
        }  else {
            message = String.format("The observer %s left the game", auth.username());
        }
        var notification = new NotificationMessage(message);
//        System.out.println("we are broadcasting the message from leave function of WebSocketHandler!");
        connections.broadcast(session, gameID, notification);
        connections.remove(session);
    }

    private void resign(String authToken, Session session) {

    }

    private ChessPiece.PieceType getPromotionPiece(ChessPiece piece, int endY) {
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE && endY == 8) {
                Scanner scanner = new Scanner(System.in);
                String promotionPiece = "";
//                while (!promotionPiece.equals("QUEEN") && !promotionPiece.equals("ROOK") && !promotionPiece.equals("KNIGHT") && !promotionPiece.equals("BISHOP")) {
////                    System.out.println(SET_TEXT_COLOR_GREEN + pawnPromotionPrompt());
//                    promotionPiece = scanner.nextLine().toUpperCase();
//                }
//                return ChessPiece.PieceType.valueOf(promotionPiece);
            } else if (piece.getTeamColor() == ChessGame.TeamColor.BLACK && endY == 1) {
                Scanner scanner = new Scanner(System.in);
//                String promotionPiece = "";
//                while (!promotionPiece.equals("QUEEN") && !promotionPiece.equals("ROOK") && !promotionPiece.equals("KNIGHT") && !promotionPiece.equals("BISHOP")) {
////                    System.out.println(SET_TEXT_COLOR_GREEN + pawnPromotionPrompt());
//                    promotionPiece = scanner.nextLine().toUpperCase();
//                }
//                return ChessPiece.PieceType.valueOf(promotionPiece);
            }
        }
        return null;
    }
}