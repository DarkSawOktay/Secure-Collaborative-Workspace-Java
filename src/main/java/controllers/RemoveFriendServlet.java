package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import models.DBConnection;
import models.Utilisateurs;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet("/RemoveFriendServlet")
public class RemoveFriendServlet extends HttpServlet {
    private static final String DELETE_SQL =
            "DELETE FROM amis WHERE user_id_1 = ? AND user_id_2 = ?";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Utilisateurs me = (Utilisateurs) session.getAttribute("utilisateur");
        int myId = me.getIdUtilisateur();
        int otherId;
        try {
            otherId = Integer.parseInt(req.getParameter("userId"));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (otherId == myId) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int uid1 = Math.min(myId, otherId);
        int uid2 = Math.max(myId, otherId);

        boolean success = false;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, uid1);
            ps.setInt(2, uid2);
            success = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ServletException("Erreur suppression ami", e);
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        JSONObject json = new JSONObject();
        json.put("success", success);
        resp.getWriter().write(json.toString());
    }
}
