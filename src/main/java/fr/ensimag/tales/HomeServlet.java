package fr.ensimag.tales;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import fr.ensimag.tales.dao.StoryDAO;
import fr.ensimag.tales.model.Story;

/**
 * Home page main servlet.
 */
@WebServlet(urlPatterns = {"/home"})
public class HomeServlet extends HttpServlet {
    @Resource(name = "jdbc/tales")
    private DataSource dataSource;

    /**
     * Retrieve and present a list of stories from the database.
     * @param request HTTP Request
     * @param response HTTP Response
     * @throws ServletException If a problem occurs
     * @throws IOException If server I/O fails
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final HttpSession session = request.getSession();
        final StoryDAO storyDAO = new StoryDAO(dataSource);
        List<Story> stories;
        if (session.getAttribute("user") != null) {
            // Authenticated users may browse all stories
            stories = storyDAO.getStories();
        } else {
            // Anonymous users may only browse published stories
            stories = storyDAO.getPublishedStories();
        }

        request.setAttribute("stories", stories);
        request.getRequestDispatcher("/WEB-INF/index.jsp").forward(request, response);
    }
}
