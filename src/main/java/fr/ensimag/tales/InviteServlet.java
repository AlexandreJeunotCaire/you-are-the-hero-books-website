package fr.ensimag.tales;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@WebServlet(urlPatterns = {"/invite/*"})
public class InviteServlet extends HttpServlet{


    @Resource(name = "jdbc/tales")
    private DataSource dataSource;
    /**
     * Post story's visibility (open/on invite) in database
     * 
     * @param req HTTP request
     * @param resp HTTP response
     * @throws ServletException If application errors occurs
     * @throws IOException If I/O fails
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
        throws ServletException, IOException {
        
        String notAuth = "Veuillez vous authentifier pour inviter un utilisateur";
        try {
            AuthServlet.checkUser(req, resp, notAuth);
        } catch (ServletException | IOException e) {
            return;
        }

        
        String invitee = req.getParameter("user");
        String author = (String) req.getSession().getAttribute("user");
        int story = 0;

        try {
            story = ServletUtil.checkStoryId(req, resp);
        } catch(ServletException | IOException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = resp.getWriter();
            out.println("Le paragraphe n'existe pas.");
            return;
        }
        

        try (final Connection conn = dataSource.getConnection()){

            if(invitee == null || "".equals(invitee)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                PrintWriter out = resp.getWriter();
                out.println("Le champ d'invitation n'est pas rempli correctement.");
                return;
            }

            try {

                final PreparedStatement storyStmt = conn.prepareStatement("SELECT author from Story WHERE id=?");
                storyStmt.setInt(1, story);

                String notRightUser = "Vous ne pouvez inviter des utilisateurs que pour les histoires que vous avez créé.";
                try {
                    AuthServlet.checkUser(storyStmt.executeQuery().getString(0), req, resp, notRightUser);
    
                } catch(ServletException | IOException e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    PrintWriter out = resp.getWriter();
                    out.println(notRightUser);
                    return;
                }

                final PreparedStatement stmt = conn.prepareStatement("SELECT name FROM TalesUser WHERE name=?");
                stmt.setString(1, invitee);
                if(!stmt.executeQuery().next()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    PrintWriter out = resp.getWriter();
                    out.println("Utilisateur non trouvé.");
                    return;
                }

                final PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO Invite VALUES(?, ?, ?)");
                insertStmt.setString(1, author);
                insertStmt.setString(2, invitee);
                insertStmt.setInt(3, story);

                insertStmt.executeQuery();

            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                PrintWriter out = resp.getWriter();
                out.println("Erreur d'accès à la base de données. Détail de l'erreur :\n" + e.getLocalizedMessage());
                return;
            }
        } catch(SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = resp.getWriter();
            out.println("Erreur de connection à la base de données. Détail de l'erreur :\n" + e.getLocalizedMessage());
            return;
        }
    } 
}
