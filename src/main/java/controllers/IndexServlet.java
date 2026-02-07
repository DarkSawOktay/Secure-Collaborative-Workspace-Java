package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * Le servlet IndexServlet est conçu pour gérer les requêtes GET vers la page principale de l'application.
 * Si l'utilisateur est authentifié, il est redirigé vers la page principale protégée.
 * Si l'utilisateur n'est pas connecté, il est redirigé vers la page de connexion.
 *
 * Fonctionnalités principales :
 * - Vérifie l'existence d'une session utilisateur.
 * - Valide la présence d'un utilisateur dans la session en cours.
 * - Redirection conditionnelle vers les ressources appropriées.
 */
@WebServlet("/IndexServlet")
public class IndexServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        // Vérifie si l'utilisateur est connecté
        if (session != null && session.getAttribute("utilisateur") != null) {
            request.getRequestDispatcher("WEB-INF/index.jsp").forward(request, response);
        } else {
            response.sendRedirect("login.jsp");
        }
    }
}
