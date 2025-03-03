# WebSocket Migration Guide for ConversationController

## Table of Contents
1. [Overview](#overview)
2. [Benefits](#benefits)
3. [Implementation Timeline](#implementation-timeline)
4. [Risks and Mitigation Strategies](#risks-and-mitigation-strategies)
   - [Technical Risks](#technical-risks)
   - [Implementation Risks](#implementation-risks)
   - [Mitigation Timeline](#mitigation-timeline)
5. [Prerequisites](#prerequisites)
6. [Migration Steps](#migration-steps)
   - [Dependencies](#1-add-dependencies)
   - [WebSocket Configuration](#2-websocket-configuration)
   - [Message DTOs](#3-create-websocket-message-dtos)
   - [Controller Changes](#4-modify-conversationcontroller)
   - [Client Implementation](#5-update-client-side-implementation)
   - [Error Handling](#6-error-handling)
7. [Testing and Verification](#testing-and-verification)
   - [Unit Testing](#unit-testing)
   - [Integration Testing](#integration-testing)
   - [Manual Testing](#manual-testing-checklist)
   - [Monitoring](#monitoring)
8. [Rollback Plan](#rollback-plan)

## Overview
This document outlines the steps required to migrate the ConversationController's postMessage functionality from HTTP to WebSocket implementation. This migration will enable real-time message delivery and improve the overall chat experience.

## Benefits
- Real-time message delivery
- Reduced server load
- Better user experience
- Improved scalability
- Lower latency

## Implementation Timeline
1. Phase 1: Setup and Configuration (1-2 days)
   - Add WebSocket dependencies
   - Configure WebSocket endpoints
   - Set up basic message handling

2. Phase 2: Core Implementation (2-3 days)
   - Implement WebSocket controller
   - Update service layer
   - Add client-side WebSocket handling

3. Phase 3: UI Implementation (2-3 days)
   - Update HTML templates
   - Implement real-time updates
   - Add error handling and reconnection

4. Phase 4: Testing and Monitoring (2-3 days)
   - Implement unit tests
   - Set up monitoring
   - Perform integration testing

Total estimated time: 7-11 days

## Risks and Mitigation Strategies

### Technical Risks
1. WebSocket Connection Stability
   - Risk: Unstable connections in poor network conditions
   - Mitigation: Implement robust reconnection logic and fallback to HTTP polling

2. Browser Compatibility
   - Risk: Older browsers might not support WebSocket
   - Mitigation: Use SockJS for fallback support

3. Server Resources
   - Risk: High number of concurrent WebSocket connections
   - Mitigation: Implement connection limits and monitoring

### Implementation Risks
1. Data Loss During Migration
   - Risk: Messages lost during transition
   - Mitigation: Implement dual-write period with both HTTP and WebSocket

2. Performance Impact
   - Risk: Unexpected performance degradation
   - Mitigation: Thorough performance testing and monitoring

3. Security Vulnerabilities
   - Risk: New attack vectors through WebSocket
   - Mitigation: Implement proper authentication and message validation

### Mitigation Timeline
- Week 1: Implement core features with basic error handling
- Week 2: Add comprehensive error handling and monitoring
- Week 3: Performance testing and security hardening

## Prerequisites
- Spring Boot 3.x
- Java 21
- Basic understanding of WebSocket protocol
- Spring WebSocket dependency
- STOMP WebSocket messaging
- SockJS client library

## Migration Steps

### 1. Add Dependencies
Add the following to pom.xml:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

### 2. WebSocket Configuration
Create WebSocketConfig class:
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
               .withSockJS();
    }
}
```

### 3. Create WebSocket Message DTOs
```java
@Data
@Builder
public class ChatMessage {
    private Long conversationId;
    private String content;
    private String sender;
    private MessageType type;
}

public enum MessageType {
    CHAT,
    JOIN,
    LEAVE
}
```

### 4. Modify ConversationController
Replace the existing postMessage endpoint with WebSocket handler:
```java
@MessageMapping("/chat.sendMessage/{conversationId}")
@SendTo("/topic/conversation/{conversationId}")
public ChatMessage sendMessage(@DestinationVariable Long conversationId,
                             @Payload ChatMessage chatMessage,
                             @AuthenticationPrincipal User user) {
    Message message = Message.builder()
            .content(chatMessage.getContent())
            .build();

    conversationService.sendMessage(conversationId, message, user);

    return chatMessage;
}

@MessageMapping("/chat.addUser/{conversationId}")
@SendTo("/topic/conversation/{conversationId}")
public ChatMessage addUser(@DestinationVariable Long conversationId,
                         @Payload ChatMessage chatMessage,
                         SimpMessageHeaderAccessor headerAccessor) {
    headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
    headerAccessor.getSessionAttributes().put("conversationId", conversationId);
    return chatMessage;
}
```

### 5. Update Client-Side Implementation
Update conversations/show.html to include the conversation ID and WebSocket implementation:

First, add the required WebSocket client libraries and styles to the HTML head:
```html
<!-- WebSocket Dependencies -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.5.1/sockjs.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>

<!-- Chat Styles -->
<style>
    .message-container {
        height: 400px;
        overflow-y: auto;
        border: 1px solid #ddd;
        padding: 1rem;
        margin-bottom: 1rem;
        border-radius: 0.5rem;
    }

    .message {
        margin-bottom: 0.5rem;
        padding: 0.5rem;
        border-radius: 0.25rem;
    }

    .chat-message {
        background-color: #f8f9fa;
    }

    .join-message {
        color: #28a745;
        font-style: italic;
    }

    .leave-message {
        color: #dc3545;
        font-style: italic;
    }

    .message-form {
        display: flex;
        gap: 0.5rem;
    }

    .message-input {
        flex: 1;
        padding: 0.5rem;
        border: 1px solid #ddd;
        border-radius: 0.25rem;
    }

    .send-button {
        padding: 0.5rem 1rem;
        background-color: #007bff;
        color: white;
        border: none;
        border-radius: 0.25rem;
        cursor: pointer;
    }

    .send-button:hover {
        background-color: #0056b3;
    }
</style>
```

Then, add the required HTML structure:
```html
<!-- Hidden input for conversation ID -->
<input type="hidden" id="conversation-id" th:value="${conversation.id}" />

<!-- Message container -->
<div id="message-container" class="message-container">
    <!-- Existing messages will be loaded here -->
    <div th:each="message : ${messages}" class="message" th:classappend="${message.type.toLowerCase() + '-message'}">
        <strong th:text="${message.sender}"></strong>
        <span th:text="${message.content}"></span>
    </div>
</div>

<!-- Message input form -->
<form id="message-form" class="message-form" onsubmit="sendMessage(); return false;">
    <input type="text" id="messageInput" class="message-input" placeholder="Type a message..." required />
    <button type="submit" class="send-button">Send</button>
</form>
```

Finally, add the WebSocket implementation:
```javascript
var stompClient = null;
var conversationId = document.getElementById('conversation-id').value; // Get conversation ID from a hidden input field

function connect() {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/conversation/' + conversationId, function(message) {
            showMessage(JSON.parse(message.body));
        });

        stompClient.send("/app/chat.addUser/" + conversationId, {},
            JSON.stringify({
                sender: username,
                type: 'JOIN'
            })
        );
    }, function(error) {
        console.error('STOMP error:', error);
        setTimeout(function() {
            console.log('Attempting to reconnect...');
            connect();
        }, 5000);
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
        console.log("Disconnected");
    }
}

function sendMessage() {
    stompClient.send("/app/chat.sendMessage/" + conversationId, {},
        JSON.stringify({
            content: messageInput.value,
            sender: username,
            type: 'CHAT'
        })
    );
}

// Initialize WebSocket connection when the page loads
document.addEventListener('DOMContentLoaded', function() {
    connect();
});

// Cleanup when the page is closed or navigated away from
window.addEventListener('beforeunload', function() {
    disconnect();
});

// Function to display incoming messages
function showMessage(message) {
    // Implementation depends on your UI structure
    // Example implementation:
    const messageContainer = document.getElementById('message-container');
    const messageElement = document.createElement('div');
    messageElement.className = 'message';

    if (message.type === 'JOIN') {
        messageElement.className += ' join-message';
        messageElement.textContent = `${message.sender} joined the conversation`;
    } else if (message.type === 'LEAVE') {
        messageElement.className += ' leave-message';
        messageElement.textContent = `${message.sender} left the conversation`;
    } else {
        messageElement.className += ' chat-message';
        messageElement.innerHTML = `
            <strong>${message.sender}:</strong>
            <span>${message.content}</span>
        `;
    }

    messageContainer.appendChild(messageElement);
    messageContainer.scrollTop = messageContainer.scrollHeight;
}
```

### 6. Error Handling
Implement WebSocket error handling:
```java
@Component
public class WebSocketEventListener {

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("Received a new web socket connection");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        Long conversationId = (Long) headerAccessor.getSessionAttributes().get("conversationId");

        if(username != null) {
            logger.info("User Disconnected : " + username);

            ChatMessage chatMessage = ChatMessage.builder()
                .type(MessageType.LEAVE)
                .sender(username)
                .build();

            messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, chatMessage);
        }
    }
}
```

### 7. Testing Strategy
1. Unit Tests:
   - Test WebSocket message handlers
   - Test connection/disconnection events
   - Test message broadcasting

2. Integration Tests:
   - Test end-to-end WebSocket communication
   - Test multiple client scenarios
   - Test reconnection scenarios

### 8. Security Considerations
1. Implement WebSocket security configuration:
```java
@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
            .simpDestMatchers("/app/**").authenticated()
            .simpSubscribeDestMatchers("/topic/**").authenticated()
            .anyMessage().denyAll();
    }
}
```

2. Add CSRF protection for WebSocket connections

### 9. Monitoring and Logging
1. Implement WebSocket interceptors for logging
2. Add metrics for WebSocket connections and messages
3. Set up monitoring for WebSocket sessions

## Testing and Verification

### Unit Testing
Create the following test cases:
```java
@SpringBootTest
class WebSocketChatTest {
    @Test
    void testWebSocketConnection() {
        // Test WebSocket connection establishment
    }

    @Test
    void testMessageDelivery() {
        // Test message sending and receiving
    }

    @Test
    void testUserJoinLeave() {
        // Test user join/leave notifications
    }

    @Test
    void testReconnection() {
        // Test automatic reconnection
    }
}
```

### Integration Testing
1. Multiple User Scenarios:
   - Test concurrent users in same conversation
   - Verify message ordering
   - Check user join/leave notifications

2. Performance Testing:
   - Test with high message frequency
   - Measure message delivery latency
   - Monitor server resource usage

3. Error Scenarios:
   - Network disconnection handling
   - Server restart recovery
   - Invalid message handling

### Manual Testing Checklist
- [ ] Messages appear in real-time
- [ ] User join/leave notifications work
- [ ] Message history loads correctly
- [ ] UI updates smoothly
- [ ] Error messages are displayed properly
- [ ] Reconnection works automatically
- [ ] Security measures are effective

### Monitoring
1. Add the following metrics:
   - WebSocket connection count
   - Message delivery latency
   - Error rate
   - Server resource usage

2. Set up alerts for:
   - High error rates
   - Connection failures
   - Unusual message patterns
   - Resource constraints

## Rollback Plan
1. Keep HTTP endpoints temporarily
2. Implement feature toggle for WebSocket
3. Monitor WebSocket performance and errors
4. Prepare rollback scripts if needed
