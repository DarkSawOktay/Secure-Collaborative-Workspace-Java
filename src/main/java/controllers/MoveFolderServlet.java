package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import models.DBConnection;
import models.Utilisateurs;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet pour gérer le déplacement des dossiers via drag-and-drop
 */
@WebServlet("/MoveFolderServlet")
public class MoveFolderServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(MoveFolderServlet.class.getName());
    private static final String SQL_UPDATE =
            "UPDATE dossiers SET parent_id = ? WHERE id = ?";
    private static final String SQL_CHECK_OWNERSHIP =
            "SELECT proprietaire_id FROM dossiers WHERE id = ?";
    private static final String SQL_CHECK_CIRCULAR =
            "WITH RECURSIVE folder_path(id, parent_id) AS (" +
                    "  SELECT id, parent_id FROM dossiers WHERE id = ? " +
                    "  UNION ALL " +
                    "  SELECT d.id, d.parent_id FROM dossiers d, folder_path fp " +
                    "  WHERE d.id = fp.parent_id" +
                    ") " +
                    "SELECT 1 FROM folder_path WHERE id = ? LIMIT 1";
    private static final String SQL_FIND_ROOT_FOLDER =
            "WITH RECURSIVE folder_path AS (" +
                    "  SELECT id, parent_id FROM dossiers WHERE id = ? " +
                    "  UNION ALL " +
                    "  SELECT d.id, d.parent_id FROM dossiers d, folder_path fp " +
                    "  WHERE d.id = fp.parent_id" +
                    ") " +
                    "SELECT id FROM folder_path WHERE parent_id IS NULL LIMIT 1";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Vérification de l'authentification
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Utilisateurs user = (Utilisateurs) session.getAttribute("utilisateur");
        int userId = user.getIdUtilisateur();

        // Récupération des paramètres
        int folderId = Integer.parseInt(req.getParameter("folderId"));
        String targetParam = req.getParameter("targetFolderId");
        Integer targetFolderId = (targetParam == null || targetParam.isEmpty())
                ? null : Integer.valueOf(targetParam);

        JSONObject response = new JSONObject();

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);  // Commencer une transaction

            try {
                // 1. Vérifier que l'utilisateur est propriétaire du dossier
                boolean isOwner = checkOwnership(conn, folderId, userId);
                if (!isOwner) {
                    LOGGER.log(Level.WARNING,
                            "Tentative de déplacement d'un dossier par un utilisateur non propriétaire. " +
                                    "Dossier: {0}, Utilisateur: {1}", new Object[]{folderId, userId});
                    response.put("success", false)
                            .put("message", "Vous n'avez pas les droits pour déplacer ce dossier");
                    sendJsonResponse(resp, response);
                    return;
                }

                // 2. Vérifier qu'il n'y a pas de dépendance circulaire
                if (targetFolderId != null && wouldCreateCircularDependency(conn, folderId, targetFolderId)) {
                    LOGGER.log(Level.WARNING, "Tentative de création d'une dépendance circulaire. " +
                            "Dossier: {0}, Cible: {1}", new Object[]{folderId, targetFolderId});
                    response.put("success", false)
                            .put("message", "Ce déplacement créerait une dépendance circulaire");
                    sendJsonResponse(resp, response);
                    return;
                }

                // 3. Effectuer le déplacement
                try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
                    if (targetFolderId == null) {
                        ps.setNull(1, Types.INTEGER);
                    } else {
                        ps.setInt(1, targetFolderId);
                    }
                    ps.setInt(2, folderId);
                    int updated = ps.executeUpdate();

                    if (updated > 0) {
                        // 4. Trouver le projet parent (dossier racine) du dossier cible
                        Integer projectId = null;
                        if (targetFolderId != null) {
                            projectId = findRootFolderId(conn, targetFolderId);
                            LOGGER.log(Level.INFO, "Projet parent identifié: {0}", projectId);
                        }

                        conn.commit();
                        LOGGER.log(Level.INFO, "Dossier déplacé avec succès. " +
                                        "Dossier: {0}, Nouveau parent: {1}, Projet: {2}",
                                new Object[]{folderId, targetFolderId, projectId});

                        response.put("success", true)
                                .put("message", "Dossier déplacé avec succès");

                        // Ajouter le projectId à la réponse
                        if (projectId != null) {
                            response.put("projectId", projectId);
                        } else if (targetFolderId != null) {
                            // Si on n'a pas trouvé de racine, utiliser le dossier cible lui-même
                            response.put("projectId", targetFolderId);
                        }
                    } else {
                        conn.rollback();
                        response.put("success", false)
                                .put("message", "Aucun dossier n'a été mis à jour");
                    }
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors du déplacement du dossier", e);
            response.put("success", false)
                    .put("message", "Erreur serveur lors du déplacement");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur inattendue lors du déplacement du dossier", e);
            response.put("success", false)
                    .put("message", "Erreur inattendue");
        }

        sendJsonResponse(resp, response);
    }

    /**
     * Vérifie si l'utilisateur est propriétaire du dossier
     */
    private boolean checkOwnership(Connection conn, int folderId, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_CHECK_OWNERSHIP)) {
            ps.setInt(1, folderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int proprietaireId = rs.getInt("proprietaire_id");
                    return proprietaireId == userId;
                }
                return false;
            }
        }
    }

    /**
     * Vérifie si le déplacement créerait une dépendance circulaire
     */
    private boolean wouldCreateCircularDependency(Connection conn, int folderId, int targetFolderId) throws SQLException {
        // Pour éviter les dépendances circulaires, on vérifie si le dossier cible est un enfant du dossier à déplacer
        try (PreparedStatement ps = conn.prepareStatement(SQL_CHECK_CIRCULAR)) {
            ps.setInt(1, targetFolderId); // Le point de départ est la cible
            ps.setInt(2, folderId);       // On cherche dans le chemin si on trouve le dossier à déplacer
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Si on trouve, c'est qu'il y aurait une dépendance circulaire
            }
        }
    }

    /**
     * Trouve le dossier racine (projet) pour un dossier donné
     * @param conn Connexion à la base de données
     * @param folderId ID du dossier
     * @return ID du dossier racine (celui qui n'a pas de parent), ou null si non trouvé
     */
    private Integer findRootFolderId(Connection conn, Integer folderId) throws SQLException {
        if (folderId == null) return null;

        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_ROOT_FOLDER)) {
            ps.setInt(1, folderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        // Si on ne trouve pas de racine, le dossier est peut-être lui-même un dossier racine
        try (PreparedStatement ps = conn.prepareStatement("SELECT parent_id FROM dossiers WHERE id = ?")) {
            ps.setInt(1, folderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getObject("parent_id") == null) {
                    return folderId;  // C'est un dossier racine
                }
            }
        }

        return null;
    }

    /**
     * Envoie une réponse JSON au client
     */
    private void sendJsonResponse(HttpServletResponse resp, JSONObject json) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json.toString());
    }
}