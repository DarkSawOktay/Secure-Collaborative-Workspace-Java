package models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Utilisation de variables d'environnement pour la sécurité
    // Si la variable n'existe pas, on met une valeur par défaut (pour le dev local uniquement)
    private static final String URL = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : "jdbc:mariadb://localhost:3306/ide_collaboratif";
    private static final String USER = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "ide_user";

    // Configurez la variable d'environnement DB_PASSWORD sur votre machine
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC MariaDB non trouvé", e);
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture : " + e.getMessage());
            }
        }
    }
}