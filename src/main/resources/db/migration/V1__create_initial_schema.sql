CREATE TABLE llm_providers
(
    llm_provider_id BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create LLM Model table
CREATE TABLE llm_models
(
    llm_model_id             BIGSERIAL PRIMARY KEY,
    name                     VARCHAR(255)   NOT NULL,
    model_id                 VARCHAR(255)   NOT NULL,
    label                    VARCHAR(255)   NOT NULL,
    temperature              DOUBLE PRECISION,
    input_price_per_million  DECIMAL(10, 6) NOT NULL,
    output_price_per_million DECIMAL(10, 6) NOT NULL,
    llm_provider_id          BIGINT         NOT NULL,
    CONSTRAINT fk_llm_provider FOREIGN KEY (llm_provider_id) REFERENCES llm_providers (llm_provider_id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_llm_models_provider_id ON llm_models (llm_provider_id);
CREATE INDEX idx_llm_models_model_id ON llm_models (model_id);
CREATE INDEX idx_llm_models_name ON llm_models (name);

CREATE TABLE companies
(
    company_id             BIGSERIAL PRIMARY KEY,
    name                   VARCHAR(255) NOT NULL,
    llm_model_id           BIGINT REFERENCES llm_models (llm_model_id),
    monthly_spending_limit DECIMAL(10, 2),
    stripe_customer_id     VARCHAR(255) UNIQUE, -- Links to Stripe Customer
    created_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles
(
    role_id BIGSERIAL PRIMARY KEY,
    name    VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users
(
    user_id    BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    enabled    BOOLEAN   DEFAULT TRUE,
    company_id BIGINT REFERENCES companies (company_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles
(
    user_id BIGINT REFERENCES users (user_id),
    role_id BIGINT REFERENCES roles (role_id),
    PRIMARY KEY (user_id, role_id)
);
-- Create auth_providers table for OAuth2 authentication
CREATE TABLE auth_providers
(
    auth_provider_id     BIGSERIAL PRIMARY KEY,
    provider_name        VARCHAR(50) NOT NULL,
    provider_user_id     VARCHAR(255),
    provider_email       VARCHAR(255),
    provider_picture_url VARCHAR(1000),
    user_id              BIGINT,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT uk_provider_user UNIQUE (provider_name, provider_user_id)
);

-- Create indexes for faster lookups
CREATE INDEX idx_auth_providers_user_id ON auth_providers (user_id);
CREATE INDEX idx_auth_providers_provider_name_user_id ON auth_providers (provider_name, provider_user_id);
CREATE INDEX idx_auth_providers_provider_name_email ON auth_providers (provider_name, provider_email);

INSERT INTO roles (name)
VALUES ('SUPER_ADMIN'),
       ('ADMIN'),
       ('USER');

CREATE TABLE projects
(
    project_id BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    overview   TEXT         NOT NULL,
    company_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_company FOREIGN KEY (company_id) REFERENCES companies (company_id)
);

CREATE TABLE conversations
(
    conversation_id       BIGSERIAL PRIMARY KEY,
    purpose               VARCHAR(1000)       NOT NULL,
    project_id            BIGINT              NOT NULL REFERENCES projects (project_id),
    user_id               BIGINT              NOT NULL REFERENCES users (user_id),
    message_cache_counter BIGINT    DEFAULT 0 NOT NULL,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_conversations_project_by ON projects (project_id);
CREATE INDEX idx_conversations_created_by ON conversations (user_id);
CREATE INDEX idx_conversations_created_at ON conversations (created_at DESC);

CREATE TABLE assistants
(
    assistant_id     BIGSERIAL PRIMARY KEY,
    name             VARCHAR(100) NOT NULL,
    expertise_areas  TEXT         NOT NULL,
    expertise_prompt TEXT         NOT NULL,
    company_id       BIGINT,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_company FOREIGN KEY (company_id) REFERENCES companies (company_id)
);
-- Create the assistant_tone table
CREATE TABLE assistant_tone
(
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(50)  NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    prompt       TEXT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_time TIMESTAMP
);

CREATE TABLE conversation_assistant
(
    id              BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT  NOT NULL,
    assistant_id    BIGINT  NOT NULL,
    muted           BOOLEAN NOT NULL DEFAULT FALSE,
    tone_id         BIGINT,
    CONSTRAINT fk_tone FOREIGN KEY (tone_id) REFERENCES assistant_tone (id),
    CONSTRAINT fk_assistant FOREIGN KEY (assistant_id) REFERENCES assistants (assistant_id) ON DELETE CASCADE,
    CONSTRAINT fk_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (conversation_id) ON DELETE CASCADE,
    CONSTRAINT uk_assistant_conversation UNIQUE (assistant_id, conversation_id)
);

CREATE TABLE messages
(
    message_id      BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT    NOT NULL,
    assistant_id    BIGINT,
    user_id         BIGINT,
    content         TEXT      NOT NULL,
    deleted         BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP,
    CONSTRAINT fk_assistant FOREIGN KEY (assistant_id) REFERENCES assistants (assistant_id) ON DELETE CASCADE,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (conversation_id) ON DELETE CASCADE
);

CREATE INDEX idx_messages_deleted ON messages (deleted);
CREATE INDEX idx_messages_conversation_deleted ON messages (conversation_id, deleted);

CREATE TABLE bookmarks
(
    bookmark_id BIGSERIAL PRIMARY KEY,
    user_id     BIGINT    NOT NULL,
    message_id  BIGINT    NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_message FOREIGN KEY (message_id) REFERENCES messages (message_id) ON DELETE CASCADE,
    CONSTRAINT uk_user_message UNIQUE (user_id, message_id)
);

CREATE INDEX idx_bookmarks_user_id ON bookmarks (user_id);
CREATE INDEX idx_bookmarks_message_id ON bookmarks (message_id);
CREATE INDEX idx_bookmarks_created_at ON bookmarks (created_at DESC);

CREATE TABLE metrics
(
    metric_id       BIGSERIAL PRIMARY KEY,
    duration        BIGINT NOT NULL,
    input_tokens    INT    NOT NULL,
    output_tokens   INT    NOT NULL,
    llm_model_id    BIGINT REFERENCES llm_models (llm_model_id),
    additional_info TEXT,
    message_id      BIGINT UNIQUE,
    CONSTRAINT fk_message FOREIGN KEY (message_id) REFERENCES messages (message_id) ON DELETE CASCADE
);

CREATE TABLE system_settings
(
    system_setting_id  BIGINT PRIMARY KEY,
    llm_model_id       BIGINT REFERENCES llm_models (llm_model_id),
    summary_batch_size INT       NOT NULL DEFAULT 10,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE plans
(
    plan_id     BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    badge       VARCHAR(255),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE plan_details
(
    plan_detail_id         BIGSERIAL PRIMARY KEY,
    plan_id                BIGINT         NOT NULL REFERENCES plans (plan_id),
    effective_date         DATE           NOT NULL,
    monthly_price          DECIMAL(10, 2),
    monthly_spending_limit DECIMAL(10, 2) NOT NULL,
    created_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE subscriptions
(
    subscription_id        BIGSERIAL PRIMARY KEY,
    company_id             BIGINT NOT NULL REFERENCES companies (company_id),
    plan_id                BIGINT NOT NULL REFERENCES plans (plan_id),
    stripe_subscription_id VARCHAR(255) UNIQUE,
    start_date             DATE   NOT NULL,
    end_date               DATE,
    created_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payments
(
    payment_id               BIGSERIAL PRIMARY KEY,
    subscription_id          BIGINT         NOT NULL REFERENCES subscriptions (subscription_id),
    amount                   DECIMAL(10, 2) NOT NULL,
    payment_date             DATE           NOT NULL,
    stripe_payment_intent_id VARCHAR(255)   NOT NULL UNIQUE,
    stripe_payment_status    VARCHAR(50)    NOT NULL,
    created_at               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE metric_cache
(
    metric_cache_id     BIGSERIAL PRIMARY KEY,
    conversation_id     BIGINT NOT NULL REFERENCES conversations (conversation_id) ON DELETE CASCADE,
    llm_model_id        BIGINT REFERENCES llm_models (llm_model_id),
    total_duration      BIGINT NOT NULL DEFAULT 0,
    message_count       INT    NOT NULL DEFAULT 0,
    total_input_tokens  INT    NOT NULL DEFAULT 0,
    total_output_tokens INT    NOT NULL DEFAULT 0,
    created_at          TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_conversation_provider_model UNIQUE (conversation_id, llm_model_id)
);

-- Create indexes for better query performance
CREATE INDEX idx_plan_details_plan_id ON plan_details (plan_id);
CREATE INDEX idx_subscriptions_company_id ON subscriptions (company_id);
CREATE INDEX idx_subscriptions_plan_id ON subscriptions (plan_id);
CREATE INDEX idx_payments_subscription_id ON payments (subscription_id);

-- Create tables for Spring Session JDBC
CREATE TABLE spring_session
(
    primary_id            CHAR(36) NOT NULL,
    session_id            CHAR(36) NOT NULL,
    creation_time         BIGINT   NOT NULL,
    last_access_time      BIGINT   NOT NULL,
    max_inactive_interval INT      NOT NULL,
    expiry_time           BIGINT   NOT NULL,
    principal_name        VARCHAR(100),
    CONSTRAINT spring_session_pk PRIMARY KEY (primary_id)
);

CREATE UNIQUE INDEX spring_session_ix1 ON spring_session (session_id);
CREATE INDEX spring_session_ix2 ON spring_session (expiry_time);
CREATE INDEX spring_session_ix3 ON spring_session (principal_name);

CREATE TABLE spring_session_attributes
(
    session_primary_id CHAR(36)     NOT NULL,
    attribute_name     VARCHAR(200) NOT NULL,
    attribute_bytes    bytea        NOT NULL,
    CONSTRAINT spring_session_attributes_pk PRIMARY KEY (session_primary_id, attribute_name),
    CONSTRAINT spring_session_attributes_fk FOREIGN KEY (session_primary_id) REFERENCES spring_session (primary_id) ON DELETE CASCADE
);

CREATE INDEX spring_session_attributes_ix1 ON spring_session_attributes (session_primary_id);

CREATE TABLE point_in_time_summaries
(
    summary_id            BIGSERIAL PRIMARY KEY,
    conversation_id       BIGINT    NOT NULL,
    message_id            BIGINT    NOT NULL,
    topics_and_key_points TEXT      NOT NULL,
    topic_summaries       TEXT      NOT NULL,
    assistant_summaries   TEXT      NOT NULL,
    created_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active             BOOLEAN   NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (conversation_id) ON DELETE CASCADE,
    CONSTRAINT fk_message FOREIGN KEY (message_id) REFERENCES messages (message_id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_summaries_conversation_id ON point_in_time_summaries (conversation_id);
CREATE INDEX idx_summaries_message_id ON point_in_time_summaries (message_id);
CREATE INDEX idx_summaries_created_at ON point_in_time_summaries (created_at DESC);
CREATE INDEX idx_summaries_is_active ON point_in_time_summaries (is_active);

-- Create the audits table for tracking user actions
CREATE TABLE audits
(
    id          BIGSERIAL PRIMARY KEY,
    action_type VARCHAR(50)  NOT NULL,
    user_id     BIGINT,
    username    VARCHAR(255) NOT NULL,
    ip_address  VARCHAR(50),
    details     VARCHAR(1000),
    timestamp   TIMESTAMP    NOT NULL,
    entity_type VARCHAR(100),
    entity_id   BIGINT,
    company_id  BIGINT,

    -- Foreign key constraints
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_company FOREIGN KEY (company_id) REFERENCES companies (company_id) ON DELETE SET NULL
);

-- Create indexes for better query performance
CREATE INDEX idx_audit_user_id ON audits (user_id);
CREATE INDEX idx_audit_company_id ON audits (company_id);
CREATE INDEX idx_audit_action_type ON audits (action_type);
CREATE INDEX idx_audit_timestamp ON audits (timestamp);
CREATE INDEX idx_audit_entity ON audits (entity_type, entity_id);

-- Add a comment to the table
COMMENT ON TABLE audits IS 'Stores audit events for tracking user actions in the system';
