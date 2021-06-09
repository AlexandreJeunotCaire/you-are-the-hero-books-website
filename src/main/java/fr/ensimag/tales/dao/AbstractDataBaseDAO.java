package fr.ensimag.tales.dao;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * Classe abstraite permettant de factoriser du code pour les DAO
 * basées sur JDBC
 */
public abstract class AbstractDataBaseDAO {

    /**
     * Main data source, typically handed in from a servlet @Resource
     */
    protected final DataSource dataSource;

    /**
     * Save SQL datasource.
     * @param ds DataSource to connect to
     */
    protected AbstractDataBaseDAO(DataSource ds) {
        this.dataSource = ds;
    }

    /**
     * Get a SQL connection from the pool.
     * @return Usable JDBC Connection
     * @throws SQLException If connecting/authenticating fails
     */
    protected Connection getConn() throws SQLException {
        return dataSource.getConnection();
    }
}
