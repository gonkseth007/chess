package server.websocket;

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
import org.jetbrains.annotations.NotNull;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.io.IOException;
import java.util.Objects;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final AuthDataAccess authDAO;
    private final GameDataAccess gameDAO;

    public WebSocketHandler(AuthDataAccess authDAO, GameDataAccess gameDAO) {
        this.authDAO = authDAO;
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
            UserGameCommand command = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> connect(command.getAuthToken(), command.getGameID(), ctx.session);
                case MAKE_MOVE -> makeMove(command.getAuthToken(), command.getGameID(), command.getMove(), ctx.session);
                case LEAVE -> leave(command.getAuthToken(), command.getGameID(), ctx.session);
                case RESIGN -> resign(command.getAuthToken(), command.getGameID(), ctx.session);
            }
        }
        catch (IOException | DataAccessException | InvalidMoveException ex) {
            throw new RuntimeException();
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connect(String authToken, int gameID, Session session) throws IOException, DataAccessException {
        String broadcastMessage;
        AuthData auth = verifyAuth(authToken, session);
        GameData game = verifyGame(gameID, session);
        if (auth == null || game == null) {
            return;
        }
        if (Objects.equals(game.whiteUsername(), auth.username())) {
            broadcastMessage = String.format("%s joined as the white player", auth.username());
        } else if (Objects.equals(game.blackUsername(), auth.username())) {
            broadcastMessage = String.format("%s joined as the black player", auth.username());
        }  else {
            broadcastMessage = String.format("%s began observing the game", auth.username());
        }
        connections.add(session, gameID);
        var notification = new NotificationMessage(broadcastMessage);
        connections.broadcast(session, gameID, notification);
        var gameMessage = new LoadGameMessage(game.game());
        connections.broadcastBack(session, gameMessage);
    }

    private void makeMove(String authToken, int gameID, ChessMove req, Session session) throws IOException, DataAccessException, InvalidMoveException {
        try {
            AuthData auth = verifyAuth(authToken, session);
            GameData gameData = verifyGame(gameID, session);
            if (auth == null || gameData == null) {
                return;
            }
            ChessGame.TeamColor teamColor;
            if (Objects.equals(gameData.whiteUsername(), auth.username())) {
                teamColor = ChessGame.TeamColor.BLACK;
            } else if (Objects.equals(gameData.blackUsername(), auth.username())) {
                teamColor = ChessGame.TeamColor.WHITE;
            } else {
                String message = "Error: You can't make a move as an observer! To play, join a game as the white or black player!";
                connections.broadcastBack(session, new ErrorMessage(message));
                return;
            }
            ChessGame game = gameData.game();
            if (gameData.game().getGameEnded()) {
                String message = "Oops the game has already ended!";
                connections.broadcastBack(session, new ErrorMessage(message));
                return;
            }
            ChessPiece piece = game.getBoard().getPiece(req.getStartPosition());
            if (piece.getTeamColor() == teamColor) {
                String message = "Error: Oops! You can't move your opponent's piece!";
                connections.broadcastBack(session, new ErrorMessage(message));
                return;
            } else if ((teamColor == ChessGame.TeamColor.WHITE && piece.getPieceType() == ChessPiece.PieceType.PAWN && req.getEndPosition().getRow() == 1) ||
                    (teamColor == ChessGame.TeamColor.BLACK && piece.getPieceType() == ChessPiece.PieceType.PAWN && req.getEndPosition().getRow() == 8)) {
                String message = "Oops! Since that pawn is being moved to the end of the board, you must enter in the piece you want to promote it to (e.g. \"move e7 e8 QUEEN\")!";
                connections.broadcastBack(session, new ErrorMessage(message));
                return;
            }
            game.makeMove(req);
            String pieceType = piece.getPieceType().toString();
            pieceType = pieceType.substring(0, 1).toUpperCase() + pieceType.substring(1).toLowerCase();
            String broadcastMessage = String.format("%s moved %s from %c%d to %c%d",
                    auth.username(),
                    pieceType,
                    (char) req.getStartPosition().getColumn() + 'a' - 1,
                    req.getStartPosition().getRow(),
                    (char) req.getEndPosition().getColumn() + 'a' - 1,
                    req.getEndPosition().getRow()
            );
            var gameMessage = new LoadGameMessage(game);
            connections.broadcast(session, gameID, gameMessage);
            var notification = new NotificationMessage(broadcastMessage);
            connections.broadcast(session, gameID, notification);
            gameMessage = new LoadGameMessage(game);
            connections.broadcastBack(session, gameMessage);
            String enemyUsername = getEnemyUsername(gameData, auth.username());
            if (game.isInCheckmate(teamColor)) {
                String checkmateBroadcastMessage = String.format("%s was put into checkmate! %s has won the game!", enemyUsername, auth.username());
                String message = String.format("%s was put into checkmate! You won the game!", enemyUsername);
                connections.broadcast(session, gameID, new NotificationMessage(checkmateBroadcastMessage));
                connections.broadcastBack(session, new NotificationMessage(message));
                game.endGame();
            } else if (game.isInStalemate(teamColor)) {
                String message = "Drats its a stalemate!";
                connections.broadcast(session, gameID, new NotificationMessage(message));
                connections.broadcastBack(session, new NotificationMessage(message));
                game.endGame();
            } else if (game.isInCheck(teamColor)) {
                String message = String.format("%s was put into check!", enemyUsername);
                connections.broadcast(session, gameID, new NotificationMessage(message));
                connections.broadcastBack(session, new NotificationMessage(message));
            }

            gameDAO.updateGame(new GameData(
                    gameID,
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    game
            ));
        } catch (IOException | DataAccessException ex) {
            String errorMessage = "Oops an unexpected error occurred!";
            var error = new ErrorMessage(errorMessage);
            connections.broadcastBack(session, error);
        } catch (InvalidMovePositionsException ex) {
            String errorMessage = "Error: Sorry that position isn't valid";
            var error = new ErrorMessage(errorMessage);
            connections.broadcastBack(session, error);
        } catch (WrongTurnException ex) {
            String errorMessage = "Error: Its not your turn - wait until its your turn and then make a move!";
            var error = new ErrorMessage(errorMessage);
            connections.broadcastBack(session, error);
        } catch (InvalidMoveException ex) {
            String errorMessage = "Error: that's not a valid move - try again!";
            var error = new ErrorMessage(errorMessage);
            connections.broadcastBack(session, error);
        }
    }

    private void leave(String authToken, int gameID, /*String username, boolean isPlaying, String playerColor,*/ Session session) throws IOException, DataAccessException {
        String message;
        AuthData auth = verifyAuth(authToken, session);
        GameData game = verifyGame(gameID, session);
        if (auth == null || game == null) {
            return;
        }
        if (Objects.equals(game.whiteUsername(), auth.username())) {
            message = String.format("The white player %s left the game", auth.username());
            GameData updatedGame = new GameData(gameID, null, game.blackUsername(), game.gameName(), game.game());
            gameDAO.updateGame(updatedGame);
        } else if (Objects.equals(game.blackUsername(), auth.username())) {
            message = String.format("The black player %s left the game", auth.username());
            GameData updatedGame = new GameData(gameID, game.whiteUsername(), null, game.gameName(), game.game());
            gameDAO.updateGame(updatedGame);
        } else {
            message = String.format("The observer %s left the game", auth.username());
        }
        var notification = new NotificationMessage(message);
        connections.broadcast(session, gameID, notification);
        connections.remove(session);
    }

    private void resign(String authToken, int gameID, Session session) throws DataAccessException, IOException {
        AuthData auth = verifyAuth(authToken, session);
        GameData gameData = verifyGame(gameID, session);
        if (auth == null || gameData == null) {
            return;
        }
        if (gameData.game().getGameEnded()) {
            String message = "Oops the game has already ended!";
            connections.broadcastBack(session, new ErrorMessage(message));
            return;
        }
        String message;
        if (!Objects.equals(gameData.whiteUsername(), auth.username()) && !Objects.equals(gameData.blackUsername(), auth.username())) {
            message = "Error: You can't resign from the game as an observer!";
            connections.broadcastBack(session, new ErrorMessage(message));
            return;
        } else if (Objects.equals(gameData.whiteUsername(), auth.username())) {
            message = String.format("The white player %s resigned from the game", auth.username());
        } else {
            message = String.format("The black player %s resigned from the game", auth.username());
        }
        gameData.game().endGame();
        GameData updatedGame = new GameData(gameID, null, gameData.blackUsername(), gameData.gameName(), gameData.game());
        gameDAO.updateGame(updatedGame);
        var notification = new NotificationMessage(message);
        connections.broadcast(session, gameID, notification);
        connections.broadcastBack(session, new NotificationMessage("You resigned from the game!"));
    }

    private GameData verifyGame(int gameID, Session session) throws DataAccessException, IOException {
        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            String errorMessage = "Error: could not find that game!";
            var error = new ErrorMessage(errorMessage);
            connections.broadcastBack(session, error);
        }
        return game;
    }

    private AuthData verifyAuth(String authToken, Session session) throws IOException, DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            String message = "Error: You aren't authorized to do that! Try logging in again!";
            connections.broadcastBack(session, new ErrorMessage(message));
        }
        return auth;
    }

    private String getEnemyUsername(GameData game, String username) {
        if (Objects.equals(game.whiteUsername(), username)) {
            return game.blackUsername();
        } else {
            return game.whiteUsername();
        }
    }
}