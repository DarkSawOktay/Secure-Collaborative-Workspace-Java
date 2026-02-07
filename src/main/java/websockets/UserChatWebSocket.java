package websockets;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import models.ChatMessage;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Point de terminaison WebSocket pour le chat entre utilisateurs
 */
@ServerEndpoint(
        value="/user-messages-ws",
        configurator=UserChatWebSocket.Config.class
)
public class UserChatWebSocket {
    private static final Logger LOGGER = Logger.getLogger(UserChatWebSocket.class.getName());

    // Map pour stocker les sessions actives (userId -> session WebSocket)
    private static final Map<Integer, Session> activeUsers = new ConcurrentHashMap<>();

    // Variables d'instance
    private int userId;
    private String username;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        LOGGER.info("Ouverture d'une nouvelle connexion WebSocket pour le chat utilisateur");

        try {
            // Récupérer l'objet HttpSession depuis le handshake
            HandshakeRequest request = (HandshakeRequest) config.getUserProperties().get("handshake");
            if (request == null || request.getHttpSession() == null) {
                LOGGER.warning("Handshake request ou HTTP session est null");
                session.close(new CloseReason(
                        CloseReason.CloseCodes.VIOLATED_POLICY,
                        "Non authentifié"
                ));
                return;
            }

            HttpSession httpSession = (HttpSession) request.getHttpSession();
            models.Utilisateurs user = (models.Utilisateurs) httpSession.getAttribute("utilisateur");

            if (user == null) {
                LOGGER.warning("Utilisateur non authentifié");
                session.close(new CloseReason(
                        CloseReason.CloseCodes.VIOLATED_POLICY,
                        "Non authentifié"
                ));
                return;
            }

            // Stocker les informations de l'utilisateur
            this.userId = user.getIdUtilisateur();
            this.username = user.getNomUtilisateur();

            // Enregistrer cette session dans la map des sessions actives
            activeUsers.put(userId, session);

            // Stocker les informations dans les propriétés de la session
            session.getUserProperties().put("userId", userId);
            session.getUserProperties().put("username", username);

            // DÉBOGAGE: Afficher la liste des utilisateurs connectés
            LOGGER.info("== UTILISATEURS CONNECTÉS: " + activeUsers.size() + " ==");
            for (Map.Entry<Integer, Session> entry : activeUsers.entrySet()) {
                LOGGER.info("User ID: " + entry.getKey() + " | Session active: " + entry.getValue().isOpen());
            }

            LOGGER.info("WebSocket connecté pour l'utilisateur " + username + " (ID: " + userId + ")");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ouverture de la WebSocket", e);
            try {
                session.close(new CloseReason(
                        CloseReason.CloseCodes.UNEXPECTED_CONDITION,
                        "Erreur interne"
                ));
            } catch (IOException ioe) {
                LOGGER.log(Level.SEVERE, "Erreur lors de la fermeture de la session après erreur", ioe);
            }
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        LOGGER.info("Message reçu: " + message);

        try {
            JSONObject json = new JSONObject(message);
            String type = json.optString("type", "");

            // Ajouter avant le if "message".equals(type)
            if ("change".equals(type) && json.has("text")) {
                // Format utilisé par WebSocketLinksUpdater
                int fromUserId = json.getInt("userId");
                int toUserId = json.getInt("fileId");
                String text = json.getString("text");
                String username = json.optString("username", "Utilisateur");

                LOGGER.info("Message de chat (format change) de l'utilisateur " + fromUserId + " à l'utilisateur " + toUserId + ": " + text);

                // Sauvegarder le message dans la base de données
                ChatMessage chatMessage = new ChatMessage(fromUserId, toUserId, text);
                boolean saved = chatMessage.save();

                if (!saved) {
                    LOGGER.warning("Échec de l'enregistrement du message en base de données");
                    sendErrorToSender(session, "Échec de l'enregistrement du message");
                    return;
                }

                // Créer la réponse JSON (garder le même format)
                JSONObject response = new JSONObject(json.toString());
                response.put("id", chatMessage.getId());
                response.put("timestamp", chatMessage.getTimestamp().getTime());

                // Envoyer le message à l'expéditeur pour confirmation
                sendToSession(session, response.toString());

                // Envoyer le message au destinataire s'il est connecté
                Session recipientSession = activeUsers.get(toUserId);

                if (recipientSession != null && recipientSession.isOpen()) {
                    LOGGER.info("Le destinataire (ID: " + toUserId + ") est connecté, envoi du message");
                    sendToSession(recipientSession, response.toString());
                } else {
                    LOGGER.warning("Le destinataire (ID: " + toUserId + ") n'est pas connecté ou sa session est fermée");
                    LOGGER.info("État actuel des utilisateurs connectés: " + activeUsers.keySet());
                }
            }

            if ("message".equals(type)) {
                // Message de chat standard
                int to = json.getInt("to");
                String text = json.getString("text");

                // Vérifier que l'utilisateur est authentifié
                Integer userId = (Integer) session.getUserProperties().get("userId");
                String username = (String) session.getUserProperties().get("username");

                if (userId == null) {
                    LOGGER.warning("Message reçu d'un utilisateur non authentifié");
                    return;
                }

                LOGGER.info("Message de l'utilisateur " + userId + " à l'utilisateur " + to + ": " + text);

                // Sauvegarder le message dans la base de données
                ChatMessage chatMessage = new ChatMessage(userId, to, text);
                boolean saved = chatMessage.save();

                if (!saved) {
                    LOGGER.warning("Échec de l'enregistrement du message en base de données");
                    sendErrorToSender(session, "Échec de l'enregistrement du message");
                    return;
                }

                // Créer la réponse JSON
                JSONObject response = new JSONObject();
                response.put("type", "message");
                response.put("id", chatMessage.getId());
                response.put("from", userId);
                response.put("username", username);
                response.put("text", text);
                response.put("timestamp", chatMessage.getTimestamp().getTime());

                // CRITICAL FIX: Envoyer le message à l'expéditeur pour confirmation
                LOGGER.info("Envoi de la confirmation à l'expéditeur (ID: " + userId + ")");
                sendToSession(session, response.toString());

                // CRITICAL FIX: Envoyer le message au destinataire s'il est connecté
                Session recipientSession = activeUsers.get(to);

                if (recipientSession != null && recipientSession.isOpen()) {
                    LOGGER.info("Le destinataire (ID: " + to + ") est connecté, envoi du message");
                    // DÉBOGAGE: Pour s'assurer que le message part bien
                    LOGGER.info("Message envoyé au destinataire: " + response.toString());
                    sendToSession(recipientSession, response.toString());
                } else {
                    LOGGER.warning("Le destinataire (ID: " + to + ") n'est pas connecté ou sa session est fermée");

                    // Afficher l'état de tous les utilisateurs pour déboguer
                    LOGGER.info("État actuel des utilisateurs connectés:");
                    for (Map.Entry<Integer, Session> entry : activeUsers.entrySet()) {
                        LOGGER.info("User ID: " + entry.getKey() + " | Session active: " + entry.getValue().isOpen());
                    }
                }
            }
            else if ("typing".equals(type)) {
                // Notification de frappe
                int to = json.getInt("to");
                boolean isTyping = json.getBoolean("isTyping");

                // Récupérer l'ID et le nom de l'utilisateur
                Integer userId = (Integer) session.getUserProperties().get("userId");
                String username = (String) session.getUserProperties().get("username");

                if (userId == null) {
                    LOGGER.warning("Notification de frappe reçue d'un utilisateur non authentifié");
                    return;
                }

                // Créer la notification JSON
                JSONObject notification = new JSONObject();
                notification.put("type", "typing");
                notification.put("from", userId);
                notification.put("username", username);
                notification.put("isTyping", isTyping);

                // Envoyer la notification uniquement au destinataire
                Session recipientSession = activeUsers.get(to);
                if (recipientSession != null && recipientSession.isOpen()) {
                    sendToSession(recipientSession, notification.toString());
                    LOGGER.info("Notification de frappe envoyée à l'utilisateur " + to);
                } else {
                    LOGGER.fine("Le destinataire (ID: " + to + ") n'est pas connecté pour la notification de frappe");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du traitement du message", e);
            sendErrorToSender(session, "Erreur de traitement du message: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        LOGGER.info("Fermeture de la connexion WebSocket: " + reason);

        // Récupérer l'ID de l'utilisateur depuis les propriétés de la session
        Integer userId = (Integer) session.getUserProperties().get("userId");

        if (userId != null) {
            // Supprimer la session de la map des sessions actives
            activeUsers.remove(userId);
            LOGGER.info("Session WebSocket fermée pour l'utilisateur " + userId);

            // Afficher la liste des utilisateurs connectés pour déboguer
            LOGGER.info("== UTILISATEURS CONNECTÉS APRÈS FERMETURE: " + activeUsers.size() + " ==");
            for (Map.Entry<Integer, Session> entry : activeUsers.entrySet()) {
                LOGGER.info("User ID: " + entry.getKey() + " | Session active: " + entry.getValue().isOpen());
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOGGER.log(Level.SEVERE, "Erreur WebSocket", throwable);

        try {
            session.close(new CloseReason(
                    CloseReason.CloseCodes.UNEXPECTED_CONDITION,
                    "Erreur interne: " + throwable.getMessage()
            ));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la fermeture de la session après erreur", e);
        }
    }

    /**
     * Envoie un message à une session spécifique
     */
    private void sendToSession(Session session, String message) {
        try {
            if (session.isOpen()) {
                // CRITICAL FIX: Utilisation de getBasicRemote au lieu de getAsyncRemote
                session.getBasicRemote().sendText(message);
                LOGGER.info("Message envoyé avec succès à la session");
            } else {
                LOGGER.warning("Tentative d'envoi de message à une session fermée");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Erreur lors de l'envoi du message à la session", e);
        }
    }

    /**
     * Envoie un message d'erreur à l'expéditeur
     */
    private void sendErrorToSender(Session session, String errorMessage) {
        try {
            JSONObject error = new JSONObject();
            error.put("type", "error");
            error.put("message", errorMessage);

            sendToSession(session, error.toString());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'envoi du message d'erreur", e);
        }
    }

    /**
     * Classe de configuration pour le Endpoint
     */
    public static class Config extends ServerEndpointConfig.Configurator {
        @Override
        public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
            config.getUserProperties().put("handshake", request);
        }
    }
}