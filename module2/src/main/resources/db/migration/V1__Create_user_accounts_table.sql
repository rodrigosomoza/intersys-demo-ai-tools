-- Create user_accounts table for Module 2 (User Service)
CREATE TABLE user_accounts (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL UNIQUE,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    currency TEXT NOT NULL DEFAULT 'USD',
    account_type TEXT NOT NULL DEFAULT 'MAIN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for performance
CREATE INDEX idx_user_accounts_user_id ON user_accounts(user_id);
CREATE INDEX idx_user_accounts_account_type ON user_accounts(account_type);

-- Insert some test data
INSERT INTO user_accounts (id, user_id, balance, currency, account_type) VALUES
('acc_001', 'user_A', 1000.00, 'USD', 'MAIN'),
('acc_002', 'user_B', 500.00, 'USD', 'MAIN'),
('acc_003', 'user_C', 2000.00, 'USD', 'MAIN');
