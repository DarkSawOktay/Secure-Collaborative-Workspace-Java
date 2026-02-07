package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import models.DBConnection;
import models.Utilisateurs;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/ListUtilisateursServlet")
public class ListUtilisateursServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Récupération de l'objet Utilisateurs en session
        Utilisateurs me = (Utilisateurs) session.getAttribute("utilisateur");
        // Puis extraction de son ID
        int userId = me.getIdUtilisateur();
        String fidParam = request.getParameter("fichier_id");
        if (fidParam == null || fidParam.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "fichier_id manquant");
            return;
        }
        int fichierId = Integer.parseInt(fidParam);


        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT u.id, u.nom, u.email " +
                    "FROM utilisateurs u " +
                    "WHERE u.id != ? " +
                    "  AND NOT EXISTS( " +
                    "    SELECT 1 FROM droits_acces da " +
                    "     WHERE da.utilisateur_id = u.id " +
                    "       AND da.fichier_id = ? " +
                    "  )";
            
            JSONArray users = new JSONArray();
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, fichierId);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    JSONObject user = new JSONObject();
                    user.put("id", rs.getInt("id"));
                    user.put("nom", rs.getString("nom"));
                    user.put("email", rs.getString("email"));
                    users.put(user);
                }
            }
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(users.toString());
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}