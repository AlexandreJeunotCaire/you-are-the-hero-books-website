package fr.ensimag.tales.dao;

/**
 * Utility exception type to handle database errors.
 */
public class DAOException extends RuntimeException {

    /**
     * Default constructor.
     */
    public DAOException() {
    }

    /**
     * **WARNING**: Message will be displayed to end user. Do not include any technical or sensitive details.
     *
     * @param message Text string displayed to the user.
     */
    public DAOException(String message) {
        super(message);
    }

    /**
     * **WARNING**: Message will be displayed to end user. Do not include any technical or sensitive details.
     *
     * @param message Text string displayed to the user.
     * @param cause Include additional debug information.
     */
    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }

}
