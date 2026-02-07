package controllers;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * LogoutServlet est un servlet responsable de la déconnexion des utilisateurs authentifiés.
 * Cette classe gère principalement l'invalidation de la session en cours
 * et redirige l'utilisateur vers la page de connexion après la déconnexion.
 *
 * Fonctionnalités principales :
 * - Invalidation de la session utilisateur.
 * - Redirection vers la page de connexion pour sécuriser l'accès ultérieur.
 *
 * Elle est typiquement appelée lorsqu'un utilisateur souhaite se déconnecter
 * de l'application afin de mettre fin à sa session active.
 */
@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getSession().invalidate();
        response.sendRedirect("login.jsp");
    }
}
