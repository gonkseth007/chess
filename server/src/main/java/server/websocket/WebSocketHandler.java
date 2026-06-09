package server.websocket;

//import exception.ResponseException;
import com.google.gson.Gson;
import dataaccess.MemoryUserDataAccess;
import dataaccess.MySqlAuthDataAccess;
import dataaccess.MySqlUserDataAccess;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import org.eclipse.jetty.websocket.api.Session;
import service.UserService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
//import webSocketMessages.Action;
//import client.ResponseException;

import java.io.IOException;

import static websocket.commands.UserGameCommand.CommandType.*;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();

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
                case CONNECT -> connect(command, ctx.session);
                case MAKE_MOVE -> makeMove(command.getAuthToken(), ctx.session);
                case LEAVE -> leave(command.getAuthToken(), ctx.session);
                case RESIGN -> resign(command.getAuthToken(), ctx.session);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connect(UserGameCommand command, Session session) throws IOException {
        System.out.println("we are adding a session");
        connections.add(session);
//        var message = String.format("%s is in the shop", visitorName);
//        var notification = new Notification(Notification.Type.ARRIVAL, message);
//        connections.broadcast(session, notification);
    }

    private void makeMove(String authToken, Session session) throws IOException {
        var message = "bout to print the board fr fr";
        var notification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, message);
        connections.broadcast(session, notification);
        connections.remove(session);
    }

    private void leave(String authToken,/*String username, boolean isPlaying, String playerColor,*/ Session session) throws IOException {
//        System.out.println("we are in leave function of WebSocketHandler!");
        String message = "";
//        if (isPlaying) {
//            message = String.format("The %s player %s left the game", playerColor, username);
//        } else {
//            message = String.format("The observer %s left the game", username);
//        }
        message = "the user is leaving rn";
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
//        System.out.println("we are broadcasting the message from leave function of WebSocketHandler!");
        connections.broadcast(session, notification);
        connections.remove(session);
    }

    private void resign(String authToken, Session session) {

    }
}