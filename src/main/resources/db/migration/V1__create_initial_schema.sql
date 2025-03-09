CREATE TABLE companies
(
    company_id         BIGSERIAL PRIMARY KEY,
    name               VARCHAR(255) NOT NULL,
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

CREATE TABLE personas
(
    persona_id       BIGSERIAL PRIMARY KEY,
    name             VARCHAR(100) NOT NULL,
    expertise_areas  TEXT         NOT NULL,
    expertise_prompt TEXT         NOT NULL,
    company_id       BIGINT,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_company FOREIGN KEY (company_id) REFERENCES companies (company_id)
);

CREATE TABLE conversation_persona
(
    conversation_id BIGINT NOT NULL,
    persona_id      BIGINT NOT NULL,
    CONSTRAINT fk_persona FOREIGN KEY (persona_id) REFERENCES personas (persona_id) ON DELETE CASCADE,
    CONSTRAINT fk_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (conversation_id) ON DELETE CASCADE,
    CONSTRAINT uk_persona_conversation UNIQUE (persona_id, conversation_id)
);

INSERT INTO personas (name, expertise_areas, expertise_prompt)
VALUES ('Dash', 'Marketing', 'Marketing expert with deep knowledge of branding, customer acquisition strategies, and digital campaign optimization. Specializing in leveraging data insights to craft effective marketing plans that drive business growth and engagement.'),
       ('Eli', 'Product Management', 'Excels at defining product vision, strategy, and roadmaps, ensuring alignment with customer needs and business objectives. They possess strong skills in market research, stakeholder collaboration, and prioritizing features to drive product development and growth.'),
       ('Leo', 'Growth Hacker', 'Specializes in using creative, data-driven strategies to rapidly scale user acquisition and engagement. Leveraging digital marketing, A/B testing, and automation to optimize growth channels while minimizing costs and maximizing results.'),
       ('Rich', 'CTO', 'Responsible for overseeing the technology strategy, ensuring the technical vision aligns with the company’s goals, and leading the development of scalable, innovative products. Their expertise includes managing engineering teams, evaluating emerging technologies, and making critical decisions on architecture and infrastructure.'),
       ('Ken', 'CFO', 'Skilled in financial planning, analysis, and strategy, ensuring the company’s financial health through effective budgeting, forecasting, and risk management. Oversees accounting operations, financial reporting, and investment decisions to align financial goals with overall business objectives.'),
       ('Riley', 'HR', 'Expert specializes in recruiting, talent management, and employee relations, ensuring a productive and positive work environment. They excel at developing company policies, handling performance management, and fostering a strong organizational culture.'),
       ('Max', 'Content Creator', 'Excels at producing engaging and high-quality content across multiple platforms, including blogs, videos, and social media. They have a strong understanding of audience behavior, SEO best practices, and storytelling, ensuring content resonates with target audiences and drives brand awareness.'),
       ('Mandy', 'Marketing Lead', 'Excels at developing and executing comprehensive marketing strategies to drive customer acquisition and brand awareness. They are skilled in data analysis, campaign management, and team leadership, ensuring alignment with business objectives and optimizing marketing efforts across multiple channels.'),
       ('Lily', 'Sales Lead/Business Development', 'Excels at identifying new business opportunities, building relationships, and driving revenue growth through strategic partnerships and customer acquisition. They possess strong negotiation, communication, and market analysis skills to effectively position the company’s products or services and expand its market presence.')
;

CREATE TABLE messages
(
    message_id      BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT    NOT NULL,
    persona_id      BIGINT,
    user_id         BIGINT,
    content         TEXT      NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_persona FOREIGN KEY (persona_id) REFERENCES personas (persona_id) ON DELETE CASCADE,
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

-- Insert default settings
INSERT INTO system_settings (system_setting_id, llm_model)
VALUES (1, 'gpt-3.5-turbo');
