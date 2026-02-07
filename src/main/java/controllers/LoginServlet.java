package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import models.Utilisateurs;

import java.io.IOException;

/**
 * LoginServlet est un servlet permettant de gérer les opérations d'authentification des utilisateurs.
 * Il prend en charge les requêtes HTTP GET et POST pour fournir respectivement l'accès
 * à la page de connexion ou traiter les identifiants entrés par l'utilisateur.
 *
 * Fonctionnalités principales :
 * - Gère la navigation entre la page de connexion et la page principale en fonction
 *   de l'état authentifié de l'utilisateur en session.
 * - Traite les informations d'authentification fournies par l'utilisateur.
 * - Vérifie les informations d'identification (nom d'utilisateur et mot de passe)
 *   contre les données persistées via `Utilisateurs.authentifier`.
 * - Gère les erreurs d'authentification avec des messages flash stockés en session.
 * - Différencie les chemins lorsqu'un utilisateur est authentifié ou non.
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Utilisateurs utilisateurs = (Utilisateurs) session.getAttribute("utilisateur");

        if (utilisateurs == null) {
            request.getRequestDispatcher("login.jsp").forward(request, response);
        } else {
            response.sendRedirect("WEB-INF/index.jsp");
        }
    }

    /**
     * Gère les requêtes HTTP POST pour traiter les informations d'identification saisies par l'utilisateur.
     * Cette méthode vérifie les informations fournies par l'utilisateur (nom d'utilisateur et mot de passe)
     * et détermine si l'authentification est réussie ou non.
     * En cas de succès, l'utilisateur est redirigé vers la page principale. Sinon, un message d'erreur est affiché.
     *
     * @param request  L'objet HttpServletRequest contenant les informations de la requête HTTP,
     *                 y compris les paramètres "username" et "password" fournis par l'utilisateur.
     * @param response L'objet HttpServletResponse permettant de formuler la réponse HTTP correspondante,
     *                 telle qu'une redirection ou un affichage de page.
     *
     * @throws ServletException Si une erreur spécifique aux servlets survient pendant le traitement.
     * @throws IOException      Si une erreur d'entrée/sortie intervient lors du traitement de la requête ou de la réponse.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        String username = request.getParameter("username").trim();
        String password = request.getParameter("password").trim();

        try {
            Utilisateurs utilisateurs = Utilisateurs.authentifier(username, password);

            if (utilisateurs == null) {
                session.setAttribute("flash", "Mauvais identifiant ou mot de passe.");
                session.setAttribute("flashType", "error");
                response.sendRedirect("login.jsp");

            } else {
                session.setAttribute("utilisateur", utilisateurs);
                request.getRequestDispatcher("WEB-INF/index.jsp").forward(request, response);
            }
        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'authentification", e);
        }
    }
}
