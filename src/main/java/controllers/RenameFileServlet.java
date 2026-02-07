package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import models.DBConnection;
import models.Utilisateurs;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;

@WebServlet("/RenameFileServlet")
public class RenameFileServlet extends HttpServlet {
    private static final String UPDATE_SQL =
            "UPDATE fichiers SET nom = ? WHERE id = ?";
    private static final String CHECK_SQL =
            "SELECT COUNT(*) FROM droits_acces WHERE fichier_id = ? AND utilisateur_id = ? AND peut_modifier = TRUE";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1) Authentification
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Utilisateurs user = (Utilisateurs) session.getAttribute("utilisateur");
        int userId = user.getIdUtilisateur();

        // 2) Récupération paramètres
        int fileId = Integer.parseInt(request.getParameter("fileId"));
        String newName = request.getParameter("newName");

        // 3) Vérification des droits
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement check = conn.prepareStatement(CHECK_SQL)) {
            check.setInt(1, fileId);
            check.setInt(2, userId);
            ResultSet rs = check.executeQuery();
            rs.next();
            if (rs.getInt(1) == 0) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        } catch (SQLException e) {
            throw new ServletException("Erreur vérification droits", e);
        }

        // 4) Renommage
        boolean success;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, newName);
            ps.setInt(2, fileId);
            success = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ServletException("Erreur renommage", e);
        }

        JSONObject json = new JSONObject();
        json.put("success", success);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json.toString());
    }
}
