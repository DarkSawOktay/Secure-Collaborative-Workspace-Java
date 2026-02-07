package websockets;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.json.JSONObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;

@ServerEndpoint(
        value="/chat-websocket",
        configurator=ChatEndpoint.Config.class
)
public class ChatEndpoint {
    private static final Logger LOGGER = Logger.getLogger(ChatEndpoint.class.getName());
    private static final Map<Integer, Session> sessions = new ConcurrentHashMap<>();
    private int userId, peerId;
    private String username;

    @OnOpen
    public void open(Session sess, EndpointConfig cfg) {
        LOGGER.info("WebSocket opening connection...");

        try {
            // Récupérer les informations de session et d'utilisateur
            HandshakeRequest req = (HandshakeRequest) cfg.getUserProperties().get("handshake");
            if (req == null || req.getHttpSession() == null) {
                LOGGER.warning("Handshake request or HTTP session is null!");
                return;
            }

            HttpSession http = (HttpSession) req.getHttpSession();
            models.Utilisateurs me = (models.Utilisateurs) http.getAttribute("utilisateur");
            if (me == null) {
                LOGGER.warning("User is not authenticated!");
                return;
            }

            userId = me.getIdUtilisateur();
            username = me.getNomUtilisateur();

            // Récupérer l'ID du destinataire
            String queryString = req.getQueryString();
            LOGGER.info("WebSocket query string: " + queryString);

            if (queryString != null) {
                for (String param : queryString.split("&")) {
                    if (param.startsWith("to=") || param.startsWith("with=")) {
                        peerId = Integer.parseInt(param.split("=")[1]);
                        break;
                    }
                }
            }

            LOGGER.info("WebSocket opened: User ID = " + userId + ", Username = " + username + ", Peer ID = " + peerId);
            sessions.put(userId, sess);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during WebSocket opening", e);
        }
    }

    @OnMessage
    public void onMsg(String msgJson) {
        LOGGER.info("Message received from user " + userId + ": " + msgJson);

        try {
            JSONObject in = new JSONObject(msgJson);

            // Traitement des notifications de frappe
            if (in.has("typing")) {
                boolean isTyping = in.getBoolean("typing");
                LOGGER.info("Typing event from user " + userId + ": " + isTyping);

                JSONObject out = new JSONObject();
                out.put("from", userId);
                out.put("typing", isTyping);

                // Si un nom d'utilisateur est fourni dans le message, l'utiliser
                // Sinon, utiliser le nom stocké lors de l'ouverture de la connexion
                String displayName = in.optString("username", username);
                out.put("username", displayName);

                // Envoyer au pair s'il est connecté
                Session peer = sessions.get(peerId);
                if (peer != null && peer.isOpen()) {
                    LOGGER.info("Sending typing notification to user " + peerId);
                    peer.getBasicRemote().sendText(out.toString());
                } else {
                    LOGGER.warning("Peer not found or session closed: " + peerId);
                }
                return;
            }

            // Traitement des messages de chat
            if (in.has("text")) {
                String text = in.getString("text");

                // Créer le message à envoyer
                JSONObject out = new JSONObject();
                out.put("from", userId);
                out.put("text", text);
                out.put("username", username);
                out.put("timestamp", System.currentTimeMillis());

                // Envoyer à soi-même pour confirmation
                Session self = sessions.get(userId);
                if (self != null && self.isOpen()) {
                    self.getBasicRemote().sendText(out.toString());
                }

                // Envoyer au pair s'il est connecté
                Session peer = sessions.get(peerId);
                if (peer != null && peer.isOpen()) {
                    peer.getBasicRemote().sendText(out.toString());
                } else {
                    LOGGER.warning("Peer not found or session closed when sending message: " + peerId);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing message", e);
        }
    }

    @OnClose
    public void close() {
        LOGGER.info("WebSocket closed for user ID: " + userId);
        sessions.remove(userId);
    }

    @OnError
    public void onError(Session session, Throwable t) {
        LOGGER.log(Level.SEVERE, "WebSocket error: " + t.getMessage(), t);
    }

    public static class Config extends ServerEndpointConfig.Configurator {
        @Override
        public void modifyHandshake(ServerEndpointConfig sec,
                                    HandshakeRequest req, HandshakeResponse res) {
            sec.getUserProperties().put("handshake", req);
        }
    }
}