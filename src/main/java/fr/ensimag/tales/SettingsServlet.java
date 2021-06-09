package fr.ensimag.tales;

import fr.ensimag.tales.dao.StoryDAO;
import fr.ensimag.tales.model.Story;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = {"/settings/*"})
public class SettingsServlet extends HttpServlet {
    @Resource(name="jdbc/tales")
    private DataSource dataSource;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            AuthServlet.checkUser(request, response, "Merci de vous identifier pour publier.");
            final int storyId = ServletUtil.checkStoryId(request, response);

            final StoryDAO storyDAO = new StoryDAO(dataSource);
            final Story story = storyDAO.getStory(storyId);
            if (story == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                final PrintWriter out = response.getWriter();
                out.println("Erreur: L'histoire demandée n'existe pas.");
                return;
            }
            AuthServlet.checkUser(story.getAuthor(), request, response, "Erreur: Vous n'êtes pas l'auteur de cette histoire. Seul l'auteur du premier paragraphe peut gérer les paramètres de cette histoire.");

            String visibility = request.getParameter("visibility");
            if ("public".equals(visibility)) {
                storyDAO.setVisibility(storyId, Story.Visibility.PUBLIC);
            } else if ("oninvite".equals(visibility)) {
                storyDAO.setVisibility(storyId, Story.Visibility.ONINVITE);
            } else {
                final PrintWriter out = response.getWriter();
                out.println("Erreur: Le mode d'accès spécifié est invalide.");
                return;
            }
            response.sendRedirect("/user");
        } catch (ServletException ignored) {
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
