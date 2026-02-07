package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import models.DBConnection;
import models.Utilisateurs;
import org.json.JSONObject;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/AddFriendServlet")
public class AddFriendServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AddFriendServlet.class.getName());
    private static final String INSERT_SQL = "INSERT INTO amis(user_id_1, user_id_2) VALUES(?, ?)";
    private static final String CHECK_USER_SQL = "SELECT 1 FROM utilisateurs WHERE id = ?";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        JSONObject json = new JSONObject();

        try {
            // Vérification de l'authentification
            if (session == null || session.getAttribute("utilisateur") == null) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            Utilisateurs me = (Utilisateurs) session.getAttribute("utilisateur");
            int myId = me.getIdUtilisateur();
            int otherId;

            // Validation des paramètres
            try {
                otherId = Integer.parseInt(req.getParameter("userId"));
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Format invalide pour userId", e);
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Format d'ID invalide");
                return;
            }

            // Ne pas s'ajouter soi-même
            if (otherId == myId) {
                LOGGER.log(Level.INFO, "Tentative d'ajout de soi-même comme ami: " + myId);
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Impossible de s'ajouter soi-même");
                return;
            }

            Connection conn = null;
            boolean success = false;

            try {
                conn = DBConnection.getConnection();
                conn.setAutoCommit(false);  // Début de transaction

                // Vérifier si l'utilisateur cible existe
                if (!userExists(conn, otherId)) {
                    LOGGER.log(Level.WARNING, "Utilisateur inexistant: " + otherId);
                    json.put("success", false);
                    json.put("message", "L'utilisateur spécifié n'existe pas");
                    sendJsonResponse(resp, json);
                    return;
                }

                // Conserver toujours user_id_1 < user_id_2
                int uid1 = Math.min(myId, otherId);
                int uid2 = Math.max(myId, otherId);

                try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
                    ps.setInt(1, uid1);
                    ps.setInt(2, uid2);
                    ps.executeUpdate();

                    conn.commit();
                    success = true;
                    LOGGER.log(Level.INFO, "Ami ajouté avec succès: " + myId + " -> " + otherId);
                }
            } catch (SQLException e) {
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        LOGGER.log(Level.SEVERE, "Erreur lors du rollback", ex);
                    }
                }
                // Vérifier si c'est une erreur de duplication (contrainte unique)
                if (e.getSQLState().equals("23000") || e.getMessage().contains("duplicate")) {
                    LOGGER.log(Level.INFO, "Relation d'amitié déjà existante: " + myId + " -> " + otherId);
                    json.put("message", "Cet utilisateur est déjà dans votre liste d'amis");
                } else {
                    LOGGER.log(Level.SEVERE, "Erreur SQL lors de l'ajout d'un ami", e);
                    json.put("message", "Erreur serveur lors de l'ajout de l'ami");
                }
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException e) {
                        LOGGER.log(Level.SEVERE, "Erreur lors de la fermeture de la connexion", e);
                    }
                }
            }

            json.put("success", success);
            sendJsonResponse(resp, json);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur non prévue", e);
            json.put("success", false);
            json.put("message", "Erreur serveur inattendue");
            sendJsonResponse(resp, json);
        }
    }

    /**
     * Vérifie si un utilisateur existe dans la base de données
     */
    private boolean userExists(Connection conn, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(CHECK_USER_SQL)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Envoie une réponse JSON standardisée
     */
    private void sendJsonResponse(HttpServletResponse resp, JSONObject json) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json.toString());
    }
}