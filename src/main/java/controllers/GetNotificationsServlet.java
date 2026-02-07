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

@WebServlet("/GetNotificationsServlet")
public class GetNotificationsServlet extends HttpServlet {
    private static final String GET_NOTIFICATIONS =
            "SELECT id, message, type, lu, date_creation " +
                    "FROM notifications " +
                    "WHERE utilisateur_id = ? " +
                    "ORDER BY date_creation DESC " +
                    "LIMIT ?";

    private static final String MARK_AS_READ =
            "UPDATE notifications SET lu = true WHERE id = ?";

    private static final String COUNT_UNREAD =
            "SELECT COUNT(*) as total FROM notifications WHERE utilisateur_id = ? AND lu = false";

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

        // Paramètre limit
        int limit = 10; // Par défaut
        String limitStr = req.getParameter("limit");
        if (limitStr != null && !limitStr.isBlank()) {
            try {
                limit = Integer.parseInt(limitStr);
                limit = Math.max(1, Math.min(50, limit)); // Limiter entre 1 et 50
            } catch (NumberFormatException e) {
                // Garder la valeur par défaut
            }
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Récupérer le nombre de notifications non lues
            int unreadCount = 0;
            try (PreparedStatement ps = conn.prepareStatement(COUNT_UNREAD)) {
                ps.setInt(1, myId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    unreadCount = rs.getInt("total");
                }
            }

            // Récupérer les notifications
            JSONArray notifications = new JSONArray();
            try (PreparedStatement ps = conn.prepareStatement(GET_NOTIFICATIONS)) {
                ps.setInt(1, myId);
                ps.setInt(2, limit);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    JSONObject notification = new JSONObject();
                    notification.put("id", rs.getInt("id"));
                    notification.put("message", rs.getString("message"));
                    notification.put("type", rs.getString("type"));
                    notification.put("lu", rs.getBoolean("lu"));
                    notification.put("dateCreation", rs.getTimestamp("date_creation").toString());
                    notifications.put(notification);
                }
            }

            json.put("success", true);
            json.put("notifications", notifications);
            json.put("unreadCount", unreadCount);
            resp.getWriter().write(json.toString());

        } catch (SQLException e) {
            getServletContext().log("Erreur SQL dans GetNotificationsServlet", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json.put("success", false)
                    .put("message", "Erreur lors de la récupération des notifications");
            resp.getWriter().write(json.toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
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

        // Récupération de l'ID de la notification à marquer comme lue
        String notificationIdStr = req.getParameter("notificationId");
        if (notificationIdStr == null || notificationIdStr.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            json.put("success", false).put("message", "ID de notification non spécifié");
            resp.getWriter().write(json.toString());
            return;
        }

        int notificationId;
        try {
            notificationId = Integer.parseInt(notificationIdStr);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            json.put("success", false).put("message", "ID de notification invalide");
            resp.getWriter().write(json.toString());
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(MARK_AS_READ)) {
            ps.setInt(1, notificationId);
            int updated = ps.executeUpdate();

            if (updated > 0) {
                json.put("success", true)
                        .put("message", "Notification marquée comme lue");
            } else {
                json.put("success", false)
                        .put("message", "Notification non trouvée");
            }

            resp.getWriter().write(json.toString());

        } catch (SQLException e) {
            getServletContext().log("Erreur SQL dans GetNotificationsServlet (marquage comme lu)", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json.put("success", false)
                    .put("message", "Erreur lors du marquage de la notification comme lue");
            resp.getWriter().write(json.toString());
        }
    }
}