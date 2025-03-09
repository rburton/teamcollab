CREATE TABLE companies
(
    company_id         BIGSERIAL PRIMARY KEY,
    name               VARCHAR(255) NOT NULL,
    llm_model          VARCHAR(255),
    stripe_customer_id VARCHAR(255) UNIQUE, -- Links to Stripe Customer
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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
    plan_detail_id BIGSERIAL PRIMARY KEY,
    plan_id        BIGINT         NOT NULL REFERENCES plans (plan_id),
    effective_date DATE           NOT NULL,
    monthly_price  DECIMAL(10, 2) NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE subscriptions
(
    subscription_id        BIGSERIAL PRIMARY KEY,
    company_id             BIGINT       NOT NULL REFERENCES companies (company_id),
    plan_id                BIGINT       NOT NULL REFERENCES plans (plan_id),
    stripe_subscription_id VARCHAR(255) NOT NULL UNIQUE,
    start_date             DATE         NOT NULL,
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
CREATE TABLE SPRING_SESSION (
                                PRIMARY_ID CHAR(36) NOT NULL,
                                SESSION_ID CHAR(36) NOT NULL,
                                CREATION_TIME BIGINT NOT NULL,
                                LAST_ACCESS_TIME BIGINT NOT NULL,
                                MAX_INACTIVE_INTERVAL INT NOT NULL,
                                EXPIRY_TIME BIGINT NOT NULL,
                                PRINCIPAL_NAME VARCHAR(100),
                                CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
);

CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE SPRING_SESSION_ATTRIBUTES (
                                           SESSION_PRIMARY_ID CHAR(36) NOT NULL,
                                           ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
                                           ATTRIBUTE_BYTES BYTEA NOT NULL,
                                           CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
                                           CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
);

CREATE INDEX SPRING_SESSION_ATTRIBUTES_IX1 ON SPRING_SESSION_ATTRIBUTES (SESSION_PRIMARY_ID);
