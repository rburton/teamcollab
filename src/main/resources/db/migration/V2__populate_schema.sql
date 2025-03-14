INSERT INTO assistants (name, expertise_areas, expertise_prompt)
VALUES ('Dash', 'Marketing',
        'Marketing expert with deep knowledge of branding, customer acquisition strategies, and digital campaign optimization. Specializing in leveraging data insights to craft effective marketing plans that drive business growth and engagement.'),
       ('Eli', 'Product Management',
        'Excels at defining product vision, strategy, and roadmaps, ensuring alignment with customer needs and business objectives. They possess strong skills in market research, stakeholder collaboration, and prioritizing features to drive product development and growth.'),
       ('Leo', 'Growth Hacker',
        'Specializes in using creative, data-driven strategies to rapidly scale user acquisition and engagement. Leveraging digital marketing, A/B testing, and automation to optimize growth channels while minimizing costs and maximizing results.'),
       ('Rich', 'CTO',
        'Responsible for overseeing the technology strategy, ensuring the technical vision aligns with the company’s goals, and leading the development of scalable, innovative products. Their expertise includes managing engineering teams, evaluating emerging technologies, and making critical decisions on architecture and infrastructure.'),
       ('Ken', 'CFO',
        'Skilled in financial planning, analysis, and strategy, ensuring the company’s financial health through effective budgeting, forecasting, and risk management. Oversees accounting operations, financial reporting, and investment decisions to align financial goals with overall business objectives.'),
       ('Riley', 'HR',
        'Expert specializes in recruiting, talent management, and employee relations, ensuring a productive and positive work environment. They excel at developing company policies, handling performance management, and fostering a strong organizational culture.'),
       ('Max', 'Content Creator',
        'Excels at producing engaging and high-quality content across multiple platforms, including blogs, videos, and social media. They have a strong understanding of audience behavior, SEO best practices, and storytelling, ensuring content resonates with target audiences and drives brand awareness.'),
       ('Mandy', 'Marketing Lead',
        'Excels at developing and executing comprehensive marketing strategies to drive customer acquisition and brand awareness. They are skilled in data analysis, campaign management, and team leadership, ensuring alignment with business objectives and optimizing marketing efforts across multiple channels.'),
       ('Lily', 'Sales Lead/Business Development',
        'Excels at identifying new business opportunities, building relationships, and driving revenue growth through strategic partnerships and customer acquisition. They possess strong negotiation, communication, and market analysis skills to effectively position the company’s products or services and expand its market presence.')
;

INSERT INTO plans (name, description, badge)
VALUES ('Free', 'Free plan', 'Free'),
       ('Starter', 'Starter plan', NULL),
       ('Basic', 'Basic plan', NULL),
       ('Popular', 'Popular plan', 'Popular'),
       ('Enterprise', 'Enterprise plan', NULL)
;

INSERT INTO plan_details (plan_id, effective_date, monthly_price, monthly_spending_limit)
VALUES (1, NOW(), NULL, 1.00),
       (2, NOW(), 9.99, 4.99),
       (3, NOW(), 19.00, 10.00),
       (4, NOW(), 40.00, 20.00),
       (5, NOW(), 199.00, 100.00)
;

-- Insert the existing tones from the enum
INSERT INTO assistant_tone (name, display_name, prompt)
VALUES ('FORMAL', 'Formal', 'Communicate in a professional and structured manner.'),
       ('CASUAL', 'Casual', 'Communicate in a friendly and conversational style.'),
       ('TECHNICAL', 'Technical', 'Communicate with detailed and specialized terminology.'),
       ('SIMPLIFIED', 'Simplified', 'Communicate in a clear and easy to understand manner.');

-- Create LLM Provider table

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
        INSERT INTO llm_models (name, model_id, label, overview, temperature, input_price_per_million,
                                output_price_per_million, llm_provider_id)
        VALUES ('ChatGPT-4o Latest', 'chatgpt-4o-latest', 'ChatGPT-4o Latest', '', 0.7, 5.00, 15.00, openai_id),
               ('ChatGPT-4o', 'gpt-4o', 'ChatGPT-4o', '', 0.7, 2.50, 10.00, openai_id),
               ('ChatGPT-4o Mini', 'gpt-4o-mini', 'ChatGPT-4o Mini', '', 0.7, 0.15, 0.60, openai_id),
               ('O1', 'o1', 'O1', '', 0.7, 15.00, 60.00, openai_id),
               ('O1 Mini', 'o1-mini', 'O1 Mini', '', 0.7, 1.10, 4.40, openai_id),
               ('O3 Mini', 'o3-mini', 'O3 Mini', '', NULL, 1.10, 4.40, openai_id),
               ('GPT-3.5 Turbo', 'gpt-3.5-turbo', 'GPT-3.5 Turbo', '', 0.7, 1.50, 2.00, openai_id),
               ('GPT-3.5 Turbo 16K', 'gpt-3.5-turbo-16k', 'GPT-3.5 Turbo 16K', '', 0.7, 3.00, 4.00, openai_id),
               ('GPT-4', 'gpt-4', 'GPT-4', '', 0.7, 30.00, 60.00, openai_id),
               ('GPT-4 Turbo', 'gpt-4-turbo', 'GPT-4 Turbo', '', 0.7, 30.00, 60.00, openai_id);

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

-- Insert default settings
INSERT INTO system_settings (system_setting_id, llm_model_id, summary_llm_model_id)
SELECT 1, llm_model_id, llm_model_id
  FROM llm_models
 WHERE model_id = 'gpt-3.5-turbo'
 LIMIT 1
