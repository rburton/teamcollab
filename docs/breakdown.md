# Breaking Down ChatServiceImpl for Better Maintainability

## Current Issues with ChatServiceImpl

The current `ChatServiceImpl` class has several issues that make it difficult to maintain:

1. **Size**: At 372 lines, the class is too large, making it hard to understand and modify.
2. **Multiple Responsibilities**: The class handles several distinct responsibilities:
   - Processing chat messages
   - Generating conversation summaries
   - Creating AI prompts
   - Managing AI model configuration
   - Handling metrics collection
3. **Tight Coupling**: The class directly depends on multiple repositories and services.
4. **Complex Methods**: Some methods are quite long and handle multiple concerns.
5. **Duplication**: There's some duplication in code patterns, especially around AI model configuration and prompt creation.

## Proposed Breakdown Structure

I recommend breaking down `ChatServiceImpl` into the following components:

### 1. ChatServiceImpl (Coordinator)

A slimmed-down version that orchestrates the interactions between the specialized components:

```java
@Service
public class ChatServiceImpl implements ChatService {
    private final MessageProcessor messageProcessor;
    private final SummaryGenerator summaryGenerator;

    // Implement the interface methods by delegating to specialized components
}
```

### 2. MessageProcessor

Responsible for processing chat messages:

```java
@Component
public class MessageProcessor {
    private final MessageRepository messageRepository;
    private final AiModelFactory aiModelFactory;
    private final PromptBuilder promptBuilder;

    public CompletableFuture<MessageResponse> process(Conversation conversation, 
                                                     Message recent, 
                                                     ChatContext chatContext) {
        // Process the message and return a response
    }
}
```

### 3. SummaryGenerator

Responsible for generating conversation summaries:

```java
@Component
public class SummaryGenerator {
    private final PointInTimeSummaryRepository pointInTimeSummaryRepository;
    private final MessageRepository messageRepository;
    private final AiModelFactory aiModelFactory;
    private final PromptBuilder promptBuilder;

    public CompletableFuture<PointInTimeSummary> generateSummary(Conversation conversation, 
                                                                ChatContext chatContext) {
        // Generate a summary and return it
    }
}
```

### 4. PromptBuilder

Responsible for building prompts for different purposes:

```java
@Component
public class PromptBuilder {
    public Prompt buildMessagePrompt(ChatContext chatContext, Message recent) {
        // Build a prompt for processing a message
    }

    public Prompt buildTopicsPrompt(ChatContext chatContext) {
        // Build a prompt for extracting topics
    }

    public Prompt buildTopicSummariesPrompt(ChatContext chatContext) {
        // Build a prompt for summarizing topics
    }

    public Prompt buildAssistantSummariesPrompt(Conversation conversation, ChatContext chatContext) {
        // Build a prompt for summarizing assistant contributions
    }
}
```

### 5. AiModelFactory

Responsible for creating and configuring AI models:

```java
@Component
public class AiModelFactory {
    private final SystemSettingsService systemSettingsService;

    public OpenAiChatModel createModel(Conversation conversation) {
        // Create and configure an AI model based on the conversation and system settings
    }
}
```

### 6. MessageFormatter

Utility class for formatting messages:

```java
@Component
public class MessageFormatter {
    public String formatUserMessage(Message message) {
        // Format a user message
    }

    public String formatAssistantMessage(Message message) {
        // Format an assistant message
    }
}
```

## Benefits of the Proposed Changes

Breaking down `ChatServiceImpl` into these components offers several benefits:

1. **Improved Maintainability**: Smaller, focused classes are easier to understand and modify.
2. **Better Testability**: Each component can be tested in isolation.
3. **Enhanced Reusability**: Components like `PromptBuilder` and `AiModelFactory` can be reused in other parts of the application.
4. **Clearer Responsibilities**: Each class has a single, well-defined responsibility.
5. **Easier Onboarding**: New developers can understand the system more quickly.
6. **Simplified Evolution**: The system can evolve more easily as requirements change.

## Implementation Approach

To implement these changes:

1. **Create New Classes**: Create each of the new component classes.
2. **Move Logic**: Move the relevant logic from `ChatServiceImpl` to each component.
3. **Update Dependencies**: Update the dependencies in each class.
4. **Refactor ChatServiceImpl**: Refactor `ChatServiceImpl` to use the new components.
5. **Update Tests**: Update existing tests to work with the new structure.
6. **Add New Tests**: Add new tests for each component.

## Specific Refactoring Examples

### Example 1: Moving Prompt Building Logic

Current:
```java
private Prompt createTopicsPrompt(ChatContext chatContext) {
    // Complex prompt building logic
}
```

Refactored:
```java
// In PromptBuilder
public Prompt buildTopicsPrompt(ChatContext chatContext) {
    // Same logic, but in a dedicated class
}

// In SummaryGenerator
final var topicsPrompt = promptBuilder.buildTopicsPrompt(chatContext);
```

### Example 2: Moving AI Model Configuration

Current:
```java
final var model = GptModel.fromId(llmModel);
final var chatModel = OpenAiChatModel.builder()
        .openAiApi(OpenAiApi.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .build())
        .defaultOptions(OpenAiChatOptions.builder()
                .model(model.getId())
                .temperature(model.getTemperature())
                .build())
        .build();
```

Refactored:
```java
// In AiModelFactory
public OpenAiChatModel createModel(Conversation conversation) {
    final var currentSettings = systemSettingsService.getCurrentSettings();
    final var llmModel = Optional.ofNullable(conversation)
            .map(Conversation::getUser)
            .map(User::getCompany)
            .map(Company::getLlmModel)
            .filter(Strings::isNotBlank)
            .orElse(currentSettings.getLlmModel());

    final var model = GptModel.fromId(llmModel);
    return OpenAiChatModel.builder()
            .openAiApi(OpenAiApi.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .build())
            .defaultOptions(OpenAiChatOptions.builder()
                    .model(model.getId())
                    .temperature(model.getTemperature())
                    .build())
            .build();
}

// In MessageProcessor or SummaryGenerator
final var chatModel = aiModelFactory.createModel(conversation);
```

## Conclusion

By breaking down `ChatServiceImpl` into these smaller, more focused components, we can significantly improve the maintainability of the codebase. Each component has a clear responsibility, making the system easier to understand, test, and evolve.

## Implementation Status

The refactoring has been completed with the following changes:

1. Created `AiModelFactory` to handle AI model creation and configuration
2. Created `MessageFormatter` to handle message formatting
3. Created `PromptBuilder` to handle prompt building for different purposes
4. Created `MessageProcessor` to handle message processing
5. Created `SummaryGenerator` to handle summary generation
6. Refactored `ChatServiceImpl` to delegate to these specialized components

The new structure is much more maintainable and follows the Single Responsibility Principle. Each class has a clear, focused responsibility, making the codebase easier to understand, test, and evolve.
