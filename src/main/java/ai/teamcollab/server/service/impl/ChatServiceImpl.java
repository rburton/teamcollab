package ai.teamcollab.server.service.impl;

import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.PointInTimeSummary;
import ai.teamcollab.server.service.ChatService;
import ai.teamcollab.server.service.domain.ChatContext;
import ai.teamcollab.server.service.domain.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Implementation of the ChatService interface that delegates to specialized components.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final MessageProcessor messageProcessor;
    private final SummaryGenerator summaryGenerator;

    @Override
    public CompletableFuture<MessageResponse> process(Conversation conversation, Message recent, ChatContext chatContext) {
        log.debug("Delegating message processing to MessageProcessor");
        return messageProcessor.process(conversation, recent, chatContext);
    }

    @Override
    public CompletableFuture<PointInTimeSummary> generatePointInTimeSummary(Conversation conversation, ChatContext chatContext) {
        log.debug("Delegating summary generation to SummaryGenerator");
        return summaryGenerator.generateSummary(conversation, chatContext);
    }

}
