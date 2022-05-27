import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ApplicationScoped
@ServerEndpoint("/chat/{username}")
public class ChatSocket {

    public Map<String, Session> sessions = new HashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        System.out.println(username+" se conecto.");
        sessions.put(username, session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("username") String username) {
        sessions.remove(username);
        broadcast("Usuario: "+username+" abandono la sala.");
    }

    @OnError
    public void onError(Session session, @PathParam("username") String username, Throwable throwable) {
        sessions.remove(username);
        broadcast("Usuario: "+username+" abandono la sala con error: "+throwable);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("username") String username) {
        if (message.equalsIgnoreCase("_ready_")) {
            broadcast("Usuario: "+username+" ingreso a la sala.");
        } else {
            broadcast(">> "+username+": "+message);
        }
    }

    private void broadcast(String message) {
        sessions.values().forEach(s -> {
            s.getAsyncRemote().sendObject(message, result -> {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: "+result.getException());
                }
            });
        });
    }
}
