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

@WebServlet("/GetFriendRequestsServlet")
public class GetFriendRequestsServlet extends HttpServlet {
    private static final String GET_INCOMING_REQUESTS =
            "SELECT d.id, d.expediteur_id, d.date_demande, u.nom, u.email " +
                    "FROM demande_ami d " +
                    "JOIN utilisateurs u ON d.expediteur_id = u.id " +
                    "WHERE d.destinataire_id = ? AND d.statut = 'EN_ATTENTE' " +
                    "ORDER BY d.date_demande DESC";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
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

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(GET_INCOMING_REQUESTS)) {
            ps.setInt(1, myId);
            ResultSet rs = ps.executeQuery();

            JSONArray requests = new JSONArray();
            while (rs.next()) {
                JSONObject request = new JSONObject();
                request.put("requestId", rs.getInt("id"));
                request.put("expediteurId", rs.getInt("expediteur_id"));
                request.put("expediteurNom", rs.getString("nom"));
                request.put("expediteurEmail", rs.getString("email"));
                request.put("dateDemande", rs.getTimestamp("date_demande").toString());
                requests.put(request);
            }

            json.put("success", true);
            json.put("requests", requests);
            resp.getWriter().write(json.toString());

        } catch (SQLException e) {
            getServletContext().log("Erreur SQL dans GetFriendRequestsServlet", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json.put("success", false)
                    .put("message", "Erreur lors de la récupération des demandes d'ami");
            resp.getWriter().write(json.toString());
        }
    }
}