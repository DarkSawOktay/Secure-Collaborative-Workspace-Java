package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import models.DBConnection;
import models.Utilisateurs;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;

@WebServlet("/CreateInvitationServlet")
public class CreateInvitationServlet extends HttpServlet {
    private static final String SQL =
            "INSERT INTO invitations(fichier_id, inviter_id, destinataire_id, role) VALUES(?, ?, ?, ?)";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Utilisateurs me = (session != null)
                ? (Utilisateurs) session.getAttribute("utilisateur")
                : null;
        if (me == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        int inviterId = me.getIdUtilisateur();
        int fichierId = Integer.parseInt(req.getParameter("fichier_id"));
        int inviteeId = Integer.parseInt(req.getParameter("invitee_id"));
        String role = req.getParameter("role"); // "viewer", "editor" ou "admin"

        JSONObject json = new JSONObject();
        try (Connection conn = DBConnection.getConnection()) {
            // Vérifier que l'utilisateur est le propriétaire ou admin
            boolean isAuthorized = false;

            // 1. Vérifier si propriétaire
            String checkOwner = "SELECT proprietaire_id FROM fichiers WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkOwner)) {
                ps.setInt(1, fichierId);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt("proprietaire_id") == inviterId) {
                    isAuthorized = true;
                }
            }

            // 2. Si pas propriétaire, vérifier si admin
            if (!isAuthorized) {
                String checkAdmin = "SELECT 1 FROM droits_acces WHERE fichier_id = ? AND utilisateur_id = ? AND role = 'admin'";
                try (PreparedStatement ps = conn.prepareStatement(checkAdmin)) {
                    ps.setInt(1, fichierId);
                    ps.setInt(2, inviterId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        isAuthorized = true;
                    }
                }
            }

            // 3. Si pas autorisé, renvoyer une erreur
            if (!isAuthorized) {
                json.put("success", false)
                        .put("message", "Vous n'avez pas les droits pour inviter des collaborateurs");
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(json.toString());
                return;
            }

            // Si autorisé, créer l'invitation
            try (PreparedStatement ps = conn.prepareStatement(SQL)) {
                ps.setInt(1, fichierId);
                ps.setInt(2, inviterId);
                ps.setInt(3, inviteeId);
                ps.setString(4, role);
                ps.executeUpdate();

                json.put("success", true);
            } catch (SQLException e) {
                json.put("success", false)
                        .put("message", "Impossible d'envoyer l'invitation: " + e.getMessage());
            }

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(json.toString());
        } catch (SQLException e) {
            json.put("success", false)
                    .put("message", "Erreur serveur: " + e.getMessage());
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(json.toString());
        }
    }
}