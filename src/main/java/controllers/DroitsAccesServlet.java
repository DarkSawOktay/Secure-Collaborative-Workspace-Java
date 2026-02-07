package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import models.DBConnection;
import models.Utilisateurs;
import org.json.JSONObject;
import org.json.JSONArray;

@WebServlet("/DroitsAccesServlet")
public class DroitsAccesServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Récupérer les droits d'accès pour un fichier
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Récupération de l'objet Utilisateurs
        Utilisateurs me = (Utilisateurs) session.getAttribute("utilisateur");
        int meId = me.getIdUtilisateur();
        int fichierId = Integer.parseInt(request.getParameter("fichier_id"));
        
        try (Connection conn = DBConnection.getConnection()) {
            int proprietaireId = -1;
            try (PreparedStatement ps = conn.prepareStatement("SELECT proprietaire_id FROM fichiers WHERE id = ?")) {
                ps.setInt(1, fichierId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    proprietaireId = rs.getInt("proprietaire_id");
                }
            }
            String sql = "SELECT u.id, u.nom, u.email, da.peut_modifier, da.role " +
                    "FROM utilisateurs u " +
                    "INNER JOIN droits_acces da ON u.id = da.utilisateur_id AND da.fichier_id = ? " +
                    "WHERE u.id != ? ";

            JSONArray usersArray = new JSONArray();
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, fichierId);
                pstmt.setInt(2, meId);
                
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    JSONObject user = new JSONObject();
                    int userId = rs.getInt("id");
                    user.put("id", rs.getInt("id"));
                    user.put("nom", rs.getString("nom"));
                    user.put("email", rs.getString("email"));
                    user.put("peutModifier", rs.getBoolean("peut_modifier"));

                    // Si c'est le propriétaire, définir un rôle spécial "proprietaire"
                    if (userId == proprietaireId) {
                        user.put("role", "proprietaire");
                        user.put("estProprietaire", true);
                    } else {
                        user.put("role", rs.getString("role"));
                        user.put("estProprietaire", false);
                    }
                    usersArray.put(user);
                }
            }

            // Ajouter aussi le propriétaire s'il n'est pas déjà dans la liste et s'il n'est pas l'utilisateur actuel
            if (proprietaireId != -1 && proprietaireId != meId) {
                boolean proprietaireInList = false;
                for (int i = 0; i < usersArray.length(); i++) {
                    JSONObject user = usersArray.getJSONObject(i);
                    if (user.getInt("id") == proprietaireId) {
                        proprietaireInList = true;
                        break;
                    }
                }

                if (!proprietaireInList) {
                    // Ajouter le propriétaire à la liste
                    try (PreparedStatement ps = conn.prepareStatement("SELECT id, nom, email FROM utilisateurs WHERE id = ?")) {
                        ps.setInt(1, proprietaireId);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            JSONObject proprietaire = new JSONObject();
                            proprietaire.put("id", rs.getInt("id"));
                            proprietaire.put("nom", rs.getString("nom"));
                            proprietaire.put("email", rs.getString("email"));
                            proprietaire.put("peutModifier", true);
                            proprietaire.put("role", "proprietaire");
                            proprietaire.put("estProprietaire", true);
                            usersArray.put(proprietaire);
                        }
                    }
                }
            }
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(usersArray.toString());
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Modifier les droits d'accès
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int fichierId = Integer.parseInt(request.getParameter("fichier_id"));
        Utilisateurs me = (Utilisateurs) session.getAttribute("utilisateur");
        int meId = me.getIdUtilisateur();
        boolean peutModifier = Boolean.parseBoolean(request.getParameter("peut_modifier"));
        
        try (Connection conn = DBConnection.getConnection()) {
            // Vérifier que l'utilisateur est le propriétaire du fichier
            String checkOwner = "SELECT proprietaire_id FROM fichiers WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkOwner)) {
                pstmt.setInt(1, fichierId);
                ResultSet rs = pstmt.executeQuery();
                if (!rs.next() || rs.getInt("proprietaire_id") != meId) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
            
            // Insérer ou mettre à jour les droits
            String sql = "INSERT INTO droits_acces (fichier_id, utilisateur_id, peut_modifier) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE peut_modifier = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, fichierId);
                pstmt.setInt(2, meId);
                pstmt.setBoolean(3, peutModifier);
                pstmt.setBoolean(4, peutModifier);
                pstmt.executeUpdate();
            }
            
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": true}");
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}