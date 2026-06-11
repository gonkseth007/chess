package client.websocket;

import client.ResponseException;
import websocket.messages.ServerMessage;

public interface ServerMessageHandler {
    void notify(ServerMessage message) throws ResponseException;

    boolean confirmResign();
}
