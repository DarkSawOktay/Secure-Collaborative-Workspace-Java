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

/**
 * Le servlet ChatServlet gère les fonctionnalités du chat en temps réel pour un projet donné.
 * Il utilise une base de données relationnelle pour stocker et récupérer les messages du chat.
 * Les utilisateurs doivent être authentifiés pour accéder à ses fonctionnalités.
 *
 * Fonctionnalités principales :
 *
 * - **doGet** : Cette méthode est utilisée pour récupérer tous les messages d'un projet spécifique
 *   et les retourner au format JSON, classés par ordre chronologique.
 *   L'utilisateur doit être connecté pour effectuer cette opération.
 *
 * - **doPost** : Cette méthode permet aux utilisateurs authentifiés d'ajouter un nouveau message
 *   pour un projet spécifique dans la base de données. Le message est lié à l'utilisateur
 *   et au projet correspondant.
 *
 * Variables utilisées :
 * - `SQL_SELECT` : Requête SQL permettant de récupérer les messages d'un projet, associés aux utilisateurs et triés par date.
 * - `SQL_INSERT` : Requête SQL utilisée pour insérer un nouveau message dans le chat pour un projet défini.
 *
 * Remarques :
 * - Ce servlet nécessite une connexion active (HttpSession) et un utilisateur authentifié (objet Utilisateurs).
 * - Manipule les données JSON pour la communication côté client.
 * - Utilise les classes DBConnection et Utilisateurs pour gérer la connexion à la base de données et les informations des utilisateurs.
 *
 * Exceptions :
 * - En cas d'erreur SQL ou de problème d'accès à la base de données, une ServletException est levée.
 * - Renvoie un statut HTTP 401 si l'utilisateur n'est pas authentifié.
 */
@WebServlet("/ChatServlet")
public class ChatServlet extends HttpServlet {
    private static final String SQL_SELECT =
            "SELECT c.id, c.message, c.timestamp, u.nom " +
                    "FROM project_chat c JOIN utilisateurs u ON c.user_id = u.id " +
                    "WHERE c.project_id = ? ORDER BY c.timestamp ASC";
    private static final String SQL_INSERT =
            "INSERT INTO project_chat(project_id, user_id, message) VALUES(?, ?, ?)";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Utilisateurs me = (Utilisateurs) session.getAttribute("utilisateur");
        if (me == null) { resp.sendError(401); return; }

        int projectId = Integer.parseInt(req.getParameter("project_id"));
        JSONArray arr = new JSONArray();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JSONObject o = new JSONObject();
                    o.put("id", rs.getInt("id"));
                    o.put("username", rs.getString("nom"));
                    o.put("message", rs.getString("message"));
                    o.put("timestamp", rs.getTimestamp("timestamp").toString());
                    arr.put(o);
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Erreur chargement chat", e);
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(arr.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Utilisateurs me = (Utilisateurs) session.getAttribute("utilisateur");
        if (me == null) { resp.sendError(401); return; }

        int projectId  = Integer.parseInt(req.getParameter("project_id"));
        String message = req.getParameter("message");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
            ps.setInt(1, projectId);
            ps.setInt(2, me.getIdUtilisateur());
            ps.setString(3, message);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ServletException("Erreur insertion chat", e);
        }

        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
