package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import models.DBConnection;
import models.Utilisateurs;

import java.io.IOException;
import java.sql.*;

/**
 * La classe CreateFolderServlet est un servlet responsable de la création d'un
 * dossier dans le système via une requête HTTP POST. Elle reçoit les données nécessaires
 * depuis la requête, les traite et interagit avec une base de données pour insérer un nouveau
 * dossier.
 *
 * Fonctionnalités principales :
 * - Récupérer les paramètres de la requête tels que le nom du dossier, l'identifiant du parent
 *   et l'utilisateur effectuant l'opération.
 * - Effectuer une insertion dans la table "dossiers" de la base de données en utilisant
 *   une requête SQL préparée.
 * - Gérer les erreurs potentielles liées à la base de données et aux opérations de servlet.
 * - Retourner une réponse JSON, contenant l'identifiant du dossier nouvellement créé, au client.
 *
 * Méthode principale :
 * - La méthode doPost(HttpServletRequest req, HttpServletResponse resp) est appelée pour traiter
 *   les requêtes POST. Elle gère toute la logique de création d'un dossier à partir des données entrantes.
 *
 * Exceptions :
 * - ServletException est levée en cas d'erreur lors de l'exécution de la logique du servlet.
 * - IOException est levée en cas de problème lors de l'écriture de la réponse.
 *
 * Remarque :
 * - Ce servlet attend qu'un objet "utilisateur" lié à la session soit présent pour identifier
 *   le propriétaire du dossier à créer.
 * - L'accès à la base de données est géré via un système de connexion fourni par la classe DBConnection.
 */
@WebServlet("/CreateFolderServlet")
public class CreateFolderServlet extends HttpServlet {
    private static final String SQL =
            "INSERT INTO dossiers(nom, parent_id, proprietaire_id) VALUES(?, ?, ?)";
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Utilisateurs u = (Utilisateurs)req.getSession(false)
                .getAttribute("utilisateur");
        int ownerId = u.getIdUtilisateur();
        String name = req.getParameter("nom");
        String p = req.getParameter("parentId");
        Integer parentId = (p==null || p.isEmpty())? null : Integer.valueOf(p);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            if (parentId==null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, parentId);
            ps.setInt(3, ownerId);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys(); rs.next();
            int newId = rs.getInt(1);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"id\":"+newId+"}");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}

