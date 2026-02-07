package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import models.DBConnection;
import models.Utilisateurs;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;

@WebServlet("/DeleteFolderServlet")
public class DeleteFolderServlet extends HttpServlet {
    private static final String SQL =
            "DELETE FROM dossiers WHERE id = ? AND proprietaire_id = ?";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Utilisateurs user = (Utilisateurs) session.getAttribute("utilisateur");
        int userId = user.getIdUtilisateur();
        int folderId = Integer.parseInt(req.getParameter("folderId"));
        String newName = req.getParameter("newName");

        boolean success;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, folderId);
            ps.setInt(2, userId);
            success = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ServletException("Erreur suppression dossier", e);
        }

        JSONObject json = new JSONObject();
        json.put("success", success);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json.toString());
    }
}
