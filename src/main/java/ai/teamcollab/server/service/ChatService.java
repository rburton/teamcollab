package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.PointInTimeSummary;
import ai.teamcollab.server.service.domain.ChatContext;
import ai.teamcollab.server.service.domain.MessageResponse;

import java.util.concurrent.CompletableFuture;

public interface ChatService {
    /**
     * Asynchronously process a message with enhanced context and generate a response.
     *
     * @param conversation The conversation context
     * @param recent The most recent message to process
     * @param chatContext The enhanced context containing conversation history, purpose and project overview
     * @return CompletableFuture<MessageResponse> containing the LLM response and associated metrics
     */
    CompletableFuture<MessageResponse> process(Conversation conversation, Message recent, ChatContext chatContext);

    /**
     * Generates a point-in-time summary for a conversation.
     * The summary includes:
     * - A list of topics and key points talked about
     * - A summary of each topic and key point with critical information
     * - A summary for each assistant in the conversation
     *
     * @param conversation the conversation to summarize
     * @param chatContext the context of the conversation
     * @return a CompletableFuture containing the generated PointInTimeSummary
     */
    CompletableFuture<PointInTimeSummary> generatePointInTimeSummary(Conversation conversation, ChatContext chatContext);
}
