package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.Project;
import ai.teamcollab.server.repository.ConversationRepository;
import ai.teamcollab.server.repository.MessageRepository;
import ai.teamcollab.server.repository.UserRepository;
import ai.teamcollab.server.service.domain.ChatContext;
import ai.teamcollab.server.service.domain.MessageResponse;
import ai.teamcollab.server.templates.ThymeleafTemplateRender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ChatService chatService;

    @Mock
    private MessageService messageService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ThymeleafTemplateRender thymeleafTemplateRender;

    private ConversationService conversationService;

    @BeforeEach
    void setUp() {
        conversationService = new ConversationService(
                chatService,
                messageService,
                userRepository,
                messageRepository,
                conversationRepository,
                messagingTemplate,
                thymeleafTemplateRender
        );
    }

    @Test
    void sendMessage_shouldBuildChatContextAndPassToLlmService() {
        // Arrange
        final var project = new Project();
        project.setOverview("Test Project Overview");

        final var conversation = new Conversation();
        conversation.setId(1L);
        conversation.setPurpose("Test Purpose");
        conversation.setProject(project);

        final var message = new Message();
        message.setId(1L);
        message.setConversation(conversation);
        message.setContent("Test Message");
        message.setCreatedAt(LocalDateTime.now());

        final var messageResponse = MessageResponse.builder()
                .content("Test Response")
                .build();

        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(messageRepository.findTop10ByConversationIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(message));
        when(chatService.process(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(messageResponse));

        // Act
//        conversationService.sendMessage(1L);

        // Assert
        verify(chatService).process(any(), any(), any(ChatContext.class));
    }
}