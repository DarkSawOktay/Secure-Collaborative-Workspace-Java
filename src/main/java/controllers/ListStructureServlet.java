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

@WebServlet("/ListStructureServlet")
public class ListStructureServlet extends HttpServlet {
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

        // Requête pour tous les dossiers (owner ou via droits_acces)
        String sqlFolders =
                "SELECT DISTINCT d.id, d.nom, d.parent_id " +
                        "FROM dossiers d " +
                        "LEFT JOIN fichiers f ON f.dossier_id = d.id " +
                        "LEFT JOIN droits_acces da ON da.fichier_id = f.id " +
                        "WHERE d.proprietaire_id = ? OR da.utilisateur_id = ?";

        // Requête pour tous les fichiers accessibles
        String sqlFiles =
                "SELECT f.id, f.nom, f.dossier_id " +
                        "FROM fichiers f " +
                        "JOIN droits_acces da ON da.fichier_id = f.id " +
                        "WHERE da.utilisateur_id = ?";

        // Classe interne représentant un nœud
        class Node {
            int id;
            String nom;
            boolean isFolder;
            Integer parentId; // Ajout d'un champ parentId
            List<Node> children = new ArrayList<>();

            Node(int id, String nom, boolean isFolder, Integer parentId) {
                this.id = id;
                this.nom = nom;
                this.isFolder = isFolder;
                this.parentId = parentId;
            }

            JSONObject toJSON() {
                JSONObject o = new JSONObject();
                o.put("id", id)
                        .put("nom", nom)
                        .put("type", isFolder ? "folder" : "file")
                        .put("parent_id", parentId); // Ajouter parent_id au JSON

                if (isFolder) {
                    JSONArray arr = new JSONArray();
                    for (Node c : children) arr.put(c.toJSON());
                    o.put("children", arr);
                }
                return o;
            }
        }

        // 1) Charger tous les dossiers dans folderMap
        Map<Integer, Node> folderMap = new HashMap<>();
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlFolders)) {
                ps.setInt(1, uid);
                ps.setInt(2, uid);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String nom = rs.getString("nom");
                        Integer parentId = null;
                        // Utiliser getObject pour vérifier si parent_id est NULL
                        if (rs.getObject("parent_id") != null) {
                            parentId = rs.getInt("parent_id");
                        }
                        Node n = new Node(id, nom, true, parentId);
                        folderMap.put(id, n);
                    }
                }
            }

            // 2) Charger tous les fichiers, rattacher à leur dossier
            try (PreparedStatement ps = conn.prepareStatement(sqlFiles)) {
                ps.setInt(1, uid);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int fid = rs.getInt("id");
                        String fname = rs.getString("nom");
                        Integer parentId = rs.getObject("dossier_id") != null
                                ? rs.getInt("dossier_id") : null;
                        if (parentId != null && folderMap.containsKey(parentId)) {
                            Node fileNode = new Node(fid, fname, false, parentId);
                            folderMap.get(parentId).children.add(fileNode);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Erreur chargement structure", e);
        }

        // 3) Construire la hiérarchie des dossiers
        Map<Integer, Node> processedFolders = new HashMap<>();
        for (Node folder : folderMap.values()) {
            if (folder.parentId != null && folderMap.containsKey(folder.parentId)) {
                folderMap.get(folder.parentId).children.add(folder);
                processedFolders.put(folder.id, folder);
            }
        }

        // 4) Assembler la liste des racines (dossiers sans parent ou parent non accessible)
        List<Node> roots = new ArrayList<>();
        for (Node folder : folderMap.values()) {
            if (folder.parentId == null || !folderMap.containsKey(folder.parentId)) {
                roots.add(folder);
            }
        }

        // 5) Retour JSON
        JSONArray out = new JSONArray();
        for (Node r : roots) out.put(r.toJSON());
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(out.toString());
    }
}