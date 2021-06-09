package fr.ensimag.tales.dao;

import fr.ensimag.tales.model.Choice;
import fr.ensimag.tales.model.Paragraph;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * ParagraphDAO provides access to Paragraphs objects and their related choices.
 */
public class ParagraphDAO extends AbstractDataBaseDAO {

    /**
     * Connect to a datasource.
     *
     * @param ds JDBC Datasource
     */
    public ParagraphDAO(DataSource ds) {
        super(ds);
    }

    /**
     * Retrieve a paragraph and its choices.
     *
     * @param id Paragraph ID
     * @return Linked tree of paragraphs (via next) and choices
     */
    public Paragraph getParagraph(int id) {
        try (final Connection conn = getConn()) {
            return getParagraph(id, conn);
        } catch (SQLException e) {
            throw new DAOException("Database error while fetching paragraph. " + e.getMessage());
        }
    }

    private Paragraph getParagraph(int id, Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Paragraph WHERE id=?");
        stmt.setInt(1, id);

        ResultSet result = stmt.executeQuery();
        if (!result.next()) {
            // Paragraph not found
            return null;
        }
        final List<Choice> choices = new ArrayList<Choice>();

        Paragraph nextParagraph = null;
        final int nextParagraphId = result.getInt("next");
        if (nextParagraphId != 0) {
            // WARNING Recursive call
            nextParagraph = getParagraph(nextParagraphId, conn);
        }
        final Paragraph paragraph = new Paragraph(
                result.getInt("id"),
                result.getString("author"),
                result.getString("text"),
                nextParagraph,
                result.getBoolean("ending"),
                choices
        );

        stmt = conn.prepareStatement("SELECT * FROM Choice WHERE src=?");
        stmt.setInt(1, id);
        result = stmt.executeQuery();
        if (!result.next()) {
            // No choices
            return paragraph;
        }
        choices.add(ChoiceDAO.makeChoice(result));
        while (result.next()) {
            choices.add(ChoiceDAO.makeChoice(result));
        }
        return paragraph;
    }

    /**
     * Insert a new paragraph.
     * @param author Current paragraph author
     * @return Created paragraph ID
     */
    public int createParagraph(String author) {
        try (final Connection conn = getConn()) {
            final PreparedStatement stmt = conn.prepareStatement("INSERT INTO Paragraph VALUES (paragraphId.nextval, ?, '', NULL, 0)");
            stmt.setString(1, author);
            stmt.execute();
            final Statement idStmt = conn.createStatement();
            final ResultSet result = idStmt.executeQuery("SELECT paragraphId.currval FROM dual");
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new DAOException("Database error while creating paragraph. " + e.getMessage());
        }
    }

    /**
     * Register a parent-child mapping.
     * @param parent Parent paragraph ID
     * @param child Child paragraph ID
     */
    public void makeParent(int parent, int child) {
        try (final Connection conn = getConn()) {
            final PreparedStatement stmt = conn.prepareStatement("INSERT INTO Parent VALUES (?, ?)");
            stmt.setInt(1, parent);
            stmt.setInt(2, child);
            stmt.execute();
        } catch (SQLException e) {
            throw new DAOException("Database error while registering parent child mapping. " + e.getMessage());
        }
    }

    /**
     * Returns whether a paragraph may lead to an ending (via choices or `next` chain).
     * @param paragraphId Paragraph to test
     * @return True if there exists a path from target paragraph to any ending.
     */
    public boolean leadsToEnding(int paragraphId) {
       final Paragraph paragraph = getParagraph(paragraphId);
       if (paragraph == null) {
           // Paragraph not found
           return false;
       }

       // Follow next chain
       Paragraph currentParagraph = paragraph;
       while (currentParagraph.getNext() != null) {
           currentParagraph = currentParagraph.getNext();
       }

       final List<Choice> choices = currentParagraph.getChoices();
       if (choices == null || choices.size() == 0) {
           // No choices
           return currentParagraph.isEnding();
       }

       // Boolean-OR all choices
       boolean ok = false;
       for (final Choice c : choices) {
           ok = ok | leadsToEnding(c.getDestination());
       }
       return ok;
    }

