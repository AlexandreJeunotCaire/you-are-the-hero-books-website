package fr.ensimag.tales;

import fr.ensimag.tales.dao.ChoiceDAO;
import fr.ensimag.tales.dao.ParagraphDAO;
import fr.ensimag.tales.dao.StoryDAO;
import fr.ensimag.tales.model.Choice;
import fr.ensimag.tales.model.Paragraph;
import fr.ensimag.tales.model.Story;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Main story servlet. This servlet handles story progression and serves story paragraphs to the users.
 */
@WebServlet(urlPatterns = {"/story/*"})
public class StoryServlet extends HttpServlet {
    private static final long serialVersionUID = -156384635132165L;

    @Resource(name = "jdbc/tales")
    private DataSource dataSource;

    private HttpSession session;

    /**
     * Retrieve a paragraph from the database.
     *
     * @param request  HTTP Request
     * @param response HTTP Response
     * @throws ServletException    When application error occurs
     * @throws java.io.IOException If input/output fails
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, java.io.IOException {
        session = request.getSession();

        int storyId = 0;
        try {
            storyId = ServletUtil.checkStoryId(request, response);
        } catch (ServletException e) {
            return;
        }
        final String back = request.getParameter("back");
        if (back != null) {
            int targetParagraphId = 0;
            try {
                // Reach specified paragraph
                targetParagraphId = Integer.parseInt(back);
            } catch (NumberFormatException e) {
                // Go back one paragraph
                Integer currentParagraphId = (Integer) session.getAttribute("current" + storyId);
                if (currentParagraphId == null) {
                    response.sendRedirect("/");
                    return;
                }

                Integer previousParagraphId = (Integer) session.getAttribute("previous" + currentParagraphId);
                if (previousParagraphId == null) {
                    // Going 'back' from the first paragraph: return to home page
                    response.sendRedirect("/");
                    return;
                }
                targetParagraphId = previousParagraphId;
            }

            // Highlight previously chosen option
            final Choice previousChoiceId = (Choice) session.getAttribute("choice" + targetParagraphId);
            if (previousChoiceId != null) {
                session.setAttribute("current" + storyId, targetParagraphId);
                response.addCookie(new Cookie("choice", String.valueOf(previousChoiceId.getId())));
            } else {
                response.sendRedirect("/story/" + storyId);
                return;
            }
        } else {
            response.addCookie(new Cookie("choice", ""));
        }
        processRequest(storyId, request, response);
    }

    /**
     * Record user progression and move to next paragraph.
     *
     * @param request  HTTP Request
     * @param response HTTP Response
     * @throws ServletException    When application error occurs
     * @throws java.io.IOException If input/output fails
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, java.io.IOException {
        session = request.getSession();

        int storyId = 0;
        try {
            storyId = ServletUtil.checkStoryId(request, response);
        } catch (ServletException e) {
            return;
        }

        final String choice = request.getParameter("choice");
        int choiceId = 0;
        try {
            choiceId = Integer.parseInt(choice);
        } catch (NumberFormatException e) {
            // Invalid request, redirect to last paragraph
            response.sendRedirect(request.getRequestURI());
            return;
        }

        final ChoiceDAO choiceDAO = new ChoiceDAO(dataSource);
        final Choice selected = choiceDAO.getChoice(choiceId);
        if (selected == null) {
            // Choice not found, return to paragraph
            response.sendRedirect(request.getRequestURI());
            return;
        }

        // Clear any earlier progression
        clearProgression(selected.getSource(), request);

        // Save user progression
        session.setAttribute("choice" + selected.getSource(), selected);
        session.setAttribute("current" + storyId, selected.getDestination());
        session.setAttribute("previous" + selected.getDestination(), selected.getSource());
        final String user = (String)session.getAttribute("user");
        if (user != null) {
            choiceDAO.save(user, selected.getSource(), selected.getId());
        }

        processRequest(storyId, request, response);
    }

    private void processRequest(int storyId, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Integer currentParagraphId = (Integer) session.getAttribute("current" + storyId);
        if (currentParagraphId == null) {
            final StoryDAO storyDAO = new StoryDAO(dataSource);
            final Story story = storyDAO.getStory(storyId);
            if (story == null) {
                // Story not found
                response.sendRedirect("/");
                return;
            }

            // Fetch complete paragraph information
            currentParagraphId = story.getFirstParagraph();
        }

        final ParagraphDAO paragraphDAO = new ParagraphDAO(dataSource);
        Paragraph topParagraph = paragraphDAO.getParagraph(currentParagraphId);
        session.setAttribute("current" + storyId, topParagraph.getId());

        // Handle only choices and next chains
        StringBuilder paragraphsTextBuilder = new StringBuilder();
        Paragraph currentParagraph = processParagraph(topParagraph, paragraphsTextBuilder);

        // Build user history
        List<Choice> history = new ArrayList<Choice>();
        final Enumeration<String> sessionKeys = session.getAttributeNames();
        while (sessionKeys.hasMoreElements()) {
            String key = sessionKeys.nextElement();
            if (key.startsWith("choice")) {
                final Choice choice = (Choice) session.getAttribute(key);
                history.add(choice);
            }
        }

        request.setAttribute("history", history);

        request.setAttribute("paragraph", new Paragraph(
                topParagraph.getId(),
                topParagraph.getAuthor(),
                paragraphsTextBuilder.toString(),
                null,
                currentParagraph.isEnding(),
                currentParagraph.getChoices())
        );
        request.getRequestDispatcher("/WEB-INF/read.jsp").forward(request, response);
    }

    /**
     * Concatenate paragraphs linked via only choices or `next` chain.
     *
     * @param currentParagraph Top paragraph whose text is already inserted in builder
     * @param paragraphsTextBuilder View text builder
     * @return The very last visited paragraph
     */
    private Paragraph processParagraph(Paragraph currentParagraph, StringBuilder paragraphsTextBuilder) {
        paragraphsTextBuilder.append(currentParagraph.getText()).append("<br>");
        // Seek through the whole next chain
        while (currentParagraph.getNext() != null) {
            currentParagraph = currentParagraph.getNext();
            paragraphsTextBuilder.append(currentParagraph.getText()).append("<br>");
        }

        // Filter choices
        final ParagraphDAO paragraphDAO = new ParagraphDAO(dataSource);
        final List<Choice> choices = currentParagraph.getChoices();
        final Iterator<Choice> it = choices.iterator();
        while (it.hasNext()) {
            final Choice c = it.next();
            if (c.getCondition() != 0) {
                Integer visitedCondition = (Integer)session.getAttribute("previous" + c.getCondition());
                if (visitedCondition == null) {
                    it.remove();
                }
            } else if (session.getAttribute("user") == null && !paragraphDAO.leadsToEnding(c.getDestination())) {
                // Only logged-in users may see unfinished branches of the story
                it.remove();
            }
        }

        // Fuse destination paragraph if only one choice remains
        if (choices.size() == 1 && choices.get(0).getDestination() != 0 && choices.get(0).getEditor() == null) {
            final Choice onlyChoice = choices.get(0);
            paragraphsTextBuilder.append(onlyChoice.getTitle()).append(" ");
            return processParagraph(paragraphDAO.getParagraph(onlyChoice.getDestination()),
                    paragraphsTextBuilder);
        }

        return currentParagraph;
    }

    private void clearProgression(int startParagraph, HttpServletRequest request) {
        final Choice previousChoice = (Choice) session.getAttribute("choice" + startParagraph);
        if (previousChoice != null) {
            session.removeAttribute("choice" + startParagraph);
            int destinationParagraphId = previousChoice.getDestination();
            Choice currentChoice = previousChoice;
            final ParagraphDAO paragraphDAO = new ParagraphDAO(dataSource);
            while (currentChoice != null) {
                final int dest = currentChoice.getDestination();
                currentChoice = (Choice) session.getAttribute("choice" + dest);
                session.removeAttribute("choice" + dest);
                session.removeAttribute("previous" + dest);

                // Follow `next` paragraph chains
                if (currentChoice == null) {
                    final Paragraph paragraph = paragraphDAO.getParagraph(dest);
                    if (paragraph.getNext() != null) {
                        Paragraph currentParagraph = paragraph;
                        while (currentParagraph.getNext() != null) {
                            currentParagraph = currentParagraph.getNext();
                        }
                        currentChoice = new Choice(0, "", dest, currentParagraph.getId(), 0, null);
                    }
                }
            }
        }
    }

}
