package ai.teamcollab.server.service.impl;

import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.service.domain.ChatContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Responsible for building prompts for different purposes.
 */
@Component
@RequiredArgsConstructor
public class PromptBuilder {
    private final MessageFormatter messageFormatter;
    
    /**
     * Builds a prompt for processing a message.
     *
     * @param chatContext the chat context
     * @param recent the recent message to process
     * @return the built prompt
     */
    public Prompt buildMessagePrompt(ChatContext chatContext, Message recent) {
        final var messages = new ArrayList<org.springframework.ai.chat.messages.Message>();

        // Add system message with context
        messages.add(new SystemMessage(String.format(
                "Conversation Purpose: %s\nProject Overview: %s",
                chatContext.getPurpose(),
                chatContext.getProjectOverview()
        )));

        // Add conversation history
        for (final var historyMessage : chatContext.getLastMessages()) {
            if (historyMessage.equals(recent)) {
                continue; // Skip the current message
            }
            if (historyMessage.isAssistant()) {
                messages.add(new AssistantMessage(historyMessage.getContent()));
            } else {
                messages.add(new UserMessage(historyMessage.getContent()));
            }
        }

        messages.add(new UserMessage(recent.getContent()));
        
        return new Prompt(messages.toArray(new org.springframework.ai.chat.messages.Message[0]));
    }
    
    /**
     * Builds a prompt for extracting topics from a conversation.
     *
     * @param chatContext the chat context
     * @return the built prompt
     */
    public Prompt buildTopicsPrompt(ChatContext chatContext) {
        final var messages = new ArrayList<org.springframework.ai.chat.messages.Message>();

        // Add system message with instructions
        messages.add(new SystemMessage(
                "You are an expert at analyzing conversations and identifying the main topics and key points discussed. " +
                        "Extract a list of topics and key points from the conversation. " +
                        "Format your response as a bulleted list with main topics as headers and key points as sub-bullets. " +
                        "Be comprehensive but concise."
        ));

        // Add context
        messages.add(new UserMessage(String.format(
                "Conversation Purpose: %s\nProject Overview: %s\n\nPlease analyze the following conversation and extract the main topics and key points:",
                chatContext.getPurpose(),
                chatContext.getProjectOverview()
        )));

        // Add conversation history
        for (final var message : chatContext.getLastMessages()) {
            final var sender = messageFormatter.getSenderName(message);
            messages.add(new UserMessage(String.format("%s: %s", sender, message.getContent())));
        }

        return new Prompt(messages.toArray(new org.springframework.ai.chat.messages.Message[0]));
    }
    
    /**
     * Builds a prompt for summarizing topics in a conversation.
     *
     * @param chatContext the chat context
     * @return the built prompt
     */
    public Prompt buildTopicSummariesPrompt(ChatContext chatContext) {
        final var messages = new ArrayList<org.springframework.ai.chat.messages.Message>();

        // Add system message with instructions
        messages.add(new SystemMessage(
                "You are an expert at summarizing complex discussions. " +
                        "For each topic and key point in the conversation, provide a concise summary that captures the critical information. " +
                        "Focus on information that would be important for continuing the conversation. " +
                        "Format your response with topic headers and summaries as paragraphs."
        ));

        // Add context
        messages.add(new UserMessage(String.format(
                "Conversation Purpose: %s\nProject Overview: %s\n\nPlease summarize the following conversation by topic:",
                chatContext.getPurpose(),
                chatContext.getProjectOverview()
        )));

        // Add conversation history
        for (final var message : chatContext.getLastMessages()) {
            final var sender = messageFormatter.getSenderName(message);
            messages.add(new UserMessage(String.format("%s: %s", sender, message.getContent())));
        }

        return new Prompt(messages.toArray(new org.springframework.ai.chat.messages.Message[0]));
    }
    
    /**
     * Builds a prompt for summarizing assistant contributions in a conversation.
     *
     * @param conversation the conversation
     * @param chatContext the chat context
     * @return the built prompt
     */
    public Prompt buildAssistantSummariesPrompt(Conversation conversation, ChatContext chatContext) {
        final var messages = new ArrayList<org.springframework.ai.chat.messages.Message>();

        // Add system message with instructions
        messages.add(new SystemMessage(
                "You are an expert at analyzing conversations and understanding the role of different participants. " +
                        "For each assistant in the conversation, provide a summary of their contributions and the critical points related to their expertise. " +
                        "Format your response with assistant names as headers and summaries as paragraphs."
        ));

        // Add context about assistants
        final var assistantsInfo = new StringBuilder("Assistants in this conversation:\n");
        for (final var assistant : conversation.getAssistants()) {
            assistantsInfo.append(String.format("- %s: %s\n", assistant.getName(), assistant.getExpertise()));
        }

        messages.add(new UserMessage(String.format(
                "Conversation Purpose: %s\nProject Overview: %s\n\n%s\n\nPlease summarize the contributions of each assistant:",
                chatContext.getPurpose(),
                chatContext.getProjectOverview(),
                assistantsInfo
        )));

        // Add conversation history
        for (final var message : chatContext.getLastMessages()) {
            final var sender = messageFormatter.getSenderName(message);
            messages.add(new UserMessage(String.format("%s: %s", sender, message.getContent())));
        }

        return new Prompt(messages.toArray(new org.springframework.ai.chat.messages.Message[0]));
    }
}