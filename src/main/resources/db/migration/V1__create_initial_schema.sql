CREATE TABLE companies
(
    company_id BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL
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
    enabled    BOOLEAN DEFAULT TRUE,
    company_id BIGINT REFERENCES companies (company_id)
);

CREATE TABLE user_roles
(
    user_id BIGINT REFERENCES users (user_id),
    role_id BIGINT REFERENCES roles (role_id),
    PRIMARY KEY (user_id, role_id)
);

INSERT INTO roles (name)
VALUES ('ADMIN'),
       ('USER');

CREATE TABLE conversations
(
    conversation_id BIGSERIAL PRIMARY KEY,
    topic           VARCHAR(255)  NOT NULL,
    purpose         VARCHAR(1000) NOT NULL,
    created_by      BIGINT        NOT NULL REFERENCES users (user_id),
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT topic_length_check CHECK (LENGTH(topic) >= 3),
    CONSTRAINT purpose_length_check CHECK (LENGTH(purpose) >= 10)
);

CREATE INDEX idx_conversations_created_by ON conversations (created_by);
CREATE INDEX idx_conversations_created_at ON conversations (created_at DESC);

CREATE TABLE personas
(
    persona_id      BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    expertise_areas TEXT         NOT NULL,
    company_id      BIGINT,
    CONSTRAINT fk_company FOREIGN KEY (company_id) REFERENCES companies (company_id)
);

-- Create persona_conversations join table
CREATE TABLE conversation_persona
(
    conversation_id BIGINT NOT NULL,
    persona_id      BIGINT NOT NULL,
    CONSTRAINT fk_persona FOREIGN KEY (persona_id) REFERENCES personas (persona_id) ON DELETE CASCADE,
    CONSTRAINT fk_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (conversation_id) ON DELETE CASCADE,
    CONSTRAINT uk_persona_conversation UNIQUE (persona_id, conversation_id)
);

INSERT INTO personas (name, expertise_areas)
VALUES ('Mary', 'Marketing'),
       ('Jack', 'Product Management'),
       ('Frank', 'Legal');