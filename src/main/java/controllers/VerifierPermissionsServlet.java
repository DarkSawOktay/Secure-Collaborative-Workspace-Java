package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import models.DBConnection;
import models.Utilisateurs;
import org.json.JSONObject;

@WebServlet("/VerifierPermissionsServlet")
public class VerifierPermissionsServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int fichierId = Integer.parseInt(request.getParameter("fichier_id"));
        Utilisateurs user = (Utilisateurs) session.getAttribute("utilisateur");
        int userId = user.getIdUtilisateur();
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT f.proprietaire_id, CASE WHEN f.proprietaire_id = ? THEN TRUE WHEN da.peut_modifier IS TRUE THEN TRUE ELSE FALSE END as peut_modifier, f.proprietaire_id = ? as est_proprietaire FROM fichiers f LEFT JOIN droits_acces da ON da.fichier_id = f.id AND da.utilisateur_id = ? WHERE f.id = ? ";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, userId);
                pstmt.setInt(3, userId);
                pstmt.setInt(4, fichierId);
                
                ResultSet rs = pstmt.executeQuery();
                JSONObject permissions = new JSONObject();
                
                if (rs.next()) {
                    permissions.put("peutModifier", rs.getBoolean("peut_modifier"));
                    permissions.put("estProprietaire", rs.getBoolean("est_proprietaire"));
                }
                
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(permissions.toString());
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}