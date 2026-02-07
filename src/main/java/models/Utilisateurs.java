package models;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * La classe Utilisateurs représente un modèle pour un utilisateur dans le système.
 * Elle est utilisée pour interagir avec les données des utilisateurs dans la base
 * de données et fournir des méthodes utilitaires liées aux utilisateurs.
 *
 * Hérite de la classe DBConnection pour bénéficier des fonctionnalités de
 * connexion à la base de données.
 *
 * Fonctionnalités principales :
 * - Stocke les informations d'un utilisateur telles que l'identifiant et le nom.
 * - Fournit des méthodes getter et setter pour accéder et modifier ces informations.
 * - Implémente une méthode statique pour authentifier un utilisateur à partir
 *   de ses identifiants (login et mot de passe).
 */
public class Utilisateurs extends DBConnection{
    private int idUtilisateur;
    private String nomUtilisateur;

    /**
     * Constructeur de la classe Utilisateurs.
     * Initialise un nouvel utilisateur avec l'identifiant fourni et le nom utilisateur.
     *
     * @param idUtilisateur l'identifiant unique de l'utilisateur.
     * @param nomUtilisateur le nom de l'utilisateur.
     */
    public Utilisateurs(int idUtilisateur, String nomUtilisateur) {
        this.idUtilisateur = idUtilisateur;
        this.nomUtilisateur = nomUtilisateur;
    }

    /**
     * Retourne l'identifiant unique de l'utilisateur.
     *
     * @return l'identifiant de l'utilisateur sous forme d'un entier.
     */
    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    /**
     * Modifie l'identifiant unique de l'utilisateur.
     *
     * @param idUtilisateur l'identifiant unique de l'utilisateur à définir.
     */
    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    /**
     * Retourne le nom de l'utilisateur.
     *
     * @return le nom de l'utilisateur sous forme de chaîne de caractères.
     */
    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    /**
     * Modifie le nom de l'utilisateur.
     *
     * @param nomUtilisateur le nouveau nom de l'utilisateur à définir.
     */
    public void setNomUtilisateur(String nomUtilisateur) {
        this.nomUtilisateur = nomUtilisateur;
    }

    /**
     * Authentifie un utilisateur en vérifiant ses identifiants (login et mot de passe)
     * dans la base de données. Si les identifiants sont corrects, retourne une instance
     * de la classe Utilisateurs correspondant à l'utilisateur authentifié.
     *
     * @param login le nom d'utilisateur fourni pour l'authentification.
     * @param password le mot de passe fourni pour l'authentification.
     * @return une instance de la classe Utilisateurs si l'authentification réussit,
     *         ou null si les identifiants sont invalides ou inexistants.
     * @throws RuntimeException en cas d'erreur SQL lors de l'interaction avec la base de données.
     */
    public static Utilisateurs authentifier(String login, String password) {
        String sql = "select * from utilisateurs where nom = ?";
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hashedPassword = rs.getString("mot_de_passe");
                if (BCrypt.checkpw(password, hashedPassword)) {
                    int idUtilisateur = rs.getInt("id");
                    String nomUtilisateur = rs.getString("nom");
                    return new Utilisateurs(idUtilisateur, nomUtilisateur);
                }
            }
            else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}

