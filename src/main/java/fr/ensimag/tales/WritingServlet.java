package fr.ensimag.tales;

import fr.ensimag.tales.dao.ParagraphDAO;
import fr.ensimag.tales.dao.StoryDAO;
import fr.ensimag.tales.model.Paragraph;
import fr.ensimag.tales.model.Story;

import java.io.PrintWriter;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.sql.DataSource;

/**
 * Main story servlet. This servlet handles story progression and serves story paragraphs to the users.
 */
@WebServlet(urlPatterns = {"/new_paragraph"})
public class WritingServlet extends HttpServlet {
    private static final long serialVersionUID = -156384635132165L;

    @Resource(name = "jdbc/tales")
    private DataSource dataSource;

    /**
     * Send the form to User
     *
     * @param request  HTTP Request
     * @param response HTTP Response
     * @throws ServletException    When application error occurs
     * @throws java.io.IOException If input/output fails
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, java.io.IOException {

        request.getRequestDispatcher("/WEB-INF/write.jsp").forward(request, response);
    }

    /**
     * Get the response from User
     *
     * @param request  HTTP Request
     * @param response HTTP Response
     * @throws ServletException    When application error occurs
     * @throws java.io.IOException If input/output fails
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, java.io.IOException {
        final PrintWriter writer = response.getWriter();
        writer.println("Hello, world! Why are you POSTing here?");
    }
}
