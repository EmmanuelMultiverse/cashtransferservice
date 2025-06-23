-- 1. Create the "users" table first, as "accounts" will reference it
CREATE TABLE IF NOT EXISTS "users" (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

-- 2. Create the "accounts" table, referencing the "users" table
CREATE TABLE IF NOT EXISTS "accounts" (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(255) UNIQUE NOT NULL,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    account_type VARCHAR(50) NOT NULL,
    user_id BIGINT UNIQUE NOT NULL, -- This is the foreign key with a UNIQUE constraint
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES "users" (id) -- Defines the foreign key relationship
);