    /**
     * Return a mapping of parent-child paragraph relationships (choices and next chains).
     * @return Map of parent-child pairs (key=child, val=parent)
     */
    private HashMap<Integer, HashSet<Integer>> getParents() {
        final HashMap<Integer, HashSet<Integer>> parents = new HashMap<Integer, HashSet<Integer>>();
        try (final Connection conn = getConn()) {
            final Statement stmt = conn.createStatement();
            final ResultSet result = stmt.executeQuery("SELECT * FROM Parent");
            while (result.next()) {
                HashSet<Integer> myParents = parents.get(result.getInt("child"));
                if (myParents == null) {
                    myParents = new HashSet<Integer>();
                    parents.put(result.getInt("child"), myParents);
                }
                myParents.add(result.getInt("parent"));
            }
            return parents;
        } catch (SQLException e) {
            throw new DAOException("Database error while fetching parent information. " + e.getMessage());
        }
    }

    /**
     * Get the list of a paragraph's children (via choices and next chains).
     * @param paragraphId Parent paragraph
     * @return List of child paragraph IDs
     */
    private List<Integer> getChildren(int paragraphId) {
        final List<Integer> children = new ArrayList<Integer>();
        try (final Connection conn = getConn()) {
           final PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Parent WHERE parent=?");
           stmt.setInt(1, paragraphId);
           final ResultSet result = stmt.executeQuery();
           while (result.next()) {
               children.add(result.getInt("child"));
           }
           return children;
        } catch (SQLException e) {
            throw new DAOException("Database error while fetching child information. " + e.getMessage());
        }
    }

    /**
     * Retrieve the first paragraph (upmost parent).
     * @param paragraphId Current paragraph
     * @param parents {@link ParagraphDAO#getParents}
     * @return First paragraph
     */
    private Paragraph getFirstParagraph(int paragraphId, HashMap<Integer, HashSet<Integer>> parents) {
        Integer parent = paragraphId;
        while (parents.get(parent) != null) {
            parent = parents.get(parent).iterator().next(); // Follow any path
        }
        return getParagraph(parent);
    }

    /**
     * Find the 1st paragraph of the story.
     * @param paragraphId Child paragraph ID
     */
    public Paragraph getFirstParagraph(int paragraphId) {
        final HashMap<Integer, HashSet<Integer>> parents = getParents();
        return getFirstParagraph(paragraphId, parents);
    }

    /**
     * Get paragraphs that may serve as a condition in a choice.
     *
     * All parent paragraphs traversing the graph of paragraphs from bottom to top.
     *
     * @param paragraphId Current paragraph
     * @return List of parent paragraphs
     */
    public Set<Paragraph> getCondTargets(int paragraphId) {
        final Set<Paragraph> condTargets = new HashSet<Paragraph>();
        try (final Connection conn = getConn()) {
            final HashMap<Integer, HashSet<Integer>> parents = getParents();
            HashSet<Integer> myParents = parents.get(paragraphId);
            if (myParents == null) {
                return condTargets;
            }
            final Queue<Integer> frontier = new ArrayDeque<Integer>(myParents);
            while (!frontier.isEmpty()) {
                Integer parent = frontier.poll();
                condTargets.add(getParagraph(parent));
                myParents = parents.get(parent);
                if (myParents != null) {
                    frontier.addAll(parents.get(parent));
                }
            }
            return condTargets;
        } catch (SQLException e) {
            throw new DAOException("Database error while fetching condition targets. " + e.getMessage());
        }
    }

    /**
     * Get paragraphs that may serve as a next paragraph from the current paragraph.
     *
     * All paragraphs except parent ones traversing the paragraph graph from bottom to top.
     */
    public Set<Paragraph> getNextTargets(int paragraphId) {
        final HashSet<Paragraph> nextTargets = new HashSet<Paragraph>();
        try (final Connection conn = getConn()) {
            final HashMap<Integer, HashSet<Integer>> parents = getParents();
            final Paragraph top = getFirstParagraph(paragraphId, parents);

            // Get all paragraphs
            final Queue<Integer> frontier = new ArrayDeque<Integer>(getChildren(top.getId()));
            while (!frontier.isEmpty()) {
                final Integer child = frontier.poll();
                nextTargets.add(getParagraph(child));
                frontier.addAll(getChildren(child));
            }

            // Remove paragraphs that may have been previously visited
            final Queue<Integer> parentFrontier = new ArrayDeque<>();
            parentFrontier.add(paragraphId);
            int parent = paragraphId;
            while (!parentFrontier.isEmpty()) {
                parent = parentFrontier.poll();
                nextTargets.remove(getParagraph(parent));
                final HashSet<Integer> myParents = parents.get(parent);
                if (myParents != null) {
                    parentFrontier.addAll(myParents);
                }
            }
            return nextTargets;
        } catch (SQLException e) {
            throw new DAOException("Database error while fetching next targets. " + e.getMessage());
        }
    }

