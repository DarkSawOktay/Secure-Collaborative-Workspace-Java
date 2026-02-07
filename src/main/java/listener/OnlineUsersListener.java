package listener;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@WebListener
public class OnlineUsersListener implements HttpSessionListener, HttpSessionAttributeListener {
    // Set global thread-safe
    private static final Set<Integer> online = Collections.synchronizedSet(new HashSet<>());

    // Quand on fait session.setAttribute("utilisateur", Utilisateurs)
    @Override
    public void attributeAdded(HttpSessionBindingEvent e) {
        if ("utilisateur".equals(e.getName())) {
            Integer id = ((models.Utilisateurs)e.getValue()).getIdUtilisateur();
            online.add(id);
        }
    }
    @Override
    public void attributeRemoved(HttpSessionBindingEvent e) {
        if ("utilisateur".equals(e.getName())) {
            Integer id = ((models.Utilisateurs)e.getValue()).getIdUtilisateur();
            online.remove(id);
        }
    }
    // nettoie à la destruction de session
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession s = se.getSession();
        Object o = s.getAttribute("utilisateur");
        if (o instanceof models.Utilisateurs) {
            online.remove(((models.Utilisateurs)o).getIdUtilisateur());
        }
    }

    public static Set<Integer> getOnlineUsers() {
        return online;
    }
}
