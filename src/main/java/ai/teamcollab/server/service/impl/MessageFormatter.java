package ai.teamcollab.server.service.impl;

import ai.teamcollab.server.domain.Message;
import org.springframework.stereotype.Component;

/**
 * Utility class for formatting messages.
 */
@Component
public class MessageFormatter {
    public static final String USER = "User";
    public static final String ASSISTANT = "Assistant";
    
    /**
     * Formats a user message for display or processing.
     *
     * @param message the message to format
     * @return the formatted message
     */
    public String formatUserMessage(Message message) {
        return !message.isAssistant() ? message.getUser().getUsername() : USER;
    }
    
    /**
     * Formats an assistant message for display or processing.
     *
     * @param message the message to format
     * @return the formatted message
     */
    public String formatAssistantMessage(Message message) {
        return message.isAssistant() ? message.getAssistant().getName() : ASSISTANT;
    }
    
    /**
     * Determines the sender name for a message.
     *
     * @param message the message to get the sender for
     * @return the sender name
     */
    public String getSenderName(Message message) {
        if (message.isAssistant()) {
            return formatAssistantMessage(message);
        } else {
            return formatUserMessage(message);
        }
    }
}