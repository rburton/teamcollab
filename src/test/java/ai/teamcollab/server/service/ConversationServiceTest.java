package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.repository.ConversationRepository;
import ai.teamcollab.server.repository.MessageRepository;
import ai.teamcollab.server.repository.UserRepository;
import ai.teamcollab.server.service.domain.MessageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatService chatService;

    @Mock
    private MessageService messageService;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private ConversationService conversationService;

    private User testUser;
    private Conversation testConversation;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "password", "test@example.com");
        testUser.setId(1L);

        testConversation = new Conversation("Test Purpose", testUser);
        testConversation.setId(1L);
        testConversation.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createConversationShouldSucceed() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);

        Conversation result = conversationService.createConversation(testConversation, testUser.getId());

        assertThat(result).isNotNull();
        assertThat(result.getPurpose()).isEqualTo(testConversation.getPurpose());
        assertThat(result.getUser()).isEqualTo(testUser);
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void createConversationShouldThrowExceptionForInvalidUser() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationService.createConversation(testConversation, testUser.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

        verify(conversationRepository, never()).save(any(Conversation.class));
    }

    @Test
    void getUserConversationsShouldReturnUserConversations() {
        List<Conversation> conversations = Arrays.asList(testConversation);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(conversationRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId())).thenReturn(conversations);

        List<Conversation> result = conversationService.getUserConversations(testUser.getId());

        assertThat(result).hasSize(1);
        verify(conversationRepository).findByUserIdOrderByCreatedAtDesc(testUser.getId());
    }

    @Test
    void getUserConversationsShouldThrowExceptionForInvalidUser() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationService.getUserConversations(testUser.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

        verify(conversationRepository, never()).findByUserIdOrderByCreatedAtDesc(any());
    }

    @Test
    void findConversationByIdShouldReturnConversation() {
        when(conversationRepository.findById(testConversation.getId())).thenReturn(Optional.of(testConversation));

        Conversation result = conversationService.findConversationById(testConversation.getId());

        assertThat(result).isNotNull();
        verify(conversationRepository).findById(testConversation.getId());
    }

    @Test
    void findConversationByIdShouldThrowExceptionForInvalidId() {
        when(conversationRepository.findById(testConversation.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationService.findConversationById(testConversation.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Conversation not found");
    }

    @Test
    void sendMessageShouldSucceed() throws ExecutionException, InterruptedException {
        Message message = new Message();
        message.setContent("Test message");
        message.setId(1L);

        when(conversationRepository.findById(testConversation.getId())).thenReturn(Optional.of(testConversation));
        when(messageService.createMessage(message, testConversation.getId(), testUser))
                .thenReturn(message);
        when(chatService.process(testConversation, message))
                .thenReturn(CompletableFuture.completedFuture(MessageResponse.builder().content("Response").build()));

        CompletableFuture<Void> result = conversationService.sendMessage(testConversation.getId(), message, testUser);
        result.get(); // Wait for completion

        verify(messageService).createMessage(message, testConversation.getId(), testUser);
        verify(chatService).process(testConversation, message);
    }

    @Test
    void sendMessageShouldHandleProcessingError() {
        Message message = new Message();
        message.setContent("Test message");
        message.setId(1L);

        when(conversationRepository.findById(testConversation.getId())).thenReturn(Optional.of(testConversation));
        when(messageService.createMessage(message, testConversation.getId(), testUser))
                .thenReturn(message);
        when(chatService.process(testConversation, message))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Processing failed")));

        CompletableFuture<Void> result = conversationService.sendMessage(testConversation.getId(), message, testUser);

        assertThatThrownBy(result::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(RuntimeException.class)
                .hasMessageContaining("Processing failed");
    }

    @Test
    void sendMessageShouldHandleInvalidConversation() {
        Message message = new Message();
        message.setContent("Test message");
        message.setId(1L);

        when(conversationRepository.findById(testConversation.getId())).thenReturn(Optional.empty());

        CompletableFuture<Void> result = conversationService.sendMessage(testConversation.getId(), message, testUser);

        assertThatThrownBy(result::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Conversation not found");

        verify(messageService, never()).createMessage(any(), any(), any());
        verify(chatService, never()).process(any(), any());
    }
}
