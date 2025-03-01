package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.service.domain.MessageResponse;

public interface ChatService {
    /**
     * Process a message in the context of a conversation and generate a response.
     *
     * @param conversation The conversation context
     * @param recent The most recent message to process
     * @return MessageResponse containing the LLM response and associated metrics
     */
    MessageResponse process(Conversation conversation, Message recent);
}