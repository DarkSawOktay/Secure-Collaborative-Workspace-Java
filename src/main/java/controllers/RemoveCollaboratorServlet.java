package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import models.DBConnection;
import models.Utilisateurs;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;

@WebServlet("/RemoveCollaboratorServlet")
public class RemoveCollaboratorServlet extends HttpServlet {
    private static final String SQL =
            "DELETE FROM droits_acces WHERE fichier_id = ? AND utilisateur_id = ?";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int fid = Integer.parseInt(req.getParameter("fichier_id"));
        int targetUserId = Integer.parseInt(req.getParameter("utilisateur_id"));

        // Récupérer l'utilisateur actuel
        HttpSession session = req.getSession(false);
        Utilisateurs me = (session != null)
                ? (Utilisateurs) session.getAttribute("utilisateur")
                : null;
        if (me == null) {
            sendJsonResponse(resp, false, "Non autorisé");
            return;
        }
        int userId = me.getIdUtilisateur();

        try (Connection conn = DBConnection.getConnection()) {
            // Vérifier que l'utilisateur est le propriétaire ou admin
            boolean isAuthorized = false;

            // 1. Vérifier si propriétaire
            String checkOwner = "SELECT proprietaire_id FROM fichiers WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkOwner)) {
                ps.setInt(1, fid);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt("proprietaire_id") == userId) {
                    isAuthorized = true;
                }
            }

            // 2. Si pas propriétaire, vérifier si admin
            if (!isAuthorized) {
                String checkAdmin = "SELECT 1 FROM droits_acces WHERE fichier_id = ? AND utilisateur_id = ? AND role = 'admin'";
                try (PreparedStatement ps = conn.prepareStatement(checkAdmin)) {
                    ps.setInt(1, fid);
                    ps.setInt(2, userId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        isAuthorized = true;
                    }
                }
            }

            // 3. Si pas autorisé, renvoyer une erreur
            if (!isAuthorized) {
                sendJsonResponse(resp, false, "Vous n'avez pas les droits pour supprimer des collaborateurs");
                return;
            }

            // Si autorisé, supprimer le collaborateur
            try (PreparedStatement ps = conn.prepareStatement(SQL)) {
                ps.setInt(1, fid);
                ps.setInt(2, targetUserId);
                ps.executeUpdate();
            }

            sendJsonResponse(resp, true, "Collaborateur supprimé avec succès");
        } catch (SQLException e) {
            getServletContext().log("Erreur SQL dans RemoveCollaboratorServlet", e);
            sendJsonResponse(resp, false, "Erreur serveur: " + e.getMessage());
        }
    }

    private void sendJsonResponse(HttpServletResponse resp, boolean success, String message) throws IOException {
        JSONObject json = new JSONObject();
        json.put("success", success);
        json.put("message", message);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json.toString());
    }
}