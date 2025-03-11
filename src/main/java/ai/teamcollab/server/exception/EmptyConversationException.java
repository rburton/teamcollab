package ai.teamcollab.server.exception;

/**
 * Exception thrown when attempting to perform operations on a conversation with no messages.
 */
public class EmptyConversationException extends RuntimeException {

    /**
     * Constructs a new EmptyConversationException with the specified detail message.
     *
     * @param message the detail message
     */
    public EmptyConversationException(String message) {
        super(message);
    }

    /**
     * Constructs a new EmptyConversationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public EmptyConversationException(String message, Throwable cause) {
        super(message, cause);
    }
}