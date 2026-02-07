package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import models.DBConnection;
import models.Utilisateurs;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;

@WebServlet("/ListInvitationsServlet")
public class ListInvitationsServlet extends HttpServlet {
    private static final String SQL =
            "SELECT i.id, i.fichier_id, f.nom AS fichier_nom, " +
                    "       u.id AS inviter_id, u.nom AS inviter_nom, " +
                    "       i.role, i.date_invite " +
                    "FROM invitations i " +
                    "JOIN fichiers f ON f.id = i.fichier_id " +
                    "JOIN utilisateurs u ON u.id = i.inviter_id " +
                    "WHERE i.destinataire_id = ? " +
                    "ORDER BY i.date_invite DESC";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Utilisateurs me = (session != null)
                ? (Utilisateurs) session.getAttribute("utilisateur") : null;
        if (me == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        int myId = me.getIdUtilisateur();

        JSONArray arr = new JSONArray();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {

            ps.setInt(1, myId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JSONObject inv = new JSONObject();
                    inv.put("invitation_id", rs.getInt("id"));
                    inv.put("fichier_id",    rs.getInt("fichier_id"));
                    inv.put("fichier_nom",   rs.getString("fichier_nom"));
                    inv.put("inviter_id",    rs.getInt("inviter_id"));
                    inv.put("inviter_nom",   rs.getString("inviter_nom"));
                    inv.put("role",          rs.getString("role"));
                    inv.put("date_invite",   rs.getTimestamp("date_invite").toString());
                    arr.put(inv);
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Erreur liste invitations", e);
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(arr.toString());
    }
}
