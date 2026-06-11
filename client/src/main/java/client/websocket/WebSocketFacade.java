package client.websocket;

import chess.*;
import client.AuthorizationException;
import client.ResponseException;
import com.google.gson.Gson;
import jakarta.websocket.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

//need to extend Endpoint for websocket to work properly
public class WebSocketFacade extends Endpoint {
    public Session session;
    ServerMessageHandler serverMessageHandler;

    public WebSocketFacade(String url, ServerMessageHandler serverMessageHandler) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");

            this.serverMessageHandler = serverMessageHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                public void onMessage(String message) {
                    ServerMessage notification = new Gson().fromJson(message, ServerMessage.class);
                    if (notification.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION) {
                        notification = new Gson().fromJson(message, NotificationMessage.class);
                    } else if (notification.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
                        notification = new Gson().fromJson(message, LoadGameMessage.class);
                    } else {
                        notification = new Gson().fromJson(message, ErrorMessage.class);
                    }
                    try {
                        serverMessageHandler.notify(notification);
                    } catch (ResponseException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (DeploymentException ex) {
            ex.printStackTrace();
            throw new ResponseException();
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public void connectToGame(String authToken, int gameID) throws ResponseException {
        try {
            var command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID, null);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException e) {
            throw new ResponseException();
        }
    }

    public void makeMove(String authToken, int gameID, String ... params) throws InvalidMoveException, AuthorizationException {
        try {
            if (params[0].length() != 2 || params[1].length() != 2) {
                throw new InvalidMovePositionsException();
            }
            int startX = params[0].charAt(0) - 'a' + 1;
            int startY = Character.getNumericValue(params[0].charAt(1));
            int endX = params[1].charAt(0) - 'a' + 1;
            int endY = Character.getNumericValue(params[1].charAt(1));
            if (startY < 1 || startY > 8 || endY < 1 || endY > 8 || startX < 1 || startX > 8 || endX < 1 || endX > 8) {
                throw new InvalidMovePositionsException();
            }
            var command = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID,
                    new ChessMove(
                            new ChessPosition(startY, startX),
                            new ChessPosition(endY, endX),
                            null));
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (InvalidMovePositionsException ex) {
            throw new InvalidMovePositionsException();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void leaveGame(String authToken, int gameID) {
        try {
            var command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID, null);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void resignFromGame(String authToken, int gameID) {
        try {
            var command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID, null);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    // This method must be overridden, but we don't have to do anything with it
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

}
