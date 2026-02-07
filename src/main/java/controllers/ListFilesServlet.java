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

@WebServlet("/ListFilesServlet")
public class ListFilesServlet extends HttpServlet {
    private static final String SQL =
            "SELECT id, nom FROM fichiers WHERE proprietaire_id = ?";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Utilisateurs user = (Utilisateurs) session.getAttribute("utilisateur");
        int userId = user.getIdUtilisateur();

        JSONArray array = new JSONArray();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", rs.getInt("id"));
                    obj.put("nom", rs.getString("nom"));
                    array.put(obj);
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Erreur liste fichiers", e);
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(array.toString());
    }
}