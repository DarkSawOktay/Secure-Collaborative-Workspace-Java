package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import models.DBConnection;
import models.Utilisateurs;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;

@WebServlet("/FindUserServlet")
public class FindUserServlet extends HttpServlet {
    private static final String SQL =
            "SELECT id, nom, email FROM utilisateurs WHERE nom = ?";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Vérification de l'authentification
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String pseudo = req.getParameter("pseudo");
        if (pseudo == null || pseudo.trim().isEmpty()) {
            sendJsonResponse(resp, false, "Pseudo non spécifié", null);
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, pseudo.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JSONObject user = new JSONObject();
                    user.put("id", rs.getInt("id"));
                    user.put("nom", rs.getString("nom"));
                    user.put("email", rs.getString("email"));

                    sendJsonResponse(resp, true, null, user);
                } else {
                    sendJsonResponse(resp, false, "Utilisateur introuvable", null);
                }
            }
        } catch (SQLException e) {
            getServletContext().log("Erreur SQL dans FindUserServlet", e);
            sendJsonResponse(resp, false, "Erreur serveur", null);
        }
    }

    private void sendJsonResponse(HttpServletResponse resp, boolean success, String message, JSONObject user)
            throws IOException {
        JSONObject json = new JSONObject();
        json.put("success", success);

        if (message != null) {
            json.put("message", message);
        }

        if (user != null) {
            json.put("user", user);
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json.toString());
    }
}