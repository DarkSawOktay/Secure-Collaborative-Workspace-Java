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
import java.sql.*;

/**
 * Servlet permettant aux utilisateurs d'accepter une invitation
 * pour accéder à un fichier partagé avec des permissions spécifiques.
 *
 * Déployé sur le chemin URL /AcceptInvitationServlet.
 *
 * Cette classe interagit avec une base de données pour vérifier
 * la validité de l'invitation, accorder les permissions
 * et gérer les erreurs potentielles.
 */
@WebServlet("/AcceptInvitationServlet")
public class AcceptInvitationServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AcceptInvitationServlet.class.getName());

    private static final String SELECT_INV =
            "SELECT fichier_id, role, destinataire_id FROM invitations WHERE id = ?";
    private static final String CHECK_FILE_EXISTS =
            "SELECT 1 FROM fichiers WHERE id = ?";
    private static final String INSERT_DROIT =
            "INSERT INTO droits_acces(fichier_id, utilisateur_id, peut_modifier, role) VALUES(?, ?, ?, ?)";
    private static final String DELETE_INV =
            "DELETE FROM invitations WHERE id = ?";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Authentification
        HttpSession session = req.getSession(false);
        Utilisateurs me = (session != null)
                ? (Utilisateurs) session.getAttribute("utilisateur") : null;
        if (me == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Utilisateur non connecté");
            return;
        }

        // Validation des entrées
        int invId;
        try {
            invId = Integer.parseInt(req.getParameter("invitation_id"));
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "ID d'invitation invalide", e);
            sendJsonResponse(resp, false, "ID d'invitation invalide");
            return;
        }

        Connection conn = null;
        JSONObject json = new JSONObject();

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Traiter l'invitation
            if (processInvitation(conn, invId, me.getIdUtilisateur())) {
                conn.commit();
                sendJsonResponse(resp, true, "Invitation acceptée avec succès");
            } else {
                conn.rollback();
                sendJsonResponse(resp, false, "Vous n'êtes pas autorisé à accepter cette invitation");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors de l'acceptation de l'invitation", e);
            rollbackSafely(conn);
            sendJsonResponse(resp, false, "Erreur lors de l'acceptation de l'invitation");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'acceptation de l'invitation", e);
            rollbackSafely(conn);
            sendJsonResponse(resp, false, "Une erreur inattendue s'est produite");
        } finally {
            closeSafely(conn);
        }
    }

    /**
     * Traite l'invitation en vérifiant les autorisations et en effectuant les opérations nécessaires
     * @return true si l'invitation a été traitée avec succès, false sinon
     */
    private boolean processInvitation(Connection conn, int invId, int userId) throws SQLException {
        // Récupérer les informations de l'invitation
        int fichierId;
        String role;
        int destinataireId;

        try (PreparedStatement ps = conn.prepareStatement(SELECT_INV)) {
            ps.setInt(1, invId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    LOGGER.warning("Invitation " + invId + " introuvable");
                    return false;
                }
                fichierId = rs.getInt("fichier_id");
                role = rs.getString("role");
                destinataireId = rs.getInt("destinataire_id");

                // Vérifier que l'utilisateur est bien le destinataire
                if (destinataireId != userId) {
                    LOGGER.warning("Utilisateur " + userId + " tente d'accepter une invitation destinée à " + destinataireId);
                    return false;
                }
            }
        }

        // Vérifier que le fichier existe toujours
        if (!fileExists(conn, fichierId)) {
            LOGGER.warning("Fichier " + fichierId + " n'existe plus");
            return false;
        }

        // Ajouter le droit d'accès
        try (PreparedStatement ps = conn.prepareStatement(INSERT_DROIT)) {
            ps.setInt(1, fichierId);
            ps.setInt(2, userId);
            ps.setBoolean(3, "editor".equals(role));
            ps.setString(4, role);
            ps.executeUpdate();
        }

        // Supprimer l'invitation
        try (PreparedStatement ps = conn.prepareStatement(DELETE_INV)) {
            ps.setInt(1, invId);
            ps.executeUpdate();
        }



        return true;
    }

    /**
     * Vérifie si un fichier existe dans la base de données
     */
    private boolean fileExists(Connection conn, int fichierId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(CHECK_FILE_EXISTS)) {
            ps.setInt(1, fichierId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Envoie une réponse JSON au client
     */
    private void sendJsonResponse(HttpServletResponse resp, boolean success, String message) throws IOException {
        JSONObject json = new JSONObject();
        json.put("success", success);
        if (message != null) {
            json.put("message", message);
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json.toString());
    }

    /**
     * Effectue un rollback de manière sécurisée
     */
    private void rollbackSafely(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors du rollback", e);
            }
        }
    }

    /**
     * Ferme la connexion de manière sécurisée
     */
    private void closeSafely(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de la fermeture de la connexion", e);
            }
        }
    }
}