package org.datavyu.models.db;

/**
 * An exception caused by the user when interacting with the database.
 *
 * All error messages are recoverable by display a warning message to the user and have them fix it and proceed.
 */
public final class UserWarningException extends Exception {

    /**
     * Creates a new instance of <code>LogicErrorException</code> without detail message
     */
    public UserWarningException() {}

    /**
     * Constructs an instance of <code>LogicErrorException</code> with the specified detail message
     *
     * @param message The detail message
     */
    public UserWarningException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of <code>LogicErrorException</code> with the specified message and call stack
     *
     * @param message An error message
     * @param exception Used to create a call stack. This is the exception that lead to a logic error exception
     */
    public UserWarningException(final String message, final Exception exception) {
        super(message, exception);
    }
}
