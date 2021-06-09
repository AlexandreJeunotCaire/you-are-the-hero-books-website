package fr.ensimag.tales;

import at.favre.lib.crypto.bcrypt.BCrypt;
import fr.ensimag.tales.dao.ChoiceDAO;
import fr.ensimag.tales.model.Choice;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

@WebServlet(urlPatterns = {"/login", "/signup"})
public class AuthServlet extends HttpServlet {
    private static final String EMAIL_REGEX = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])";

    @Resource(name = "jdbc/tales")
    private DataSource dataSource;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getRequestURI().contains("login")) {
            resp.sendRedirect(req.getContextPath() + "/");
        } else { // signup
            req.getRequestDispatcher("/WEB-INF/signup.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (final Connection conn = dataSource.getConnection()) {
            final String username = req.getParameter("user");
            final String password = req.getParameter("pass");
            if (username == null || "".equals(username) || password == null || "".equals(password)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            if (req.getRequestURI().contains("login")) {
                try {
                    final PreparedStatement stmt = conn.prepareStatement("SELECT hash FROM TalesUser WHERE name=?");
                    stmt.setString(1, username);
                    ResultSet result = stmt.executeQuery();
                    if (result.next()) {
                        String hash = result.getString("hash");
                        BCrypt.Result res = BCrypt.verifyer().verify(password.toCharArray(), hash.toCharArray());
                        final HttpSession session = req.getSession();
                        if (res.verified) {
                            session.setAttribute("user", username);

                            // Fill session with previous choices
                            final ChoiceDAO choiceDAO = new ChoiceDAO(dataSource);
                            final Map<Integer, Choice> choices = choiceDAO.getChoices(username);
                            for (Map.Entry<Integer, Choice> choice : choices.entrySet()) {
                                final Choice c = choice.getValue();
                                session.setAttribute("choice" + choice.getKey(), choice.getValue());
                                session.setAttribute("previous" + c.getDestination(), c.getSource());
                            }
                        } else {
                            session.setAttribute("error", "Identifiant ou mot de passe incorrect.");
                        }
                    }
                } catch (SQLException e) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else if (req.getRequestURI().contains("signup")) {
                final String mail = req.getParameter("mail");
                if (mail == null || !mail.matches(EMAIL_REGEX)) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Adresse E-mail invalide");
                    return;
                }
                try {
                    final PreparedStatement stmt = conn.prepareStatement("INSERT INTO TalesUser VALUES (?, ?, ?)");
                    stmt.setString(1, username);
                    stmt.setString(2, mail);
                    stmt.setString(3, BCrypt.withDefaults().hashToString(10, password.toCharArray()));
                    stmt.execute();
                    resp.sendRedirect(req.getContextPath() + "/");
                } catch (SQLIntegrityConstraintViolationException e) {
                    // Username already taken, redirect to home page
                    resp.sendRedirect(req.getContextPath() + "/");
                } catch (SQLException e) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        resp.sendRedirect(req.getContextPath() + "/");
    }

    /**
     * Checks for an authenticated user.
     *
     * @param request HTTP Request
     * @param response HTTP Response
     * @param errorMessage Error message to display to an unauthenticated user
     * @throws ServletException On unauthenticated user
     * @throws IOException If I/O fails
     */
    public static void checkUser(HttpServletRequest request, HttpServletResponse response, String errorMessage) throws ServletException, IOException {
        final HttpSession session = request.getSession();
        if (session.getAttribute("user") == null) {
            session.setAttribute("error", errorMessage);
            response.sendRedirect(request.getContextPath() + "/");
            throw new ServletException();
        }
    }

    /**
     * Checks for a specific authentificated user.
     *
     * @param user Target user
     * @param request HTTP Request
     * @param response HTTP Response
     * @param errorMessage Error message displayed to any other user
     * @throws ServletException On other user or unauthenticated
     * @throws IOException If I/O fails
     */
    public static void checkUser(String user, HttpServletRequest request, HttpServletResponse response, String errorMessage) throws ServletException, IOException {
        final HttpSession session = request.getSession();
        if (!user.equals(session.getAttribute("user"))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            final PrintWriter out = response.getWriter();
            out.println(errorMessage);
            throw new ServletException();
        }
    }
}
