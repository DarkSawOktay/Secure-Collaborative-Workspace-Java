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
import java.util.*;

@WebServlet("/ListProjectsServlet")
public class ListProjectsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        Utilisateurs me = session != null
                ? (Utilisateurs) session.getAttribute("utilisateur")
                : null;
        if (me == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        int uid = me.getIdUtilisateur();

        // Requête pour tous les dossiers RACINE (parent_id IS NULL) uniquement
        String sqlProjects =
                "SELECT DISTINCT d.id, d.nom, d.proprietaire_id = ? as owner " +
                        "FROM dossiers d " +
                        "LEFT JOIN fichiers f ON f.dossier_id = d.id " +
                        "LEFT JOIN droits_acces da ON da.fichier_id = f.id " +
                        "WHERE (d.proprietaire_id = ? OR da.utilisateur_id = ?) " +
                        "AND d.parent_id IS NULL"; // Assure que ce sont des dossiers racine

        JSONArray projects = new JSONArray();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlProjects)) {

            ps.setInt(1, uid);
            ps.setInt(2, uid);
            ps.setInt(3, uid);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JSONObject project = new JSONObject();
                    project.put("id", rs.getInt("id"))
                            .put("nom", rs.getString("nom"))
                            .put("owner", rs.getBoolean("owner"));
                    projects.put(project);
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Erreur lors du chargement des projets", e);
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(projects.toString());
    }
}