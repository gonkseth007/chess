package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ConnectionManager {
    public final Set<Session> connections = new HashSet<>();

    public void add(Session session) {
        connections.add(session);
    }

    public void remove(Session session) {
        connections.remove(session);
    }

    public void broadcast(Session excludeSession, ServerMessage message) throws IOException {
        String msg = message.toString();
        for (Session c : connections) {
            if (c.isOpen()) {
                if (!c.equals(excludeSession)) {
                    c.getRemote().sendString(msg);
                }
            }
        }
    }
}
