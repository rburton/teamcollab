-- Create auth_providers table for OAuth2 authentication
CREATE TABLE auth_providers
(
    auth_provider_id    BIGSERIAL PRIMARY KEY,
    provider_name       VARCHAR(50)  NOT NULL,
    provider_user_id    VARCHAR(255),
    provider_email      VARCHAR(255),
    provider_picture_url VARCHAR(1000),
    user_id             BIGINT,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT uk_provider_user UNIQUE (provider_name, provider_user_id)
);

-- Create indexes for faster lookups
CREATE INDEX idx_auth_providers_user_id ON auth_providers (user_id);
CREATE INDEX idx_auth_providers_provider_name_user_id ON auth_providers (provider_name, provider_user_id);
CREATE INDEX idx_auth_providers_provider_name_email ON auth_providers (provider_name, provider_email);