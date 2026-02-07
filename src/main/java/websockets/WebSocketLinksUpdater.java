package websockets;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import models.ChatMessage;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/links-websocket")
public class WebSocketLinksUpdater {
    private static final Map<Integer, Set<Session>> fileSessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("WS OPEN: session " + session.getId());
    }

    @OnMessage
    public void onMessage(Session session, String msg) {
        System.out.println("WS RECV: " + msg);
        JSONObject o = new JSONObject(msg);
        String type = o.optString("type", "");

        if ("subscribe".equals(type)) {
            int fid = o.getInt("fileId");
            session.getUserProperties().put("fileId", fid);
            fileSessions
                    .computeIfAbsent(fid, k -> ConcurrentHashMap.newKeySet())
                    .add(session);
            System.out.println("subscribed session " + session.getId() + " to file/chat " + fid);
        }
        else if ("change".equals(type)) {
            int fid = (Integer) session.getUserProperties().get("fileId");
            Set<Session> subs = fileSessions.getOrDefault(fid, Collections.emptySet());

            // AJOUT: Si c'est un message de chat (contient du texte)
            if (o.has("text")) {
                try {
                    // Sauvegarder le message dans la base de données
                    int fromUserId = o.getInt("userId");
                    int toUserId = o.getInt("fileId"); // Le fileId est l'ID du destinataire pour le chat
                    String text = o.getString("text");

                    // Enregistrer le message
                    ChatMessage chatMessage = new ChatMessage(fromUserId, toUserId, text);
                    boolean saved = chatMessage.save();

                    if (saved) {
                        System.out.println("Message de chat enregistré: " + fromUserId + " -> " + toUserId);
                    } else {
                        System.err.println("Échec de l'enregistrement du message");
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'enregistrement du message: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Diffuser le message à tous les abonnés (code existant)
            subs.forEach(s -> {
                if (s.isOpen()) s.getAsyncRemote().sendText(msg);
            });
        }

        else if ("cursor".equals(type)) {
            int fid = (Integer) session.getUserProperties().get("fileId");
            Set<Session> subs = fileSessions.getOrDefault(fid, Collections.emptySet());
            subs.forEach(s -> {
                if (s.isOpen()) s.getAsyncRemote().sendText(msg);
            });
            System.out.println("broadcast cursor to " + subs.size() + " sessions");
        }
        else if ("typing".equals(type)) {
            int fid = (Integer) session.getUserProperties().get("fileId");
            Set<Session> subs = fileSessions.getOrDefault(fid, Collections.emptySet());
            subs.forEach(s -> {
                if (s.isOpen()) s.getAsyncRemote().sendText(msg);
            });
            System.out.println("broadcast typing notification to " + subs.size() + " sessions");
        }
    }

    @OnClose
    public void onClose(Session session) {
        Integer fid = (Integer) session.getUserProperties().get("fileId");
        if (fid != null) {
            fileSessions.getOrDefault(fid, Collections.emptySet()).remove(session);
        }
        System.out.println("WS CLOSE: session " + session.getId() + " (file " + fid + ")");
    }
}