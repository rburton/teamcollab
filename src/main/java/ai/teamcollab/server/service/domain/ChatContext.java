package ai.teamcollab.server.service.domain;

import ai.teamcollab.server.domain.Message;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

@Getter
@Builder
public class ChatContext {
    @NonNull
    private final String purpose;
    
    @NonNull
    private final String projectOverview;
    
    @NonNull
    private final List<Message> lastMessages;
}