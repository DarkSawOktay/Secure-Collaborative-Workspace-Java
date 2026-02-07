package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import models.ChatMessage;
import models.Utilisateurs;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet permettant de gérer les messages de chat entre deux utilisateurs
 */
@WebServlet("/ChatMessageServlet")
public class ChatMessageServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ChatMessageServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Vérification de l'authentification
        HttpSession session = req.getSession(false);
        Utilisateurs me = (session != null) ? (Utilisateurs) session.getAttribute("utilisateur") : null;
        if (me == null) {
            LOGGER.warning("Tentative d'accès non authentifiée à ChatMessageServlet");
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Non connecté");
            return;
        }

        // Récupération du paramètre (ID de l'autre utilisateur)
        String withIdParam = req.getParameter("with");
        if (withIdParam == null || withIdParam.isEmpty()) {
            LOGGER.warning("Paramètre 'with' manquant");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Paramètre 'with' manquant");
            return;
        }

        int myId = me.getIdUtilisateur();
        int otherId;

        try {
            otherId = Integer.parseInt(withIdParam);
            LOGGER.info("Récupération des messages entre " + myId + " et " + otherId);
        } catch (NumberFormatException e) {
            LOGGER.warning("ID utilisateur invalide: " + withIdParam);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID utilisateur invalide");
            return;
        }

        // Récupération des messages de la conversation
        List<ChatMessage> messages = ChatMessage.getConversation(myId, otherId);
        LOGGER.info("Nombre de messages récupérés: " + messages.size());

        // Conversion en JSON
        JSONArray jsonMessages = new JSONArray();
        for (ChatMessage message : messages) {
            JSONObject jsonMsg = new JSONObject();
            jsonMsg.put("id", message.getId());
            jsonMsg.put("from", message.getExpediteurId());
            jsonMsg.put("to", message.getDestinataireId());
            jsonMsg.put("message", message.getMessage());
            jsonMsg.put("timestamp", message.getTimestamp().getTime());
            jsonMsg.put("username", message.getExpediteurNom());
            jsonMsg.put("isSent", message.getExpediteurId() == myId);

            jsonMessages.put(jsonMsg);
        }

        // Envoi de la réponse
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(jsonMessages.toString());
        LOGGER.info("Réponse JSON envoyée avec " + jsonMessages.length() + " messages");
    }
}