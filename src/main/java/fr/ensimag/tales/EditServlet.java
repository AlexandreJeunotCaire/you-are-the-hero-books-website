package fr.ensimag.tales;

import fr.ensimag.tales.dao.ChoiceDAO;
import fr.ensimag.tales.dao.ParagraphDAO;
import fr.ensimag.tales.dao.StoryDAO;
import fr.ensimag.tales.model.Choice;
import fr.ensimag.tales.model.Paragraph;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@WebServlet(urlPatterns = {"/edit/*"})
public class EditServlet extends HttpServlet {

    @Resource(name = "jdbc/tales")
    private DataSource dataSource;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            AuthServlet.checkUser(request, response, "Vous devez vous connecter pour proposer un paragraphe");
        } catch (ServletException e) {
            return;
        }
        final String user = (String) request.getSession().getAttribute("user");

        // Validate target paragraph
        String paragraphIdStr = request.getParameter("paragraph");
        if (paragraphIdStr == null || "".equals(paragraphIdStr)) {
            response.sendRedirect("/");
            return;
        }

        // Check target paragraph exists
        int paragraphId;
        try {
            paragraphId = Integer.parseInt(paragraphIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect("/");
            return;
        }

        final ParagraphDAO paragraphDAO = new ParagraphDAO(dataSource);

        // Delete unsaved paragraph
        final String delete = request.getParameter("delete");
        if (!(delete == null || "".equals(delete))) {
            if (paragraphId != 0) {
                final Paragraph paragraph = paragraphDAO.getParagraph(paragraphId);
                if (paragraph == null) {
                    // Paragraph not found
                    response.sendRedirect("/");
                    return;
                }
                if (paragraph.getChoices().size() > 0 || paragraph.getNext() != null) {
                    ServletUtil.fail(response, "Erreur: Impossible de supprimer ce paragraphe car il mène à d'autres branches de l'histoire.");
                    return;
                }
                paragraphDAO.deleteParagraph(paragraphId);
                paragraphDAO.unlockChoices(paragraphId);
            }
            response.sendRedirect("/");
            return;
        }

        // Validate form data
        final String text = request.getParameter("content");
        if (text == null || "".equals(text)) {
            ServletUtil.fail(response, "Erreur: Texte vide");
            return;
        }

        List<Choice> choices = null;
        Integer nextParagraphId = null;
        boolean isEnding = false;

        final String mode = request.getParameter("mode");
        if ("choices".equals(mode)) { // New choices
            choices = new ArrayList<Choice>();
            String[] title = request.getParameterValues("title");
            String[] condStr = request.getParameterValues("cond");
            if (title == null || condStr == null) {
                ServletUtil.fail(response, "Erreur: Condition invalide");
                return;
            } else if (title.length != condStr.length) {
                ServletUtil.fail(response, "Erreur: Condition invalide");
                return;
            }
            try {
                for (int i = 0; i < condStr.length; ++i) {
                    final int cond = Integer.parseInt(condStr[i]);
                    if (cond != 0) {
                        if (paragraphDAO.getParagraph(cond) == null) {
                            throw new ServletException("Paragraphe introuvable!");
                        }
                    }
                    if (!"".equals(title[i])) {
                        choices.add(new Choice(title[i], 0, cond));

                    }
                }
            } catch (NumberFormatException e) {
                ServletUtil.fail(response, "Erreur: Condition invalide");
                return;
            } catch (ServletException e) {
                ServletUtil.fail(response, "Erreur: Le paragraphe indiqué comme condition n'existe pas.");
                return;
            }
        } else if ("next".equals(mode)) { // Story converges
            String next = request.getParameter("next");
            if (next == null || "".equals(next)) {
                ServletUtil.fail(response, "Erreur: Paragraphe suivant invalide.");
                return;
            }
            try {
                nextParagraphId = Integer.parseInt(next);
            } catch (NumberFormatException e) {
                ServletUtil.fail(response, "Erreur: Paragraphe suivant invalide.");
                return;
            }

            if (paragraphDAO.getParagraph(nextParagraphId) == null) {
                ServletUtil.fail(response, "Erreur: Le paragraphe suivant demandé n'existe pas.");
            }

            if (paragraphId != 0) {
                final Paragraph paragraph = paragraphDAO.getParagraph(paragraphId);
                if (paragraph.getChoices().size() > 0) {
                    ServletUtil.fail(response, "Erreur: Le paragraphe cible a des choix disponibles. Impossible de les supprimer pour faire converger l'histoire. Recommencez depuis l'une des branches de choix.");
                    return;
                }
            }
        } else if ("ending".equals(mode)) {
            if (paragraphId != 0) {
                final Paragraph paragraph = paragraphDAO.getParagraph(paragraphId);
                if (paragraph.getChoices().size() > 0) {
                    ServletUtil.fail(response, "Erreur: Le paragraphe cible a des choix disponibles. Impossible de les supprimer pour terminer l'histoire. Recommencez depuis l'une des branches de choix.");
                    return;
                }
            }
            isEnding = true;
        } else { // Unknown mode
            response.sendRedirect("/");
            return;
        }

        // All user data is valid
        // Proceed with paragraph creation/update
        if (paragraphId == 0) {
            // New story
            paragraphId = paragraphDAO.createParagraph(user);

            final StoryDAO storyDAO = new StoryDAO(dataSource);
            final int storyId = storyDAO.createStory(user, paragraphId);
            storyDAO.addAuthor(storyId, user);
        }

        final Paragraph paragraph = paragraphDAO.getParagraph(paragraphId);
        if (paragraph.getAuthor().equals(user)) {
            paragraphDAO.setText(paragraphId, text);
        }

        if (choices != null) {
            paragraphDAO.addChoices(paragraphId, choices);
        } else if (nextParagraphId != null) {
            paragraphDAO.setNext(paragraphId, nextParagraphId);
            paragraphDAO.makeParent(paragraphId, nextParagraphId);
        } else { // isEnding == true
            paragraphDAO.makeEnding(paragraphId);
        }

        // Unlock choices
        paragraphDAO.unlockChoices(paragraphId);

        // Add user to story authors
        final Paragraph firstParagraph = paragraphDAO.getFirstParagraph(paragraphId);
        if (firstParagraph == null) {
            // Paragraph not found
            response.sendRedirect("/edit/" + paragraphId);
            return;
        }
        final StoryDAO storyDAO = new StoryDAO(dataSource);
        final int storyId = storyDAO.getStoryWithFirstParagraph(firstParagraph.getId());
        if (storyId == 0) {
            // Story not found
            response.sendRedirect("/edit/" + paragraphId);
            return;
        }
        storyDAO.addAuthor(storyId, user);

        response.sendRedirect("/edit/" + paragraphId);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            AuthServlet.checkUser(request, response, "Vous devez vous connecter pour proposer un paragraphe");
        } catch (ServletException e) {
            return;
        }

        final HttpSession session = request.getSession();
        final String user = (String) session.getAttribute("user");

        String uri = request.getRequestURI();
        boolean isNewParagraph = uri.startsWith("/edit/new");
        uri = uri.substring(uri.lastIndexOf("/") + 1);
        if (isNewParagraph) {
            try {
                final int choiceId = Integer.parseInt(uri);
                final ChoiceDAO choiceDAO = new ChoiceDAO(dataSource);
                final Choice choice = choiceDAO.getChoice(choiceId);
                if (choice == null) {
                    response.sendRedirect("/");
                    return;
                }
                if (choice.getEditor() == null) {
                    System.out.println("OK");
                } else {
                    if (!choice.getEditor().equals(user)) {
                        // Another user is currently editing the choice
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        final PrintWriter writer = response.getWriter();
                        writer.println(String.format("Erreur: %s édite déjà ce paragraphe.", choice.getEditor()));
                        return;
                    } else {
                        // Current user is already editing the choice
                        response.sendRedirect("/edit/" + choice.getDestination());
                        return;
                    }
                }

                choiceDAO.lockChoice(choiceId, user);

                final ParagraphDAO paragraphDAO = new ParagraphDAO(dataSource);
                final int paragraphId = paragraphDAO.createParagraph(user);
                paragraphDAO.makeParent(choice.getSource(), paragraphId);
                choiceDAO.setDestination(choiceId, paragraphId);
                response.sendRedirect("/edit/" + paragraphId);
                return;
            } catch (NumberFormatException e) {
                response.sendRedirect("/");
                return;
            }
        }

        Paragraph paragraph;
        Set<Paragraph> condTargets;
        Set<Paragraph> nextTargets;
        try {
            final int paragraphId = Integer.parseInt(uri);
            final ParagraphDAO paragraphDAO = new ParagraphDAO(dataSource);
            paragraph = paragraphDAO.getParagraph(paragraphId);
            if (paragraph == null) {
                // Paragraph not found
                response.sendRedirect("/edit");
                response.sendRedirect(request.getContextPath() + "/edit");
            }
            condTargets = paragraphDAO.getCondTargets(paragraphId);
            nextTargets = paragraphDAO.getNextTargets(paragraphId);
        } catch (NumberFormatException e) {
            // New paragraph
            final List<Choice> choices = new ArrayList<Choice>();
            paragraph = new Paragraph(0, "", "", null, false, choices);
            condTargets = new HashSet<Paragraph>();
            nextTargets = new HashSet<Paragraph>();
        }

        request.setAttribute("paragraph", paragraph);
        request.setAttribute("condTargets", condTargets);
        request.setAttribute("nextTargets", nextTargets);

        request.getRequestDispatcher("/WEB-INF/write.jsp").forward(request, response);
    }

}
