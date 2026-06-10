package client.websocket;

import chess.*;
import client.ResponseException;
import com.google.gson.Gson;
import jakarta.websocket.*;
import model.ChessMoveRequest;
import model.GameData;
import websocket.commands.UserGameCommand;
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
//            System.out.println("we in class declaration of WebSocket Facade");
            url = url.replace("http", "ws");
//            System.out.print("this is the new url -> ");
//            System.out.println(url + "/ws");
            URI socketURI = new URI(url + "/ws");

//            System.out.println("got the socketURI!");

            this.serverMessageHandler = serverMessageHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
//            System.out.println("got the web socket container!");
            this.session = container.connectToServer(this, socketURI);
//            System.out.println("got the session!");

            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                public void onMessage(String message) {
//                    System.out.println("we in onMessage!");
//                    System.out.println("\nEnter another message you want to echo:");
                    ServerMessage notification = new Gson().fromJson(message, ServerMessage.class);
//                    System.out.println(notification.getMessage());
//                    System.out.println("the message is above!");
                    try {
                        serverMessageHandler.notify(notification);
                    } catch (ResponseException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (DeploymentException ex) {
//            System.out.println("we got a deployment exception");
            ex.printStackTrace();
            throw new ResponseException();
        } catch (URISyntaxException ex) {
//            System.out.println("we got a uri syntax exception");
            ex.printStackTrace();
            throw new RuntimeException(ex);
        } catch (IOException ex) {
//            System.out.println("we got an IO exception");
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

    public void makeMove(String authToken, int gameID, String ... params) throws InvalidMoveException {
        //            System.out.println("we are in leaveGame of WebSocketFacade!");
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
            var command = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID, new ChessMoveRequest(
                    new ChessMove(new ChessPosition(startY, startX), new ChessPosition(endY, endX), null)));
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (InvalidMoveException e) {
            throw new InvalidMoveException();
//            return "Sorry that wasn't a valid move! Check the valid moves for that piece with the command \"highlight\" <PIECE POSITION>";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //            System.out.println("we sent the leaveGame command to the session!");
    }

    public void leaveGame(String authToken, int gameID) {
        try {
//            System.out.println("we are in leaveGame of WebSocketFacade!");
            var command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID, null);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
//            System.out.println("we sent the leaveGame command to the session!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void resignFromGame() {

    }

    public void send(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    // This method must be overridden, but we don't have to do anything with it
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

}
