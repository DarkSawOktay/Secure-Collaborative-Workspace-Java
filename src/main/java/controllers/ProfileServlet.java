package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/ProfileServlet")
public class ProfileServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            // pas connecté, redirige vers login
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        // transmet à JSP
        req.getRequestDispatcher("/WEB-INF/profile.jsp")
                .forward(req, resp);
    }
}
