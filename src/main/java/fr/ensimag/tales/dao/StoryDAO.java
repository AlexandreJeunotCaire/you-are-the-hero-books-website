package fr.ensimag.tales.dao;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

import fr.ensimag.tales.model.Story;

/**
 * StoryDAO provides access to Story objects stored in the database.
 */
public class StoryDAO extends AbstractDataBaseDAO {

    /**
     * Base query to retrieve stories and their first paragraph.
     */
    private static final String STORY_QUERY = "SELECT " +
            "s.id, s.author as storyAuthor, s.visibility, s.published, " +
            "p.id as firstParagraph, p.text as summary, a.author as coAuthor " +
            "FROM Story s " +
            "JOIN Paragraph p ON firstPar=p.id " +
            "JOIN Authors a ON s.id=a.story";

    private static final String STORY_PUBLISHED_QUERY = STORY_QUERY + " WHERE s.published=1";

    /**
     * Connect to a datasource.
     *
     * @param ds JDBC Datasource
     */
    public StoryDAO(DataSource ds) {
        super(ds);
    }

    /**
     * Construct a Story object from an SQL query result.
     *
     * **REQUIRES** A result from a query based on STORY_QUERY or
     * compatible naming.
     *
     * @param result Database row
     * @return A story
     * @throws SQLException On missing fields etc.
     */
    private Story makeStory(ResultSet result) throws SQLException {
        final HashSet<String> authors = new HashSet<String>();
        authors.add(result.getString("coAuthor"));
        return new Story(result.getInt("id"),
                result.getString("storyAuthor"),
                result.getString("visibility").equals("public") ? Story.Visibility.PUBLIC : Story.Visibility.ONINVITE,
                result.getBoolean("published"),
                result.getInt("firstParagraph"),
                result.getString("summary"),
                authors
        );
    }

    private List<Story> handleResult(ResultSet result) throws SQLException {
        final HashMap<Integer, Story> stories = new HashMap<Integer, Story>();
        while (result.next()) {
            final int storyId = result.getInt("id");
            final Story currentStory = stories.get(storyId);
            if (currentStory != null) {
                final String coAuthor = result.getString("coAuthor");
                currentStory.getAuthors().add(result.getString("coAuthor"));
            } else {
                stories.put(storyId, makeStory(result));
            }
        }
        final Collection<Story> storyCollection = stories.values();
        return new ArrayList<Story>(storyCollection);
    }

    /**
     * Retrieve a list of all stories (published or unpublished).
     *
     * @return All stories.
     */
    public List<Story> getStories() {
        return getStories(STORY_QUERY);
    }

    /**
     * Filter stories to output published ones.
     *
     * A published story must have been published by its author and it
     * contains at least one ending paragraph.
     *
     * @return
     */
    public List<Story> getPublishedStories() {
        return getStories(STORY_PUBLISHED_QUERY);
    }

    /**
     * Get stories started by a specific user.
     * @param username Story author
     * @return List of stories
     */
    public List<Story> getStoriesBy(String username) {
        try (final Connection conn = getConn()) {
            final PreparedStatement stmt = conn.prepareStatement(STORY_QUERY + " WHERE s.author=?");
            stmt.setString(1, username);
            final ResultSet result = stmt.executeQuery();
            return handleResult(result);
        } catch (SQLException e) {
            throw new DAOException("Database error while fetching user stories. " + e.getMessage());
        }
    }

    /**
     * Find a story from its first paragraph.
     * @param paragraphId Target paragraph ID
     * @return Story ID
     */
    public int getStoryWithFirstParagraph(int paragraphId) {
        try (final Connection conn = getConn()) {
            final PreparedStatement stmt = conn.prepareStatement("SELECT id FROM Story WHERE firstPar=?");
            stmt.setInt(1, paragraphId);
            final ResultSet result = stmt.executeQuery();
            if (!result.next()) {
                return 0;
            }
            return result.getInt("id");
        } catch (SQLException e) {
            throw new DAOException("Database error while retrieving story based on paragraph information. " + e.getMessage());
        }
    }

    /**
     * Retrieve all stories matching a generic query (no parameters).
     *
     * @param query SQL generic query
     * @return List of stories.
     */
    private List<Story> getStories(String query) {
        try (final Connection conn = getConn()) {
            final Statement stmt = conn.createStatement();
            final ResultSet result = stmt.executeQuery(query);
            return handleResult(result);
        } catch (SQLException e) {
            throw new DAOException("Database error while fetching list of stories. " + e.getMessage());
        }
    }

    /**
     * Fetch a single story.
     *
     * @return A nullable story
     */
    public Story getStory(int id) {
        try (final Connection conn = getConn()) {
            final PreparedStatement stmt = conn.prepareStatement(STORY_QUERY + " WHERE s.id=?");
            stmt.setInt(1, id);
            final ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return makeStory(result);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DAOException("Database error while fetching story. " + e.getMessage());
        }
    }

    /**
     * Create a story from its first paragraph.
     * @param author Original author of the story
     * @param firstParagraph First paragraph ID
     * @return New story id
     */
    public int createStory(String author, int firstParagraph) {
        try (final Connection conn = getConn()) {
            final PreparedStatement stmt = conn.prepareStatement("INSERT INTO Story VALUES(storyId.nextval, ?, 'public', 0, ?)");
            stmt.setString(1, author);
            stmt.setInt(2, firstParagraph);
            stmt.execute();
            final Statement idStmt = conn.createStatement();
            final ResultSet result = idStmt.executeQuery("SELECT storyId.currval FROM dual");
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new DAOException("Database error while creating story. " + e.getMessage());
        }
    }


    /**
     * Make a story publicly readable
     * @param storyId Target story ID
     */
    public void publishStory(int storyId) {
        setPublished(storyId, true);
    }

    /**
     * Cancel public user readability.
     * @param storyId Target story ID
     */
    public void unpublishStory(int storyId) {
        setPublished(storyId, false);
    }

    /**
     * Set a story publication status
     * @param storyId Target story ID
     * @param published Whether the story should be publicly visible
     */
    private void setPublished(int storyId, boolean published) {
        try (final Connection conn = getConn()) {
            final PreparedStatement stmt = conn.prepareStatement("UPDATE Story SET published=? WHERE id=?");
            stmt.setBoolean(1, published);
            stmt.setInt(2, storyId);
            stmt.execute();
        } catch (SQLException e) {
            throw new DAOException("Database error while managinc story publication. " + e.getMessage());
        }
    }

    /**
     * Set who can contribute to the story.
     * @param storyId Target story ID
     * @param visibility Public or private visibility
     */
    public void setVisibility(int storyId, Story.Visibility visibility) {
        try (final Connection conn = getConn()) {
            final PreparedStatement stmt = conn.prepareStatement("UPDATE Story SET visibility=? WHERE id=?");
            stmt.setString(1, visibility == Story.Visibility.PUBLIC ? "public" : "oninvite");
            stmt.setInt(2, storyId);
            stmt.execute();
        } catch (SQLException e) {
            throw new DAOException("Database error while managing story visiblity. " + e.getMessage());
        }
    }

    /**
     * Add a user to the list of story authors.
     */
    public void addAuthor(int storyId, String username) {
        try (final Connection conn = getConn()) {
            final PreparedStatement stmt = conn.prepareStatement("INSERT INTO Authors VALUES (?, ?)");
            stmt.setInt(1, storyId);
            stmt.setString(2, username);
            stmt.execute();
        } catch (SQLException e) {
            // It's OK to add the same user more than once
            return;
        }
    }

}
