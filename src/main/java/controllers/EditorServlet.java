package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import models.DBConnection;
import models.Utilisateurs;

import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/EditorServlet")
public class EditorServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(EditorServlet.class.getName());

    private static final String FIND_FIRST =
            "SELECT id FROM fichiers WHERE proprietaire_id = ? ORDER BY id LIMIT 1";
    private static final String INSERT_DROITS =
            "INSERT INTO droits_acces(fichier_id, utilisateur_id, peut_modifier, role) VALUES(?, ?, TRUE, 'editor')";
    private static final String LOAD_SQL =
            "SELECT nom, contenu FROM fichiers WHERE id = ?";

    // Requête pour trouver le projet (dossier racine) d'un fichier
    private static final String FIND_ROOT_PROJECT =
            "WITH RECURSIVE folder_path AS (" +
                    "  SELECT d.id, d.parent_id, d.nom " +
                    "  FROM dossiers d " +
                    "  JOIN fichiers f ON f.dossier_id = d.id " +
                    "  WHERE f.id = ? " +
                    "  UNION ALL " +
                    "  SELECT d.id, d.parent_id, d.nom " +
                    "  FROM dossiers d " +
                    "  JOIN folder_path fp ON d.id = fp.parent_id" +
                    ") " +
                    "SELECT id, nom FROM folder_path WHERE parent_id IS NULL LIMIT 1";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        Utilisateurs me = (Utilisateurs) session.getAttribute("utilisateur");
        int userId = me.getIdUtilisateur();

        // Vérifier si l'utilisateur a explicitement demandé la page de sélection de projet
        String showSelectParam = req.getParameter("showSelect");
        boolean showSelectPage = "true".equals(showSelectParam);

        String projectParam = req.getParameter("projectId");
        String fileParam    = req.getParameter("fileId");
        Integer fileId = (fileParam != null && !fileParam.isEmpty())
                ? Integer.valueOf(fileParam) : null;
        Integer projectId = (projectParam != null && !projectParam.isEmpty())
                ? Integer.valueOf(projectParam) : null;

        LOGGER.log(Level.INFO, "EditorServlet: fileId={0}, projectId={1}, showSelect={2}",
                new Object[]{fileId, projectId, showSelectPage});

        // Si demande explicite de la page de sélection, ignorer le projectId en session
        if (showSelectPage) {
            LOGGER.log(Level.INFO, "Affichage explicite de la page de sélection de projet demandé");
            req.getRequestDispatcher("/WEB-INF/selectProject.jsp").forward(req, resp);
            return;
        }

        // Vérifier aussi le sessionStorage si pas de projectId dans la requête
        if (projectId == null && !showSelectPage) {
            String storedProjectId = (String) session.getAttribute("lastProjectId");
            if (storedProjectId != null && !storedProjectId.isEmpty()) {
                try {
                    projectId = Integer.valueOf(storedProjectId);
                    LOGGER.log(Level.INFO, "Récupération du projectId depuis la session: {0}", projectId);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Impossible de convertir le projectId stocké: {0}", storedProjectId);
                }
            }
        }

        // Si on a un projectId, le stocker dans la session pour les futures requêtes
        if (projectId != null) {
            session.setAttribute("lastProjectId", projectId.toString());
            LOGGER.log(Level.INFO, "ProjectId {0} sauvegardé dans la session", projectId);
        }

        try (Connection conn = DBConnection.getConnection()) {
            // 1) Cas projectId sans fileId rediriger vers premier fichier du projet
            if (fileId == null && projectId != null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT id FROM fichiers WHERE dossier_id = ? ORDER BY id LIMIT 1")) {
                    ps.setInt(1, projectId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            resp.sendRedirect(req.getContextPath()
                                    + "/EditorServlet?fileId=" + rs.getInt("id") + "&projectId=" + projectId);
                            return;
                        }
                    }
                }

                // Chercher aussi dans les sous-dossiers du projet
                try (PreparedStatement ps = conn.prepareStatement(
                        "WITH RECURSIVE sub_folders AS (" +
                                "  SELECT id FROM dossiers WHERE id = ? " +
                                "  UNION ALL " +
                                "  SELECT d.id FROM dossiers d JOIN sub_folders sf ON d.parent_id = sf.id" +
                                ") " +
                                "SELECT f.id FROM fichiers f JOIN sub_folders sf ON f.dossier_id = sf.id " +
                                "ORDER BY f.id LIMIT 1")) {
                    ps.setInt(1, projectId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            resp.sendRedirect(req.getContextPath()
                                    + "/EditorServlet?fileId=" + rs.getInt("id") + "&projectId=" + projectId);
                            return;
                        }
                    }
                }
                // si toujours pas de fichier, afficher la page pour en créer un
                req.setAttribute("projectId", projectId);
                req.getRequestDispatcher("/WEB-INF/emptyProject.jsp").forward(req, resp);
                return;
            }

            // 2) Si ni projectId ni fileId afficher selectProject.jsp
            if (fileId == null) {
                req.getRequestDispatcher("/WEB-INF/selectProject.jsp")
                        .forward(req, resp);
                return;
            }

            // 3) Si fileId est spécifié mais pas projectId, essayer de le trouver
            if (fileId != null && projectId == null) {
                projectId = findProjectForFile(conn, fileId);
                LOGGER.log(Level.INFO, "Projet trouvé pour le fichier {0}: {1}", new Object[]{fileId, projectId});

                // Si on ne trouve toujours pas, essayer de trouver le dossier direct
                if (projectId == null) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT dossier_id FROM fichiers WHERE id = ?")) {
                        ps.setInt(1, fileId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next() && rs.getObject("dossier_id") != null) {
                                int dossierId = rs.getInt("dossier_id");
                                // Vérifier si c'est un dossier racine
                                try (PreparedStatement ps2 = conn.prepareStatement(
                                        "SELECT parent_id FROM dossiers WHERE id = ?")) {
                                    ps2.setInt(1, dossierId);
                                    try (ResultSet rs2 = ps2.executeQuery()) {
                                        if (rs2.next() && rs2.getObject("parent_id") == null) {
                                            // C'est un dossier racine, donc un projet
                                            projectId = dossierId;
                                        } else if (rs2.next()) {
                                            // C'est un sous-dossier, trouver sa racine
                                            try (PreparedStatement ps3 = conn.prepareStatement(
                                                    "WITH RECURSIVE folder_path AS (" +
                                                            "  SELECT id, parent_id FROM dossiers WHERE id = ? " +
                                                            "  UNION ALL " +
                                                            "  SELECT d.id, d.parent_id FROM dossiers d " +
                                                            "  JOIN folder_path fp ON d.id = fp.parent_id" +
                                                            ") " +
                                                            "SELECT id FROM folder_path WHERE parent_id IS NULL LIMIT 1")) {
                                                ps3.setInt(1, dossierId);
                                                try (ResultSet rs3 = ps3.executeQuery()) {
                                                    if (rs3.next()) {
                                                        projectId = rs3.getInt("id");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4) Stocker le projectId dans le request pour l'utiliser dans la JSP
            if (projectId != null) {
                req.setAttribute("projectId", projectId);
                // Aussi le stocker dans la session pour les futures requêtes
                session.setAttribute("lastProjectId", projectId.toString());
                LOGGER.log(Level.INFO, "ProjectId {0} ajouté aux attributs de la requête et sauvegardé en session", projectId);
            }

            // 5) Enfin, charger le fichier demandé
            forwardToEditor(req, resp, fileId, conn);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors de l'initialisation de l'éditeur", e);
            throw new ServletException("Erreur initialisation éditeur", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Procéder à la création du dossier + main.java
        HttpSession session = req.getSession(false);
        Utilisateurs me = session != null ? (Utilisateurs) session.getAttribute("utilisateur") : null;
        if (me == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        int userId = me.getIdUtilisateur();

        String projectName = req.getParameter("projectName");
        if (projectName == null || projectName.isBlank()) {
            req.setAttribute("error", "Le nom du projet est requis.");
            req.getRequestDispatcher("/WEB-INF/createProject.jsp")
                    .forward(req, resp);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // 1) Créer le dossier principal
            int folderId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO dossiers(nom, parent_id, proprietaire_id) VALUES(?, NULL, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, projectName);
                ps.setInt(2, userId);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    folderId = rs.getInt(1);
                }
            }

            // 2) Créer le fichier Main.java
            int mainFileId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO fichiers(nom, contenu, proprietaire_id, dossier_id) "
                            + "VALUES('Main.java', "
                            + "'public class Main { public static void main(String[] args) { } }', ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setInt(2, folderId);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    mainFileId = rs.getInt(1);
                }
            }

            // 3) Donner les droits au créateur
            try (PreparedStatement ps = conn.prepareStatement(INSERT_DROITS)) {
                ps.setInt(1, mainFileId);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }

            conn.commit();

            // 4) Rediriger vers l'éditeur du Main.java avec l'ID du projet
            resp.sendRedirect(req.getContextPath()
                    + "/EditorServlet?fileId=" + mainFileId + "&projectId=" + folderId);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors de la création du projet", e);
            throw new ServletException("Erreur création du projet initial", e);
        }
    }

    /**
     * Trouve le projet (dossier racine) auquel appartient un fichier
     * @param conn Connection à la BD
     * @param fileId ID du fichier
     * @return ID du projet (dossier racine) ou null si non trouvé
     */
    private Integer findProjectForFile(Connection conn, int fileId) {
        try (PreparedStatement ps = conn.prepareStatement(FIND_ROOT_PROJECT)) {
            ps.setInt(1, fileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la recherche du projet pour le fichier " + fileId, e);
        }
        return null;
    }

    /**
     * Charge le contenu du fichier et transmet à l'éditeur JSP
     */
    private void forwardToEditor(HttpServletRequest req, HttpServletResponse resp,
                                 Integer fileId, Connection conn)
            throws SQLException, ServletException, IOException {
        try (PreparedStatement ps = conn.prepareStatement(LOAD_SQL)) {
            ps.setInt(1, fileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String fileName = rs.getString("nom");
                    String content = rs.getString("contenu");

                    req.setAttribute("fileId", fileId);
                    req.setAttribute("fileName", fileName);
                    req.setAttribute("content", content);

                    // Vérifier si le fichier appartient à un projet
                    Integer projectId = (Integer) req.getAttribute("projectId");
                    if (projectId != null) {
                        LOGGER.log(Level.INFO, "Forward vers editor.jsp avec projectId=" + projectId);
                    } else {
                        LOGGER.log(Level.WARNING, "Forward vers editor.jsp sans projectId pour fileId=" + fileId);
                    }

                    // Vérifier que le fichier editor.jsp existe
                    String jspPath = "/editor.jsp";
                    if (req.getServletContext().getResourceAsStream(jspPath) == null) {
                        LOGGER.log(Level.SEVERE, "Le fichier JSP " + jspPath + " est introuvable");
                        // Essayer le chemin avec WEB-INF
                        jspPath = "/WEB-INF/editor.jsp";
                        if (req.getServletContext().getResourceAsStream(jspPath) == null) {
                            LOGGER.log(Level.SEVERE, "Le fichier JSP " + jspPath + " est aussi introuvable");
                            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                    "Erreur de configuration: Le template editor.jsp est introuvable.");
                            return;
                        }
                    }

                    req.getRequestDispatcher(jspPath).forward(req, resp);
                } else {
                    // Fichier non trouvé
                    LOGGER.log(Level.WARNING, "Fichier avec ID=" + fileId + " introuvable dans la base de données");
                    // Rediriger vers la sélection de projet avec un message d'erreur
                    req.setAttribute("error", "Le fichier demandé n'existe plus ou a été déplacé.");

                    // Si on a un projectId, essayer de retourner à ce projet
                    Integer projectId = (Integer) req.getAttribute("projectId");
                    if (projectId != null) {
                        resp.sendRedirect(req.getContextPath() + "/EditorServlet?projectId=" + projectId);
                    } else {
                        req.getRequestDispatcher("/WEB-INF/selectProject.jsp").forward(req, resp);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors du chargement du fichier " + fileId, e);
            req.setAttribute("error", "Erreur lors du chargement du fichier: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/selectProject.jsp").forward(req, resp);
        }
    }
}