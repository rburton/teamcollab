package ai.teamcollab.server.controller.domain;

import ai.teamcollab.server.domain.Assistant;
import ai.teamcollab.server.domain.AssistantTone;
import ai.teamcollab.server.domain.ConversationAssistant;
import lombok.Builder;
import lombok.Getter;

import java.util.Collection;
import java.util.List;

@Getter
@Builder
public class AssistantView {
    private String id;
    private String name;
    private String expertise;
    private String tone;
    private boolean muted;

    public static List<AssistantView> fromAssistants(Collection<Assistant> assistants) {
        return assistants.stream()
                .map(AssistantView::from)
                .toList();
    }

    public static AssistantView from(Assistant assistant) {
        return AssistantView.builder()
                .id(String.valueOf(assistant.getId()))
                .name(assistant.getName())
                .muted(false)
                .expertise(assistant.getExpertise())
                .build();
    }

    public static AssistantView from(ConversationAssistant conversationAssistant) {
        final var assistant = conversationAssistant.getAssistant();
        final var tone = conversationAssistant.getTone();
        return AssistantView.builder()
                .id(String.valueOf(assistant.getId()))
                .name(assistant.getName())
                .muted(conversationAssistant.isMuted())
                .tone(tone != null ? tone.getName() : "FORMAL")
                .expertise(assistant.getExpertise())
                .build();
    }

    public static List<AssistantView> from(Collection<ConversationAssistant> conversationAssistant) {
        return conversationAssistant.stream()
                .map(AssistantView::from)
                .toList();
    }

}
