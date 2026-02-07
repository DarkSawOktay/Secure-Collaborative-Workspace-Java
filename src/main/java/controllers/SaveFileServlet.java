package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import models.DBConnection;
import models.Utilisateurs;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;

@WebServlet("/SaveFileServlet")
public class SaveFileServlet extends HttpServlet {
    private static final String UPDATE_SQL =
            "UPDATE fichiers SET contenu = ? WHERE id = ?";
    private static final String CHECK_SQL =
            "SELECT COUNT(*) FROM droits_acces WHERE fichier_id = ? AND utilisateur_id = ? AND peut_modifier = TRUE";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1) Vérification session/utilisateur
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Utilisateurs user = (Utilisateurs) session.getAttribute("utilisateur");
        int userId = user.getIdUtilisateur();

        // 2) Récupération des paramètres
        int fileId = Integer.parseInt(request.getParameter("fileId"));
        String content = request.getParameter("content");

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

        // 4) Sauvegarde du contenu
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, content);
            ps.setInt(2, fileId);
            int updated = ps.executeUpdate();

            JSONObject json = new JSONObject();
            json.put("success", updated > 0);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json.toString());

        } catch (SQLException e) {
            throw new ServletException("Erreur en sauvegardant le fichier", e);
        }
    }
}
