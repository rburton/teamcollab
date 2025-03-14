# Chat Summary Research

## Classes to be Modified

Based on the analysis of the codebase, the following classes need to be modified to support chat summary functionality:

1. **ChatService/ChatServiceImpl**
   - The interface and implementation that define the core chat functionality
   - Need to modify to include the current point-in-time summary in the context

2. **SummaryGenerator**
   - Responsible for generating conversation summaries
   - Need to modify to include the current point-in-time summary in the generation process

3. **PromptBuilder**
   - Builds prompts for different purposes, including summary generation
   - Need to modify the existing prompts or create new ones to include the current point-in-time summary

4. **MessageProcessor**
   - Processes messages in conversations
   - Need to modify to include the current point-in-time summary in the context

5. **ConversationService**
   - Manages conversations
   - Need to modify to retrieve and include the current point-in-time summary when processing messages

## Prompt Engineering for Conversation Summarization

### 1. Topics and Key Points Extraction Prompt

```
You are an expert at analyzing conversations and identifying the main topics and key points discussed.

Extract a list of topics and key points from the conversation. Format your response as a bulleted list with main topics as headers and key points as sub-bullets.

Be comprehensive but concise, focusing on information that would be important for continuing the conversation.

If a previous summary is provided, focus on new topics and key points that have emerged since that summary was created.

Conversation Purpose: {purpose}
Project Overview: {projectOverview}
Previous Summary: {previousSummary}

Please analyze the following conversation and extract the main topics and key points:
```

### 2. Topic Summaries Prompt

```
You are an expert at summarizing complex discussions.

For each topic and key point in the conversation, provide a concise summary that captures the critical information. Focus on information that would be important for continuing the conversation.

Format your response with topic headers and summaries as paragraphs.

If a previous summary is provided, focus on updating or expanding that summary with new information from the recent messages.

Conversation Purpose: {purpose}
Project Overview: {projectOverview}
Previous Summary: {previousSummary}

Please summarize the following conversation by topic:
```

### 3. Assistant Summaries Prompt

```
You are an expert at analyzing conversations and understanding the role of different participants.

For each assistant in the conversation, provide a summary of their contributions and the critical points related to their expertise. Format your response with assistant names as headers and summaries as paragraphs.

Your summary should reflect the perspective of each assistant, highlighting their unique insights and expertise.

If a previous summary is provided, focus on updating or expanding that summary with new contributions from each assistant.

Conversation Purpose: {purpose}
Project Overview: {projectOverview}
Previous Summary: {previousSummary}

Assistants in this conversation:
{assistantsInfo}

Please summarize the contributions of each assistant:
```

### 4. Message Processing Prompt with Summary Context

```
You are an AI assistant participating in a conversation.

Conversation Purpose: {purpose}
Project Overview: {projectOverview}
Current Conversation Summary:
{currentSummary}

Based on the conversation history and the current summary, provide a thoughtful and helpful response to the user's latest message.

Your response should be consistent with the conversation history and build upon the current summary.
```

## Implementation Considerations

1. **Incremental Summarization**
   - The system should support incremental summarization, where new summaries build upon previous ones rather than starting from scratch each time.
   - This requires storing and retrieving the most recent summary for a conversation.

2. **Assistant Perspective**
   - Summaries should be generated from the perspective of each assistant in the conversation.
   - This requires understanding each assistant's expertise and role in the conversation.

3. **Performance Optimization**
   - Generating summaries can be computationally expensive, so the system should optimize when and how summaries are generated.
   - The current approach of generating summaries after a batch of messages is a good starting point.

4. **User Experience**
   - Consider how summaries will be presented to users, if at all.
   - Summaries could be used to help users catch up on long conversations or to provide context for new participants.