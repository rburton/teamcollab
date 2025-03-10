-- Add monthly_spending_limit column to companies table
ALTER TABLE companies
ADD COLUMN monthly_spending_limit DECIMAL(10, 2);

-- Set default value for existing companies (null means no limit)
UPDATE companies
SET monthly_spending_limit = NULL;

-- Add comment to explain the column
COMMENT ON COLUMN companies.monthly_spending_limit IS 'Monthly spending limit for LLM API calls. NULL means no limit.';