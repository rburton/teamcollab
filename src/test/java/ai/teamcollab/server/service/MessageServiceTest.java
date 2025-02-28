package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.repository.ConversationRepository;
import ai.teamcollab.server.repository.MessageRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @InjectMocks
    private MessageService messageService;

    private User user;
    private Conversation conversation;
    private Message message;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        conversation = new Conversation();
        conversation.setId(1L);
        conversation.setUser(user);

        message = new Message();
        message.setContent("Test message");
    }

    @Test
    void createMessage_Success() {
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        Message result = messageService.createMessage(message, 1L, user);

        assertNotNull(result);
        assertEquals("Test message", result.getContent());
        assertEquals(conversation, result.getConversation());
        assertEquals(user, result.getUser());
        assertNotNull(result.getCreatedAt());

        verify(conversationRepository).findById(1L);
        verify(messageRepository).save(message);
    }

    @Test
    void createMessage_ConversationNotFound() {
        when(conversationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> messageService.createMessage(message, 1L, user));

        verify(conversationRepository).findById(1L);
        verify(messageRepository, never()).save(any());
    }

    @Test
    void createMessage_UnauthorizedUser() {
        User unauthorizedUser = new User();
        unauthorizedUser.setId(2L);

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));

        assertThrows(IllegalArgumentException.class,
                () -> messageService.createMessage(message, 1L, unauthorizedUser));

        verify(conversationRepository).findById(1L);
        verify(messageRepository, never()).save(any());
    }

    @Test
    void getConversationMessages_Success() {
        List<Message> messages = Arrays.asList(
                new Message(), new Message()
        );

        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(1L))
                .thenReturn(messages);

        List<Message> result = messageService.getConversationMessages(1L);

        assertEquals(2, result.size());
        verify(messageRepository).findByConversationIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void getUserMessages_Success() {
        List<Message> messages = Arrays.asList(
                new Message(), new Message()
        );

        when(messageRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(messages);

        List<Message> result = messageService.getUserMessages(1L);

        assertEquals(2, result.size());
        verify(messageRepository).findByUserIdOrderByCreatedAtDesc(1L);
    }
}