    /**
     * Set paragraph text.
     * @param paragraphId Target paragraph ID
     * @param text
     */
    public void setText(int paragraphId, String text) {
        try (final Connection conn = getConn()) {
            final PreparedStatement stmt = conn.prepareStatement("UPDATE Paragraph SET text=? WHERE id=?");
            stmt.setString(1, text);
            stmt.setInt(2, paragraphId);
            stmt.execute();
        } catch (SQLException e) {
            throw new DAOException("Database error while setting paragraph text. " + e.getMessage());
        }
    }

    /**
     * Set next paragraph.
     * @param paragraphId Target paragraph ID
     * @param nextParagraph Next paragraph ID
     */
    public void setNext(int paragraphId, int nextParagraph) {
        try (final Connection conn = getConn()) {
            final PreparedStatement stmt = conn.prepareStatement("UPDATE Paragraph SET next=? WHERE id=?");
            stmt.setInt(1, nextParagraph);
            stmt.setInt(2, paragraphId);
            stmt.execute();
        } catch (SQLException e) {
            throw new DAOException("Database error while setting next paragraph. " + e.getMessage());
        }
    }

    /**
     * Add choices to a paragraph.
     * @param paragraphId Target paragraph ID
     * @param choices List of new choices
     */
    public void addChoices(int paragraphId, List<Choice> choices) {
        try (final Connection conn = getConn()) {
            for (final Choice choice : choices) {
                final PreparedStatement stmt = conn.prepareStatement("INSERT INTO Choice VALUES (choiceId.nextval, ?, ?, null, ?, null)");
                stmt.setString(1, choice.getTitle());
                stmt.setInt(2, paragraphId);
                final int cond = choice.getCondition();
                if (cond != 0) {
                    stmt.setInt(3, cond);
                } else {
                    stmt.setNull(3, Types.INTEGER);
                }
                stmt.execute();
            }
        } catch (SQLException e) {
            throw new DAOException("Database error while adding choices. " + e.getMessage());
        }
    }

    /**
     * Make a paragraph a possible ending to the story.
     * @param paragraphId Target paragraph ID
     */
    public void makeEnding(int paragraphId) {
        try (final Connection conn = getConn()) {
            final PreparedStatement stmt = conn.prepareStatement("UPDATE Paragraph SET ending=1 WHERE id=?");
            stmt.setInt(1, paragraphId);
            stmt.execute();
        } catch (SQLException e) {
            throw new DAOException("Database error while making paragraph an ending. " + e.getMessage());
        }
    }

    /**
     * Unlock any choices preventing other users from editing this paragraph.
     * @param paragraphId Target paragraph ID
     */
    public void unlockChoices(int paragraphId) {
        try (final Connection conn = getConn()) {
            final PreparedStatement stmt = conn.prepareStatement("UPDATE Choice SET editor=null WHERE dest=?");
            stmt.setInt(1, paragraphId);
            stmt.execute();
        } catch (SQLException e) {
            throw new DAOException("Database error while unlocking paragraph. " + e.getMessage());
        }
    }

    /**
     * Delete a paragraph. Stories with this paragraph as their first paragraph will also be deleted.
     *
     * @param paragraphId Target paragraph ID
     */
    public void deleteParagraph(int paragraphId) {
        try (final Connection conn = getConn()) {
            final PreparedStatement choiceStmt = conn.prepareStatement("UPDATE Choice SET dest=null WHERE dest=?");
            choiceStmt.setInt(1, paragraphId);
            choiceStmt.execute();
            final PreparedStatement storyStmt = conn.prepareStatement("DELETE FROM Story WHERE firstPar=?");
            storyStmt.setInt(1, paragraphId);
            storyStmt.execute();
            final PreparedStatement stmt = conn.prepareStatement("DELETE FROM Paragraph WHERE id=?");
            stmt.setInt(1, paragraphId);
            stmt.execute();
        } catch (SQLException e) {
            throw new DAOException("Database error while deleting paragraph. " + e.getMessage());
        }
    }


}
