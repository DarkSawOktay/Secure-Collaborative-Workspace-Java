package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import models.DBConnection;
import models.Utilisateurs;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;

@WebServlet("/CancelInvitationServlet")
public class CancelInvitationServlet extends HttpServlet {
    private static final String CHECK_SQL =
            "SELECT fichier_id, inviter_id FROM invitations WHERE id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM invitations WHERE id = ?";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Vérification de l'authentification
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Utilisateurs user = (Utilisateurs) session.getAttribute("utilisateur");
        int userId = user.getIdUtilisateur();

        // Récupération du paramètre invitation_id
        String invitationIdParam = req.getParameter("invitation_id");
        if (invitationIdParam == null || invitationIdParam.trim().isEmpty()) {
            sendJsonResponse(resp, false, "Paramètre invitation_id manquant");
            return;
        }

        int invitationId;
        try {
            invitationId = Integer.parseInt(invitationIdParam);
        } catch (NumberFormatException e) {
            sendJsonResponse(resp, false, "Paramètre invitation_id invalide");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Vérifier que l'utilisateur est bien l'inviteur
            boolean isInviter = false;

            try (PreparedStatement ps = conn.prepareStatement(CHECK_SQL)) {
                ps.setInt(1, invitationId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        isInviter = (rs.getInt("inviter_id") == userId);
                    }
                }
            }

            if (!isInviter) {
                sendJsonResponse(resp, false, "Vous n'êtes pas autorisé à annuler cette invitation");
                return;
            }

            // Supprimer l'invitation
            try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
                ps.setInt(1, invitationId);
                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    sendJsonResponse(resp, true, "Invitation annulée avec succès");
                } else {
                    sendJsonResponse(resp, false, "Invitation introuvable");
                }
            }
        } catch (SQLException e) {
            getServletContext().log("Erreur SQL dans CancelInvitationServlet", e);
            sendJsonResponse(resp, false, "Erreur serveur");
        }
    }

    private void sendJsonResponse(HttpServletResponse resp, boolean success, String message)
            throws IOException {
        JSONObject json = new JSONObject();
        json.put("success", success);
        json.put("message", message);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json.toString());
    }
}