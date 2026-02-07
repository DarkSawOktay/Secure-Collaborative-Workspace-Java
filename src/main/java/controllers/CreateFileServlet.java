package controllers;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import models.DBConnection;
import models.Utilisateurs;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;

/**
 * La classe CreateFileServlet est une servlet utilisée pour gérer la création
 * de nouveaux fichiers dans une application web. Elle permet à un utilisateur
 * authentifié de créer un fichier, de l'associer à un dossier parent (optionnel),
 * et de lui attribuer des droits d'accès.
 *
 * Fonctionnalités principales :
 * 1. Vérifie l'authentification de l'utilisateur à partir de la session HTTP.
 * 2. Récupère le nom du fichier à partir des paramètres de la requête. Si aucun nom
 *    n'est fourni, un nom par défaut est attribué.
 * 3. Crée un nouveau fichier dans la base de données en associant son contenu
 *    et ses droits d'accès au créateur.
 * 4. Facultativement, associe le fichier à un dossier parent spécifié.
 * 5. Retourne l'identifiant du nouveau fichier en réponse au format JSON pour
 *    permettre à l'interface utilisateur de rediriger vers un éditeur de fichier.
 *
 * Méthodes principales :
 * - doPost(HttpServletRequest req, HttpServletResponse resp) :
 *   Gère la requête HTTP POST pour la création du fichier. Cette méthode
 *   traite les données de la requête, interagit avec la base de données
 *   et renvoie la réponse appropriée au client.
 *
 * Cas d'erreurs :
 * - Si l'utilisateur n'est pas authentifié, la servlet renvoie le code
 *   HTTP 401 (Unauthorized).
 * - En cas d'erreur lors de la création du fichier ou de l'interaction avec
 *   la base de données, une exception ServletException est levée.
 *
 * Remarques :
 * - Cette classe dépend de la connexion à une base de données via
 *   la classe DBConnection pour exécuter les requêtes SQL.
 * - L'identification de l'utilisateur authentifié repose sur l'utilisation de
 *   la classe Utilisateurs, qui stocke les informations de l'utilisateur
 *   actuel via les attributs de session.
 */
@WebServlet("/CreateFileServlet")
public class CreateFileServlet extends HttpServlet {
    private static final String INSERT_SQL =
            "INSERT INTO fichiers (nom, contenu, proprietaire_id, dossier_id) VALUES (?, '', ?, ?)";
    private static final String INSERT_DROITS =
            "INSERT INTO droits_acces (fichier_id, utilisateur_id, peut_modifier) VALUES (?, ?, TRUE)";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Utilisateurs u = (session != null)
                ? (Utilisateurs) session.getAttribute("utilisateur")
                : null;
        if (u == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        int userId = u.getIdUtilisateur();

        // 1) Récupération du nom et de l'ID du dossier parent (ou null)
        String name = req.getParameter("name");
        if (name == null || name.isBlank()) {
            name = "Nouveau-fichier.txt";
        }
        String parentParam = req.getParameter("parentId");
        Integer parentId = (parentParam == null || parentParam.isEmpty())
                ? null : Integer.valueOf(parentParam);

        int newFileId;
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            // 2) Création du fichier avec son dossier
            try (PreparedStatement ps = conn.prepareStatement(
                    INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setInt(2, userId);
                if (parentId != null) {
                    ps.setInt(3, parentId);
                } else {
                    ps.setNull(3, Types.INTEGER);
                }
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    newFileId = rs.getInt(1);
                }
            }
            // 3) Ajout des droits pour le créateur
            try (PreparedStatement ps2 = conn.prepareStatement(INSERT_DROITS)) {
                ps2.setInt(1, newFileId);
                ps2.setInt(2, userId);
                ps2.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            throw new ServletException("Erreur création fichier", e);
        }

        // 4) On renvoie l'ID pour que le front redirige vers l'éditeur
        JSONObject json = new JSONObject().put("fileId", newFileId);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json.toString());
    }
}
