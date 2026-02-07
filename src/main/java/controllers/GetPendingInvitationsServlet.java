package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import models.DBConnection;
import models.Utilisateurs;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;

@WebServlet("/GetPendingInvitationsServlet")
public class GetPendingInvitationsServlet extends HttpServlet {
    private static final String SQL =
            "SELECT i.id, i.destinataire_id AS invitee_id, u.nom AS invitee_nom, i.role, i.date_invite " +
                    "FROM invitations i " +
                    "JOIN utilisateurs u ON i.destinataire_id = u.id " +
                    "WHERE i.fichier_id = ? AND i.inviter_id = ?";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Vérification de l'authentification
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Utilisateurs user = (Utilisateurs) session.getAttribute("utilisateur");
        int userId = user.getIdUtilisateur();

        // Récupération du paramètre fichier_id
        String fileIdParam = req.getParameter("fichier_id");
        if (fileIdParam == null || fileIdParam.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Paramètre fichier_id manquant");
            return;
        }

        int fileId;
        try {
            fileId = Integer.parseInt(fileIdParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Paramètre fichier_id invalide");
            return;
        }

        JSONArray invitations = new JSONArray();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, fileId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JSONObject invitation = new JSONObject();
                    invitation.put("id", rs.getInt("id"));
                    invitation.put("invitee_id", rs.getInt("invitee_id"));
                    invitation.put("invitee_nom", rs.getString("invitee_nom"));
                    invitation.put("role", rs.getString("role"));
                    invitation.put("date_invite", rs.getTimestamp("date_invite").toString());

                    invitations.put(invitation);
                }
            }
        } catch (SQLException e) {
            getServletContext().log("Erreur SQL dans GetPendingInvitationsServlet", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur serveur");
            return;
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(invitations.toString());
    }
}