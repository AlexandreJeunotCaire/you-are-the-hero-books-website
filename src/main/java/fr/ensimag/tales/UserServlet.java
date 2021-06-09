package fr.ensimag.tales;

import fr.ensimag.tales.dao.StoryDAO;
import fr.ensimag.tales.model.Story;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/user/*"})
public class UserServlet extends HttpServlet {

    @Resource(name = "jdbc/tales")
    private DataSource dataSource;

    /**
     * Display user profile page. If no user is supplied, the currently logged-in user profile is displayed.
     *
     * @param req HTTP Request
     * @param resp HTTP Response
     * @throws ServletException If application error occurs
     * @throws IOException If I/O fails
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String uri = req.getRequestURI();
        final int usernameStartIndex = uri.indexOf("/", 1) + 1;
        String username;
        if (usernameStartIndex > 0) {
           username = uri.substring(usernameStartIndex);
        } else {
            // No supplied username
            final HttpSession session = req.getSession();
            username = (String)session.getAttribute("user");
            if (username == null) {
                // User not found
                resp.sendRedirect("/");
                return;
            }
        }

        final StoryDAO storyDAO = new StoryDAO(dataSource);
        final List<Story> stories = storyDAO.getStoriesBy(username);

        req.setAttribute("user", username);
        req.setAttribute("stories", stories);
        req.getRequestDispatcher("/WEB-INF/user.jsp").forward(req, resp);
    }

}
