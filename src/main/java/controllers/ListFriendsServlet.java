// controllers/ListFriendsServlet.java
package controllers;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.sql.*;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import listener.OnlineUsersListener;
import models.DBConnection;
import models.Utilisateurs;

@WebServlet("/ListFriendsServlet")
public class ListFriendsServlet extends HttpServlet {
    private static final String SQL =
            "SELECT u.id, u.nom, u.email FROM amis a " +
                    "JOIN utilisateurs u ON (u.id = a.user_id_1 AND a.user_id_2 = ?) " +
                    "   OR (u.id = a.user_id_2 AND a.user_id_1 = ?)";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session==null || session.getAttribute("utilisateur")==null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Utilisateurs me = (Utilisateurs) session.getAttribute("utilisateur");
        int myId = me.getIdUtilisateur();
        Set<Integer> online = OnlineUsersListener.getOnlineUsers();

        JSONArray arr = new JSONArray();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, myId);
            ps.setInt(2, myId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                JSONObject f = new JSONObject();
                int id = rs.getInt("id");
                f.put("id", id);
                f.put("nom", rs.getString("nom"));
                f.put("email", rs.getString("email"));
                f.put("online", online.contains(id));
                arr.put(f);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(arr.toString());
    }
}
