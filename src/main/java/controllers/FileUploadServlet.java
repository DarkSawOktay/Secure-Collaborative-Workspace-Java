package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;

import models.DBConnection;
import org.json.JSONObject;

@WebServlet("/FileUploadServlet")
@MultipartConfig
public class FileUploadServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Part filePart = request.getPart("file");
        String fileName = filePart.getSubmittedFileName();
        String content = new String(filePart.getInputStream().readAllBytes());
        int userId = (Integer) session.getAttribute("utilisateur");

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Insertion du fichier
            int fileId;
            String sqlFile = "INSERT INTO fichiers (nom, contenu, proprietaire_id) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlFile, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, fileName);
                pstmt.setString(2, content);
                pstmt.setInt(3, userId);
                pstmt.executeUpdate();

                // Récupération de l'ID du fichier inséré
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        fileId = rs.getInt(1);
                    } else {
                        throw new SQLException("Échec de création du fichier, aucun ID obtenu.");
                    }
                }
            }

            // Ajout des droits d'accès pour le propriétaire (tous les droits)
            String sqlDroits = "INSERT INTO droits_acces (fichier_id, utilisateur_id, peut_modifier) VALUES (?, ?, TRUE)";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlDroits)) {
                pstmt.setInt(1, fileId);
                pstmt.setInt(2, userId);
                pstmt.executeUpdate();
            }

            conn.commit();

            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("fileName", fileName);
            jsonResponse.put("content", content);
            jsonResponse.put("fileId", fileId);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonResponse.toString());

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}