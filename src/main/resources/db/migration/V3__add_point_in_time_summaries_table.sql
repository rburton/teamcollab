CREATE TABLE point_in_time_summaries
(
    summary_id           BIGSERIAL PRIMARY KEY,
    conversation_id      BIGINT NOT NULL,
    message_id           BIGINT NOT NULL,
    topics_and_key_points TEXT NOT NULL,
    topic_summaries      TEXT NOT NULL,
    assistant_summaries  TEXT NOT NULL,
    created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active            BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (conversation_id) ON DELETE CASCADE,
    CONSTRAINT fk_message FOREIGN KEY (message_id) REFERENCES messages (message_id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_summaries_conversation_id ON point_in_time_summaries (conversation_id);
CREATE INDEX idx_summaries_message_id ON point_in_time_summaries (message_id);
CREATE INDEX idx_summaries_created_at ON point_in_time_summaries (created_at DESC);
CREATE INDEX idx_summaries_is_active ON point_in_time_summaries (is_active);