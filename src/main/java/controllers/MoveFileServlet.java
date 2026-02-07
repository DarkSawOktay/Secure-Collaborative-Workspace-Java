package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import models.DBConnection;
import models.Utilisateurs;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet pour gérer le déplacement de fichiers via glisser-déposer ou interface
 */
@WebServlet("/MoveFileServlet")
public class MoveFileServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(MoveFileServlet.class.getName());
    private static final String SQL_UPDATE =
            "UPDATE fichiers SET dossier_id = ? WHERE id = ?";
    private static final String SQL_CHECK_ACCESS =
            "SELECT COUNT(*) FROM droits_acces WHERE fichier_id = ? AND utilisateur_id = ? AND peut_modifier = TRUE";
    private static final String SQL_CHECK_OWNER =
            "SELECT proprietaire_id FROM fichiers WHERE id = ?";
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
        int fileId = Integer.parseInt(req.getParameter("fileId"));
        String targetParam = req.getParameter("targetFolderId");
        Integer targetFolderId = (targetParam == null || targetParam.isEmpty())
                ? null : Integer.valueOf(targetParam);

        JSONObject response = new JSONObject();

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1. Vérifier si l'utilisateur a les droits pour modifier ce fichier
                boolean canModify = canModifyFile(conn, fileId, userId);
                if (!canModify) {
                    LOGGER.log(Level.WARNING, "Tentative de déplacement sans autorisation. " +
                            "Fichier: {0}, Utilisateur: {1}", new Object[]{fileId, userId});
                    response.put("success", false)
                            .put("message", "Vous n'avez pas les droits pour déplacer ce fichier");
                    sendJsonResponse(resp, response);
                    return;
                }

                // 2. Effectuer le déplacement
                try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
                    if (targetFolderId == null) {
                        ps.setNull(1, Types.INTEGER);
                    } else {
                        ps.setInt(1, targetFolderId);
                    }
                    ps.setInt(2, fileId);
                    int updated = ps.executeUpdate();

                    if (updated > 0) {
                        // 3. Trouver le projet parent (dossier racine) du dossier cible
                        Integer projectId = null;
                        if (targetFolderId != null) {
                            projectId = findRootFolderId(conn, targetFolderId);
                            LOGGER.log(Level.INFO, "Projet parent identifié: {0}", projectId);
                        }

                        // 4. Vérifier que l'utilisateur a toujours accès après le déplacement
                        ensureUserAccess(conn, fileId, userId);

                        conn.commit();
                        LOGGER.log(Level.INFO, "Fichier déplacé avec succès. " +
                                        "Fichier: {0}, Nouveau dossier: {1}, Projet: {2}",
                                new Object[]{fileId, targetFolderId, projectId});

                        response.put("success", true)
                                .put("message", "Fichier déplacé avec succès");

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
                                .put("message", "Aucun fichier n'a été mis à jour");
                    }
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors du déplacement du fichier", e);
            response.put("success", false)
                    .put("message", "Erreur serveur lors du déplacement");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur inattendue lors du déplacement du fichier", e);
            response.put("success", false)
                    .put("message", "Erreur inattendue");
        }

        sendJsonResponse(resp, response);
    }

    /**
     * Vérifie si l'utilisateur peut modifier le fichier
     * (soit il est propriétaire, soit il a les droits d'édition)
     */
    private boolean canModifyFile(Connection conn, int fileId, int userId) throws SQLException {
        // D'abord vérifier si propriétaire
        try (PreparedStatement ps = conn.prepareStatement(SQL_CHECK_OWNER)) {
            ps.setInt(1, fileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt("proprietaire_id") == userId) {
                    return true;
                }
            }
        }

        // Sinon vérifier droits d'accès en modification
        try (PreparedStatement ps = conn.prepareStatement(SQL_CHECK_ACCESS)) {
            ps.setInt(1, fileId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        }

        return false;
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
     * S'assure que l'utilisateur a toujours accès au fichier après le déplacement
     * Si ce n'est pas le cas, ajoute les droits d'accès
     */
    private void ensureUserAccess(Connection conn, int fileId, int userId) throws SQLException {
        // Vérifier si l'utilisateur a encore accès au fichier
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM droits_acces WHERE fichier_id = ? AND utilisateur_id = ?")) {
            ps.setInt(1, fileId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int count = rs.getInt(1);

                // Si l'utilisateur n'a plus d'accès, lui en donner un
                if (count == 0) {
                    LOGGER.log(Level.INFO, "Ajout de droits d'accès pour l'utilisateur {0} sur le fichier {1}",
                            new Object[]{userId, fileId});

                    try (PreparedStatement psInsert = conn.prepareStatement(
                            "INSERT INTO droits_acces(fichier_id, utilisateur_id, peut_modifier, role) VALUES(?, ?, TRUE, 'editor')")) {
                        psInsert.setInt(1, fileId);
                        psInsert.setInt(2, userId);
                        psInsert.executeUpdate();
                    }
                }
            }
        }
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