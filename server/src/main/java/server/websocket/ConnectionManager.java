package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.HashMap;

public class ConnectionManager {
    public final HashMap<Session, Integer> connections = new HashMap<>();

    public void add(Session session, int gameID) {
        connections.put(session, gameID);
    }

    public void remove(Session session) {
        connections.remove(session);
    }

    public void broadcast(Session excludeSession, int gameID, ServerMessage message) throws IOException {
        String msg = message.toString();
//        System.out.println("we are in broadcast function of ConnectionManager");
//        System.out.printf("we have %d sessions!%n", connections.size());
        for (Session c : connections.keySet()) {
//            System.out.println("in loop");
            if (c.isOpen() && connections.get(c) == gameID) {
                if (!c.equals(excludeSession)) {
//                    System.out.println("we are sending the message to the different clients");
                    c.getRemote().sendString(msg);
                }
            }
        }
    }
}
