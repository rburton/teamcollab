-- Create LLM Provider table
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

-- Insert initial data for OpenAI provider
INSERT INTO llm_providers (name, updated_at)
VALUES ('OpenAI', CURRENT_TIMESTAMP);

-- Insert initial data for Gemini provider
INSERT INTO llm_providers (name, updated_at)
VALUES ('Gemini', CURRENT_TIMESTAMP);

-- Get the provider IDs
DO
$$
    DECLARE
        openai_id BIGINT;
        gemini_id BIGINT;
    BEGIN
        SELECT llm_provider_id INTO openai_id FROM llm_providers WHERE name = 'OpenAI';
        SELECT llm_provider_id INTO gemini_id FROM llm_providers WHERE name = 'Gemini';

        -- Insert OpenAI models
        INSERT INTO llm_models (name, model_id, label, temperature, input_price_per_million, output_price_per_million,
                                llm_provider_id)
        VALUES ('ChatGPT-4o Latest', 'chatgpt-4o-latest', 'ChatGPT-4o Latest', 0.7, 5.00, 15.00, openai_id),
               ('ChatGPT-4o', 'gpt-4o', 'ChatGPT-4o', 0.7, 2.50, 10.00, openai_id),
               ('ChatGPT-4o Mini', 'gpt-4o-mini', 'ChatGPT-4o Mini', 0.7, 0.15, 0.60, openai_id),
               ('O1', 'o1', 'O1', 0.7, 15.00, 60.00, openai_id),
               ('O1 Mini', 'o1-mini', 'O1 Mini', 0.7, 1.10, 4.40, openai_id),
               ('O3 Mini', 'o3-mini', 'O3 Mini', NULL, 1.10, 4.40, openai_id),
               ('GPT-3.5 Turbo', 'gpt-3.5-turbo', 'GPT-3.5 Turbo', 0.7, 1.50, 2.00, openai_id),
               ('GPT-3.5 Turbo 16K', 'gpt-3.5-turbo-16k', 'GPT-3.5 Turbo 16K', 0.7, 3.00, 4.00, openai_id),
               ('GPT-4', 'gpt-4', 'GPT-4', 0.7, 30.00, 60.00, openai_id),
               ('GPT-4 Turbo', 'gpt-4-turbo', 'GPT-4 Turbo', 0.7, 30.00, 60.00, openai_id);

        -- Insert Gemini models
        INSERT INTO llm_models (name, model_id, label, temperature, input_price_per_million, output_price_per_million,
                                llm_provider_id)
        VALUES ('Gemini 2.0 Flash', 'gemini-2.0-flash', 'Gemini 2.0 Flash', NULL, 0.15, 0.60, gemini_id),
               ('Gemini 2.0 Flash Lite', 'gemini-2.0-flash-lite', 'Gemini 2.0 Flash Lite', NULL, 0.15, 0.60, gemini_id),
               ('Gemini 1.5 Flash', 'gemini-1.5-flash', 'Gemini 1.5 Flash', NULL, 0.075, 0.30, gemini_id),
               ('Gemini 1.5 Pro', 'gemini-1.5-pro', 'Gemini 1.5 Pro', NULL, 0.3125, 1.25, gemini_id);
    END
$$;

-- Add comment to tables
COMMENT ON TABLE llm_providers IS 'Stores information about LLM providers such as OpenAI, Gemini, etc.';
COMMENT ON TABLE llm_models IS 'Stores information about LLM models, their configurations, and pricing';