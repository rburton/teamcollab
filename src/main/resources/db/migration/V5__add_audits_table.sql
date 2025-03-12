-- Create the audits table for tracking user actions
CREATE TABLE audits (
    id BIGSERIAL PRIMARY KEY,
    action_type VARCHAR(50) NOT NULL,
    user_id BIGINT,
    username VARCHAR(255) NOT NULL,
    ip_address VARCHAR(50),
    details VARCHAR(1000),
    timestamp TIMESTAMP NOT NULL,
    entity_type VARCHAR(100),
    entity_id BIGINT,
    company_id BIGINT,

    -- Foreign key constraints
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_company FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE SET NULL
);

-- Create indexes for better query performance
CREATE INDEX idx_audit_user_id ON audits(user_id);
CREATE INDEX idx_audit_company_id ON audits(company_id);
CREATE INDEX idx_audit_action_type ON audits(action_type);
CREATE INDEX idx_audit_timestamp ON audits(timestamp);
CREATE INDEX idx_audit_entity ON audits(entity_type, entity_id);

-- Add a comment to the table
COMMENT ON TABLE audits IS 'Stores audit events for tracking user actions in the system';
