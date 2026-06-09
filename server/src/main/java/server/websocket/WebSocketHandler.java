package server.websocket;

//import exception.ResponseException;
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
import service.UserService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
//import webSocketMessages.Action;
//import client.ResponseException;

import java.io.IOException;
import java.util.Objects;

import static websocket.commands.UserGameCommand.CommandType.*;

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
                case MAKE_MOVE -> makeMove(command.getAuthToken(), ctx.session);
                case LEAVE -> leave(command.getAuthToken(), command.getGameID(), ctx.session);
                case RESIGN -> resign(command.getAuthToken(), ctx.session);
            }
        }
        catch (IOException | DataAccessException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connect(String authToken, int gameID, Session session) throws IOException, DataAccessException {
//        System.out.println("we are adding a session");
        String message;
        AuthData auth = authDAO.getAuth(authToken);
        GameData game = gameDAO.getGame(gameID);
        if (Objects.equals(game.whiteUsername(), auth.username())) {
            message = String.format("%s joined as the white player", auth.username());
        } else if (Objects.equals(game.blackUsername(), auth.username())) {
            message = String.format("%s joined as the black player", auth.username());
        }  else {
            message = String.format("%s began observing the game", auth.username());
        }
        connections.add(session);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
//        System.out.println("we are broadcasting the message from leave function of WebSocketHandler!");
        connections.broadcast(session, notification);
    }

    private void makeMove(String authToken, Session session) throws IOException {
        var message = "bout to print the board fr fr";
        var notification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, message);
        connections.broadcast(session, notification);
        connections.remove(session);
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
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
//        System.out.println("we are broadcasting the message from leave function of WebSocketHandler!");
        connections.broadcast(session, notification);
        connections.remove(session);
    }

    private void resign(String authToken, Session session) {

    }
}