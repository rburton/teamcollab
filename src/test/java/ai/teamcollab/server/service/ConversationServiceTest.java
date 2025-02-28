package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.repository.ConversationRepository;
import ai.teamcollab.server.repository.UserRepository;
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

    @InjectMocks
    private ConversationService conversationService;

    private User testUser;
    private Conversation testConversation;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "password", "test@example.com");
        testUser.setId(1L);

        testConversation = new Conversation("Test Topic", "Test Purpose", testUser);
        testConversation.setId(1L);
        testConversation.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createConversationShouldSucceed() {
        when(userRepository.existsById(testUser.getId())).thenReturn(true);
        when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);

        Conversation result = conversationService.createConversation(testConversation, testUser.getId());

        assertThat(result).isNotNull();
        assertThat(result.getTopic()).isEqualTo(testConversation.getTopic());
        assertThat(result.getPurpose()).isEqualTo(testConversation.getPurpose());
        assertThat(result.getUser()).isEqualTo(testUser);
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void createConversationShouldThrowExceptionForInvalidUser() {
        when(userRepository.existsById(testUser.getId())).thenReturn(false);

        assertThatThrownBy(() -> conversationService.createConversation(testConversation, testUser.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

        verify(conversationRepository, never()).save(any(Conversation.class));
    }

    @Test
    void getUserConversationsShouldReturnUserConversations() {
        List<Conversation> conversations = Arrays.asList(testConversation);
        when(userRepository.existsById(testUser.getId())).thenReturn(true);
        when(conversationRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId())).thenReturn(conversations);

        List<Conversation> result = conversationService.getUserConversations(testUser.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTopic()).isEqualTo(testConversation.getTopic());
        verify(conversationRepository).findByUserIdOrderByCreatedAtDesc(testUser.getId());
    }

    @Test
    void getUserConversationsShouldThrowExceptionForInvalidUser() {
        when(userRepository.existsById(testUser.getId())).thenReturn(false);

        assertThatThrownBy(() -> conversationService.getUserConversations(testUser.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

        verify(conversationRepository, never()).findByUserIdOrderByCreatedAtDesc(any());
    }

    @Test
    void getConversationByIdShouldReturnConversation() {
        when(conversationRepository.findById(testConversation.getId())).thenReturn(Optional.of(testConversation));

        Conversation result = conversationService.getConversationById(testConversation.getId());

        assertThat(result).isNotNull();
        assertThat(result.getTopic()).isEqualTo(testConversation.getTopic());
        verify(conversationRepository).findById(testConversation.getId());
    }

    @Test
    void getConversationByIdShouldThrowExceptionForInvalidId() {
        when(conversationRepository.findById(testConversation.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationService.getConversationById(testConversation.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Conversation not found");
    }
}