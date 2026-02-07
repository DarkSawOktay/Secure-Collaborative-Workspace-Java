package models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Modèle représentant un message de chat entre deux utilisateurs
 */
public class ChatMessage {
    private int id;
    private int expediteurId;
    private int destinataireId;
    private String message;
    private Timestamp timestamp;
    private String expediteurNom; // Pour l'affichage

    // SQL statements
    private static final String INSERT_MESSAGE =
            "INSERT INTO chat_messages(expediteur_id, destinataire_id, message) VALUES(?, ?, ?)";
    private static final String GET_CONVERSATION =
            "SELECT cm.id, cm.expediteur_id, cm.destinataire_id, cm.message, cm.timestamp, u.nom as expediteur_nom " +
                    "FROM chat_messages cm " +
                    "JOIN utilisateurs u ON cm.expediteur_id = u.id " +
                    "WHERE (cm.expediteur_id = ? AND cm.destinataire_id = ?) " +
                    "   OR (cm.expediteur_id = ? AND cm.destinataire_id = ?) " +
                    "ORDER BY cm.timestamp ASC";

    // Constructeur pour un nouveau message
    public ChatMessage(int expediteurId, int destinataireId, String message) {
        this.expediteurId = expediteurId;
        this.destinataireId = destinataireId;
        this.message = message;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    // Constructeur pour un message récupéré de la BDD
    public ChatMessage(int id, int expediteurId, int destinataireId, String message,
                       Timestamp timestamp, String expediteurNom) {
        this.id = id;
        this.expediteurId = expediteurId;
        this.destinataireId = destinataireId;
        this.message = message;
        this.timestamp = timestamp;
        this.expediteurNom = expediteurNom;
    }

    /**
     * Enregistre un message dans la base de données
     * @return true si l'opération a réussi
     */
    public boolean save() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_MESSAGE, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, expediteurId);
            ps.setInt(2, destinataireId);
            ps.setString(3, message);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    this.id = generatedKeys.getInt(1);
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupère la conversation entre deux utilisateurs
     * @param userId1 ID du premier utilisateur
     * @param userId2 ID du deuxième utilisateur
     * @return Liste des messages échangés
     */
    public static List<ChatMessage> getConversation(int userId1, int userId2) {
        List<ChatMessage> messages = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(GET_CONVERSATION)) {

            ps.setInt(1, userId1);
            ps.setInt(2, userId2);
            ps.setInt(3, userId2);
            ps.setInt(4, userId1);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(new ChatMessage(
                            rs.getInt("id"),
                            rs.getInt("expediteur_id"),
                            rs.getInt("destinataire_id"),
                            rs.getString("message"),
                            rs.getTimestamp("timestamp"),
                            rs.getString("expediteur_nom")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messages;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public int getExpediteurId() {
        return expediteurId;
    }

    public int getDestinataireId() {
        return destinataireId;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getExpediteurNom() {
        return expediteurNom;
    }
}