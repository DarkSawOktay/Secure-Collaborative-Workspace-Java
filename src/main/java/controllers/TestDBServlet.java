package controllers;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import models.DBConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;


/**
 * TestDBServlet est un servlet conçu pour tester la connexion à une base de données MariaDB.
 *
 * Fonctionnalités principales :
 * - Établit une connexion avec une base de données via la classe utilitaire DBConnection.
 * - Retourne un message au client indiquant si la connexion a été établie avec succès ou si une erreur s'est produite.
 * - Gère les exceptions liées à l'accès à la base de données et informe l'utilisateur en conséquence.
 *
 * Ce servlet est accessible via l'URL "test-db".
 */
@WebServlet("/test-db")
public class TestDBServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (Connection conn = DBConnection.getConnection()) {
            response.getWriter().println("Connexion reussie a MariaDB !");
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            response.getWriter().println("Erreur de connexion : " + e.getMessage());
        }
    }
}
