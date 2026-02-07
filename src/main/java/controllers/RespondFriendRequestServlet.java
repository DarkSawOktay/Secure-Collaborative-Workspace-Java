package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import models.DBConnection;
import models.Utilisateurs;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;

@WebServlet("/RespondFriendRequestServlet")
public class RespondFriendRequestServlet extends HttpServlet {
    private static final String GET_REQUEST_DETAILS =
            "SELECT expediteur_id, destinataire_id FROM demande_ami WHERE id = ? AND statut = 'EN_ATTENTE'";

    private static final String UPDATE_REQUEST_STATUS =
            "UPDATE demande_ami SET statut = ?, date_reponse = NOW() WHERE id = ?";

    private static final String INSERT_FRIENDSHIP =
            "INSERT INTO amis(user_id_1, user_id_2, date_creation) VALUES(?, ?, NOW())";

    private static final String INSERT_NOTIFICATION =
            "INSERT INTO notifications(utilisateur_id, message, type, lu, date_creation) VALUES(?, ?, ?, false, NOW())";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        JSONObject json = new JSONObject();

        // Vérification de l'authentification
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            json.put("success", false).put("message", "Non authentifié");
            resp.getWriter().write(json.toString());
            return;
        }
        Utilisateurs me = (Utilisateurs) session.getAttribute("utilisateur");
        int myId = me.getIdUtilisateur();

        // Récupération des paramètres
        String requestIdStr = req.getParameter("requestId");
        String action = req.getParameter("action"); // "accept" ou "reject"

        if (requestIdStr == null || action == null ||
                (!action.equals("accept") && !action.equals("reject"))) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            json.put("success", false).put("message", "Paramètres invalides");
            resp.getWriter().write(json.toString());
            return;
        }

        int requestId;
        try {
            requestId = Integer.parseInt(requestIdStr);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            json.put("success", false).put("message", "ID de requête invalide");
            resp.getWriter().write(json.toString());
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1) Récupérer les détails de la demande
                int expediteurId;
                try (PreparedStatement ps = conn.prepareStatement(GET_REQUEST_DETAILS)) {
                    ps.setInt(1, requestId);
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        json.put("success", false)
                                .put("message", "Demande d'ami non trouvée ou déjà traitée");
                        resp.getWriter().write(json.toString());
                        return;
                    }

                    expediteurId = rs.getInt("expediteur_id");
                    int destinataireId = rs.getInt("destinataire_id");

                    // Vérifier que l'utilisateur est bien le destinataire
                    if (destinataireId != myId) {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        json.put("success", false)
                                .put("message", "Vous n'êtes pas autorisé à répondre à cette demande");
                        resp.getWriter().write(json.toString());
                        return;
                    }
                }

                // 2) Mettre à jour le statut de la demande
                String newStatus = action.equals("accept") ? "ACCEPTEE" : "REFUSEE";
                try (PreparedStatement ps = conn.prepareStatement(UPDATE_REQUEST_STATUS)) {
                    ps.setString(1, newStatus);
                    ps.setInt(2, requestId);
                    ps.executeUpdate();
                }

                // 3) Si acceptée, créer la relation d'amitié
                if (action.equals("accept")) {
                    try (PreparedStatement ps = conn.prepareStatement(INSERT_FRIENDSHIP)) {
                        ps.setInt(1, Math.min(myId, expediteurId));
                        ps.setInt(2, Math.max(myId, expediteurId));
                        ps.executeUpdate();
                    }
                }

                // 4) Envoyer une notification à l'expéditeur
                String notificationType = action.equals("accept") ? "DEMANDE_ACCEPTEE" : "DEMANDE_REFUSEE";
                String notificationMessage = action.equals("accept")
                        ? me.getNomUtilisateur() + " a accepté votre demande d'ami"
                        : me.getNomUtilisateur() + " a refusé votre demande d'ami";

                try (PreparedStatement ps = conn.prepareStatement(INSERT_NOTIFICATION)) {
                    ps.setInt(1, expediteurId);
                    ps.setString(2, notificationMessage);
                    ps.setString(3, notificationType);
                    ps.executeUpdate();
                }

                // 5) Valider la transaction
                conn.commit();

                // Journal de l'action
                String actionLog = action.equals("accept")
                        ? "Utilisateur " + myId + " a accepté la demande d'ami de " + expediteurId
                        : "Utilisateur " + myId + " a refusé la demande d'ami de " + expediteurId;
                getServletContext().log(actionLog);

                // 6) Renvoyer la réponse
                json.put("success", true)
                        .put("message", action.equals("accept")
                                ? "Demande d'ami acceptée avec succès"
                                : "Demande d'ami refusée");

                resp.getWriter().write(json.toString());

            } catch (SQLException e) {
                conn.rollback();
                getServletContext().log("Erreur SQL dans RespondFriendRequestServlet", e);

                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                json.put("success", false)
                        .put("message", "Erreur lors du traitement de la demande d'ami");
                resp.getWriter().write(json.toString());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServletException("Erreur de connexion à la BD", e);
        }
    }
}