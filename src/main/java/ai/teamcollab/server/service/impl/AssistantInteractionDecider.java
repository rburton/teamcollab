package ai.teamcollab.server.service.impl;

import ai.teamcollab.server.domain.Assistant;
import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.service.SystemSettingsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;

/**
 * Decides which assistants should respond to a message based on content analysis. Uses LLM to determine if assistants
 * were mentioned by name, if the question is related to their expertise, or if the question was directed to everyone.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssistantInteractionDecider {
    private final AiModelFactory aiModelFactory;
    private final SystemSettingsService systemSettingsService;
    private final ObjectMapper objectMapper;

    /**
     * Determines which assistants should respond to a message.
     *
     * @param conversation     the conversation
     * @param message          the message to analyze
     * @param activeAssistants list of active (not muted) assistants in the conversation
     * @return list of assistant IDs that should respond
     */
    public List<Long> decideAssistantsToRespond(@NonNull Conversation conversation, @NonNull Message message, @NonNull Collection<Assistant> activeAssistants) {
        if (activeAssistants.isEmpty()) {
            log.debug("No active assistants in conversation {}", conversation.getId());
            return Collections.emptyList();
        }

        try {
            // Build the prompt for the LLM
            final var prompt = buildPrompt(message, activeAssistants);

            // Get the LLM model specifically for assistant interaction decisions
            var chatModel = aiModelFactory.createAssistantInteractionModel();

            // Call the LLM
            var response = chatModel.call(prompt);
            var result = response.getResult().getOutput().getText();

            log.debug("LLM response for assistant interaction decision: {}", result);

            // Parse the response to get the list of assistants that should respond
            return parseResponse(result, activeAssistants);
        } catch (Exception e) {
            log.error("Error deciding which assistants should respond: {}", e.getMessage(), e);
            // In case of error, return all active assistants to ensure the conversation continues
            return activeAssistants.stream()
                    .map(Assistant::getId)
                    .toList();
        }
    }

    /**
     * Builds a prompt for the LLM to determine which assistants should respond.
     *
     * @param message          the message to analyze
     * @param activeAssistants list of active assistants in the conversation
     * @return the built prompt
     */
    private Prompt buildPrompt(@NonNull Message message, @NonNull Collection<Assistant> activeAssistants) {
        var messages = new ArrayList<org.springframework.ai.chat.messages.Message>();

        // Create a system message with instructions

        final var systemPrompt = """
                You are an expert at analyzing messages and determining which assistants should respond.
                Analyze the user message and determine which assistants should respond based on the following criteria:

                1. If an assistant is mentioned by name
                2. If the message contains a question related to an assistant's expertise
                3. If the message contains a question directed to everyone in the chat

                Respond with a JSON array of objects with the following structure:
                [
                  {
                    "assistantId": <assistant_id>,
                    "triggered": <true|false>
                  },
                  ...
                ]

                Only include assistants that should respond (triggered=true) in your response.
                """;

        messages.add(new SystemMessage(systemPrompt));

        // Create a user message with the message content and assistant information
        var userPrompt = new StringBuilder();
        userPrompt.append("User message: ").append(message.getContent()).append("\n\n");
        userPrompt.append("Available assistants:\n");

        for (var assistant : activeAssistants) {
            userPrompt.append("- ID: ").append(assistant.getId())
                    .append(", Name: ").append(assistant.getName())
                    .append(", Expertise: ").append(assistant.getExpertise())
                    .append("\n");
        }

        messages.add(new UserMessage(userPrompt.toString()));

        return new Prompt(messages);
    }

    /**
     * Parses the LLM response to extract the list of assistant IDs that should respond.
     *
     * @param response         the LLM response
     * @param activeAssistants list of active assistants in the conversation
     * @return list of assistant IDs that should respond
     */
    private List<Long> parseResponse(@NonNull String response, @NonNull Collection<Assistant> activeAssistants) {
        try {
            // Extract JSON array from response (in case there's additional text)
            String jsonResponse = extractJsonArray(response);

            // Parse the JSON response
            List<Map<String, Object>> assistantResponses = objectMapper.readValue(
                    jsonResponse, new TypeReference<List<Map<String, Object>>>() {
                    });

            // Extract assistant IDs where triggered is true
            return assistantResponses.stream()
                    .filter(map -> TRUE.equals(map.get("triggered")))
                    .map(map -> {
                        // Handle different types of ID representation (Long, Integer, String)
                        final var idObj = map.get("assistantId");
                        if (idObj instanceof Number) {
                            return ((Number) idObj).longValue();
                        } else if (idObj instanceof String) {
                            return Long.parseLong((String) idObj);
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .toList();
        } catch (JsonProcessingException e) {
            log.error("Error parsing LLM response: {}", e.getMessage(), e);
            // In case of parsing error, return all active assistants
            return activeAssistants.stream().map(Assistant::getId).collect(Collectors.toList());
        }
    }

    /**
     * Extracts a JSON array from a string that might contain additional text.
     *
     * @param text the text to extract from
     * @return the extracted JSON array
     */
    private String extractJsonArray(@NonNull String text) {
        // Find the start and end of the JSON array
        final var start = text.indexOf('[');
        final var end = text.lastIndexOf(']') + 1;

        if (start >= 0 && end > start) {
            return text.substring(start, end);
        }

        // If no JSON array is found, return an empty array
        return "[]";
    }
}
