package controllers;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import models.DBConnection;
import models.Utilisateurs;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;

@WebServlet("/RejectInvitationServlet")
public class RejectInvitationServlet extends HttpServlet {
    private static final String DELETE_INV =
            "DELETE FROM invitations WHERE id = ?";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Utilisateurs me = (session != null)
                ? (Utilisateurs) session.getAttribute("utilisateur") : null;
        if (me == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int invId = Integer.parseInt(req.getParameter("invitation_id"));
        JSONObject json = new JSONObject();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_INV)) {
            ps.setInt(1, invId);
            ps.executeUpdate();
            json.put("success", true);
        } catch (SQLException e) {
            json.put("success", false).put("message", "Échec du refus");
        }

        resp.setContentType("application/json");
        resp.getWriter().write(json.toString());
    }
}
