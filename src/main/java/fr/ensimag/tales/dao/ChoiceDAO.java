package fr.ensimag.tales.dao;

import fr.ensimag.tales.model.Choice;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ChoiceDAO extends AbstractDataBaseDAO {
    /**
     * Connect to datasource.
     *
     * @param ds DataSource to connect to
     */
    public ChoiceDAO(DataSource ds) {
        super(ds);
    }

    public static Choice makeChoice(ResultSet result) throws SQLException {
        return new Choice(
                result.getInt("id"),
                result.getString("title"),
                result.getInt("src"),
                result.getInt("dest"),
                result.getInt("cond"),
                result.getString("editor")
        );
    }

    public Choice getChoice(int id) {
        try (final Connection conn = getConn()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Choice WHERE id=?");
            stmt.setInt(1, id);
            ResultSet result = stmt.executeQuery();
            if (!result.next()) {
                return null;
            }
            return makeChoice(result);
        } catch (SQLException e) {
            throw new DAOException("Database error while fetching choice. " + e.getMessage());
        }
    }

    /**
     * Save user progression.
     *
     * @param username  Target user
     * @param paragraph Target paragraph
     * @param choice    Selected choice
     */
    public void save(String username, int paragraph, int choice) {
        try (final Connection conn = getConn()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO ChoiceMap VALUES (?, ?, ?)");
            stmt.setString(1, username);
            stmt.setInt(2, paragraph);
            stmt.setInt(3, choice);
            try {
                stmt.execute();
            } catch (SQLIntegrityConstraintViolationException e) {
                // Changing an existing choice
                stmt = conn.prepareStatement("UPDATE ChoiceMap SET choice=? WHERE reader=? AND paragraph=?");
                stmt.setInt(1, choice);
                stmt.setString(2, username);
                stmt.setInt(3, paragraph);
                stmt.execute();
            }
        } catch (SQLException e) {
            throw new DAOException("Database error while saving choice. " + e.getMessage());
        }
    }

    /**
     * Retrieve saved user progression.
     *
     * @param username Name of the user account
     * @return Choice map of user choice history
     */
    public Map<Integer, Choice> getChoices(String username) {
        try (final Connection conn = getConn()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM ChoiceMap JOIN Choice ON id=choice WHERE reader=?");
            stmt.setString(1, username);
            ResultSet result = stmt.executeQuery();
            final Map<Integer, Choice> choices = new HashMap<Integer, Choice>();
            while (result.next()) {
                choices.put(result.getInt("paragraph"), makeChoice(result));
            }
            return choices;
        } catch (SQLException e) {
            throw new DAOException("Database error while retrieving choices. " + e.getMessage());
        }
    }

    /**
     * Update the destination paragraph for a given choice.
     *
     * @param choiceId    Target choice ID
     * @param destination Destination paragraph ID
     */
    public void setDestination(int choiceId, int destination) {
        try (final Connection conn = getConn()) {
            final PreparedStatement stmt = conn.prepareStatement("UPDATE Choice SET dest=? WHERE id=?");
            stmt.setInt(1, destination);
            stmt.setInt(2, choiceId);
            stmt.execute();
        } catch (SQLException e) {
            throw new DAOException("Database error while setting choice destination. " + e.getMessage());
        }

    }

    public void lockChoice(int choiceId, String editor) {
        try (final Connection conn = getConn()) {
            final PreparedStatement stmt = conn.prepareStatement("UPDATE Choice SET editor=? WHERE id=?");
            stmt.setString(1, editor);
            stmt.setInt(2, choiceId);
            stmt.execute();
        } catch (SQLException e) {
            throw new DAOException("Database error while acquiring choice lock. " + e.getMessage());
        }
    }

}
