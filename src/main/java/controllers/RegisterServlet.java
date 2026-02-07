package controllers;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import models.DBConnection;
import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
    private static final String INSERT_USER_QUERY = "INSERT INTO utilisateurs (email, nom, mot_de_passe) VALUES (?, ?, ?)";
    private static final String LOGIN_PAGE = "login.jsp";
    private static final String REGISTER_PAGE = "register.jsp";
    
    private static final String MSG_INSCRIPTION_REUSSIE = "Inscription effectuée, reconnectez vous";
    private static final String MSG_UTILISATEUR_EXISTANT = "Utilisateur déjà existant";
    private static final String MSG_DOUBLON = "Ce nom d'utilisateur ou email est déjà utilisé.";
    private static final String MSG_ERREUR_INTERNE = "Erreur interne. Veuillez réessayer.";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        UserRegistrationData userData = extractUserData(request);

        try {
            if (registerUser(userData)) {
                setFlashMessage(session, MSG_INSCRIPTION_REUSSIE, "success");
                response.sendRedirect(LOGIN_PAGE);
            } else {
                setFlashMessage(session, MSG_UTILISATEUR_EXISTANT, "error");
                response.sendRedirect(REGISTER_PAGE);
            }
        } catch (SQLException e) {
            handleRegistrationError(session, e);
            response.sendRedirect(REGISTER_PAGE);
        }
    }

    private UserRegistrationData extractUserData(HttpServletRequest request) {
        return new UserRegistrationData(
            request.getParameter("username"),
            request.getParameter("email"),
            request.getParameter("password")
        );
    }

    private boolean registerUser(UserRegistrationData userData) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_USER_QUERY)) {
            
            stmt.setString(1, userData.email);
            stmt.setString(2, userData.username);
            stmt.setString(3, BCrypt.hashpw(userData.password, BCrypt.gensalt()));

            return stmt.executeUpdate() > 0;
        }
    }

    private void handleRegistrationError(HttpSession session, SQLException e) {
        e.printStackTrace();
        String message = e.getMessage().contains("Duplicate") ? MSG_DOUBLON : MSG_ERREUR_INTERNE;
        setFlashMessage(session, message, "error");
    }

    private void setFlashMessage(HttpSession session, String message, String type) {
        session.setAttribute("flash", message);
        session.setAttribute("flashType", type);
    }

    private static class UserRegistrationData {
        final String username;
        final String email;
        final String password;

        UserRegistrationData(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
        }
    }
}