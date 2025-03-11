CREATE TABLE companies
(
    company_id             BIGSERIAL PRIMARY KEY,
    name                   VARCHAR(255) NOT NULL,
    llm_model              VARCHAR(255),
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
    conversation_id BIGSERIAL PRIMARY KEY,
    purpose         VARCHAR(1000) NOT NULL,
    project_id      BIGINT        NOT NULL REFERENCES projects (project_id),
    user_id         BIGINT        NOT NULL REFERENCES users (user_id),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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

CREATE TABLE conversation_assistant
(
    conversation_id BIGINT NOT NULL,
    assistant_id    BIGINT NOT NULL,
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
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assistant FOREIGN KEY (assistant_id) REFERENCES assistants (assistant_id) ON DELETE CASCADE,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (conversation_id) ON DELETE CASCADE
);
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
    provider        VARCHAR(255),
    model           VARCHAR(255),
    additional_info TEXT,
    message_id      BIGINT UNIQUE,
    CONSTRAINT fk_message FOREIGN KEY (message_id) REFERENCES messages (message_id) ON DELETE CASCADE
);

CREATE TABLE system_settings
(
    system_setting_id BIGINT PRIMARY KEY,
    llm_model         VARCHAR(255) NOT NULL,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
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
