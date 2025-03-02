//package ai.teamcollab.server.service.impl;
//
//import ai.teamcollab.server.domain.Conversation;
//import ai.teamcollab.server.domain.Message;
//import ai.teamcollab.server.domain.User;
//import ai.teamcollab.server.repository.MessageRepository;
//import ai.teamcollab.server.service.domain.MessageResponse;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.ai.chat.prompt.Prompt;
//import org.springframework.ai.openai.OpenAiChatModel;
//import org.springframework.ai.chat.messages.AssistantMessage;
//import org.springframework.ai.chat.ChatResponse;
//
//import java.time.LocalDateTime;
//import java.util.HashSet;
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ChatServiceImplTest {
//
//    @Mock
//    private MessageRepository messageRepository;
//
//    @Mock
//    private OpenAiChatModel chatModel;
//
//    private ChatServiceImpl chatService;
//
//    @BeforeEach
//    void setUp() {
//        chatService = new ChatServiceImpl(messageRepository, chatModel);
//    }
//
//    @Test
//    void process_WithValidConversationAndMessage_ReturnsResponse() {
//        // Arrange
//        Conversation conversation = new Conversation();
//        conversation.setId(1L);
//
//        User user = new User();
//        user.setId(1L);
//
//        Message message = new Message();
//        message.setId(1L);
//        message.setContent("Test message");
//        message.setUser(user);
//        message.setCreatedAt(LocalDateTime.now());
//        message.setConversation(conversation);
//
//        Set<Message> messages = new HashSet<>();
//        messages.add(message);
//        conversation.setMessages(messages);
//
//        ChatResponse mockResponse = mock(ChatResponse.class);
//        AssistantMessage mockAssistantMessage = new AssistantMessage("AI response");
//        when(mockResponse.getResult()).thenReturn(mockAssistantMessage);
//        doReturn(mockResponse).when(chatModel).call(any(Prompt.class));
//
//        // Act
//        MessageResponse result = chatService.process(conversation, message);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("AI response", result.getContent());
//        verify(messageRepository).save(message);
//        verify(chatModel).call(any(Prompt.class));
//    }
//
//    @Test
//    void process_WithNullConversation_ThrowsException() {
//        // Arrange
//        Message message = new Message();
//        message.setContent("Test message");
//
//        // Act & Assert
//        assertThrows(NullPointerException.class,
//                () -> chatService.process(null, message),
//                "Conversation cannot be null");
//    }
//
//    @Test
//    void process_WithNullMessage_ThrowsException() {
//        // Arrange
//        Conversation conversation = new Conversation();
//        conversation.setId(1L);
//
//        // Act & Assert
//        assertThrows(NullPointerException.class,
//                () -> chatService.process(conversation, null),
//                "Message cannot be null");
//    }
//
//    @Test
//    void process_WithConversationHistory_IncludesHistory() {
//        // Arrange
//        Conversation conversation = new Conversation();
//        conversation.setId(1L);
//
//        User user = new User();
//        user.setId(1L);
//
//        Message oldMessage = new Message();
//        oldMessage.setId(1L);
//        oldMessage.setContent("Old message");
//        oldMessage.setUser(user);
//        oldMessage.setCreatedAt(LocalDateTime.now().minusMinutes(5));
//        oldMessage.setConversation(conversation);
//
//        Message aiResponse = new Message();
//        aiResponse.setId(2L);
//        aiResponse.setContent("AI response to old message");
//        aiResponse.setCreatedAt(LocalDateTime.now().minusMinutes(4));
//        aiResponse.setConversation(conversation);
//
//        Message newMessage = new Message();
//        newMessage.setId(3L);
//        newMessage.setContent("New message");
//        newMessage.setUser(user);
//        newMessage.setCreatedAt(LocalDateTime.now());
//        newMessage.setConversation(conversation);
//
//        Set<Message> messages = new HashSet<>();
//        messages.add(oldMessage);
//        messages.add(aiResponse);
//        messages.add(newMessage);
//        conversation.setMessages(messages);
//
//        ChatResponse mockResponse = mock(ChatResponse.class);
//        AssistantMessage mockAssistantMessage = new AssistantMessage("New AI response");
//        when(mockResponse.getResult()).thenReturn(mockAssistantMessage);
//        doReturn(mockResponse).when(chatModel).call(any(Prompt.class));
//
//        // Act
//        MessageResponse result = chatService.process(conversation, newMessage);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("New AI response", result.getContent());
//        verify(messageRepository).save(newMessage);
//        verify(chatModel).call(any(Prompt.class));
//    }
//}
