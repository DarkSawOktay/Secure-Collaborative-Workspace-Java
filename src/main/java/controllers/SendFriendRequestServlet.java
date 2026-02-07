package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import models.DBConnection;
import models.Utilisateurs;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;

@WebServlet("/SendFriendRequestServlet")
public class SendFriendRequestServlet extends HttpServlet {
    private static final String FIND_USER_SQL =
            "SELECT id, nom, email FROM utilisateurs WHERE nom = ?";

    private static final String CHECK_EXISTING_FRIENDSHIP =
            "SELECT * FROM amis WHERE (user_id_1 = ? AND user_id_2 = ?) OR (user_id_1 = ? AND user_id_2 = ?)";

    private static final String CHECK_EXISTING_REQUEST =
            "SELECT * FROM demande_ami WHERE (expediteur_id = ? AND destinataire_id = ?)";

    private static final String CHECK_INCOMING_REQUEST =
            "SELECT * FROM demande_ami WHERE (expediteur_id = ? AND destinataire_id = ?)";

    private static final String INSERT_REQUEST_SQL =
            "INSERT INTO demande_ami(expediteur_id, destinataire_id, statut, date_demande) VALUES(?, ?, 'EN_ATTENTE', NOW())";

    private static final String INSERT_NOTIFICATION_SQL =
            "INSERT INTO notifications(utilisateur_id, message, type, lu, date_creation) VALUES(?, ?, 'DEMANDE_AMI', false, NOW())";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        JSONObject json = new JSONObject();

        // Vérification de l'authentification
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            json.put("success", false).put("message", "Non authentifié");
            resp.getWriter().write(json.toString());
            return;
        }
        Utilisateurs me = (Utilisateurs) session.getAttribute("utilisateur");
        int myId = me.getIdUtilisateur();

        // Récupération et validation du pseudo
        String pseudo = req.getParameter("pseudo");
        if (pseudo == null || pseudo.isBlank() || pseudo.length() > 50) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            json.put("success", false).put("message", "Pseudo invalide ou manquant");
            resp.getWriter().write(json.toString());
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1) Trouver l'utilisateur destinataire par pseudo
                int destinataireId;
                String destinataireNom;
                String destinataireEmail;

                try (PreparedStatement ps = conn.prepareStatement(FIND_USER_SQL)) {
                    ps.setString(1, pseudo);
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        json.put("success", false)
                                .put("message", "Utilisateur introuvable");
                        resp.getWriter().write(json.toString());
                        return;
                    }
                    destinataireId = rs.getInt("id");
                    destinataireNom = rs.getString("nom");
                    destinataireEmail = rs.getString("email");
                }

                // 2) Vérifier qu'on ne s'ajoute pas soi-même
                if (destinataireId == myId) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    json.put("success", false)
                            .put("message", "Vous ne pouvez pas vous envoyer une demande d'ami");
                    resp.getWriter().write(json.toString());
                    return;
                }

                // 3) Vérifier si une relation d'amitié existe déjà
                try (PreparedStatement ps = conn.prepareStatement(CHECK_EXISTING_FRIENDSHIP)) {
                    ps.setInt(1, Math.min(myId, destinataireId));
                    ps.setInt(2, Math.max(myId, destinataireId));
                    ps.setInt(3, Math.max(myId, destinataireId));
                    ps.setInt(4, Math.min(myId, destinataireId));
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        json.put("success", false)
                                .put("message", "Vous êtes déjà ami avec " + destinataireNom);
                        resp.getWriter().write(json.toString());
                        return;
                    }
                }

                // 4) Vérifier si une demande sortante existe déjà
                try (PreparedStatement ps = conn.prepareStatement(CHECK_EXISTING_REQUEST)) {
                    ps.setInt(1, myId);
                    ps.setInt(2, destinataireId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        json.put("success", false)
                                .put("message", "Vous avez déjà envoyé une demande d'ami à " + destinataireNom);
                        resp.getWriter().write(json.toString());
                        return;
                    }
                }

                // 5) Vérifier si une demande entrante existe déjà
                try (PreparedStatement ps = conn.prepareStatement(CHECK_INCOMING_REQUEST)) {
                    ps.setInt(1, destinataireId);
                    ps.setInt(2, myId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        json.put("success", false)
                                .put("message", destinataireNom + " vous a déjà envoyé une demande d'ami. Consultez vos notifications.");
                        resp.getWriter().write(json.toString());
                        return;
                    }
                }

                // 6) Insérer la demande d'ami
                try (PreparedStatement ps = conn.prepareStatement(INSERT_REQUEST_SQL)) {
                    ps.setInt(1, myId);
                    ps.setInt(2, destinataireId);
                    ps.executeUpdate();
                }

                // 7) Créer une notification pour le destinataire
                try (PreparedStatement ps = conn.prepareStatement(INSERT_NOTIFICATION_SQL)) {
                    ps.setInt(1, destinataireId);
                    ps.setString(2, me.getNomUtilisateur() + " vous a envoyé une demande d'ami");
                    ps.executeUpdate();
                }

                // 8) Enregistrer les actions et validation
                conn.commit();
                getServletContext().log("Demande d'ami envoyée par " + myId + " à " + destinataireId);

                // 9) Renvoyer succès avec informations
                JSONObject friendInfo = new JSONObject();
                friendInfo.put("id", destinataireId);
                friendInfo.put("nom", destinataireNom);
                friendInfo.put("email", destinataireEmail);

                json.put("success", true)
                        .put("message", "Demande d'ami envoyée à " + destinataireNom)
                        .put("destinataire", friendInfo);

                resp.getWriter().write(json.toString());

            } catch (SQLException e) {
                conn.rollback();
                getServletContext().log("Erreur SQL dans SendFriendRequestServlet", e);

                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                json.put("success", false)
                        .put("message", "Erreur lors de l'envoi de la demande d'ami");
                resp.getWriter().write(json.toString());
            } finally {
                conn.setAutoCommit(true); // Restaurer l'état par défaut
            }
        } catch (SQLException e) {
            throw new ServletException("Erreur de connexion à la BD", e);
        }
    }
}