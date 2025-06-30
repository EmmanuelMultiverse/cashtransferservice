DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS users;


CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(255) UNIQUE NOT NULL,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    account_type VARCHAR(50) NOT NULL,
    user_id BIGINT UNIQUE NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    sending_account_id VARCHAR(255) NOT NULL,
    receiving_account_id VARCHAR(255) NOT NULL,
    transfer_amount DECIMAL(19, 2) NOT NULL,
    local_date_time TIMESTAMP WITHOUT TIME ZONE NOT NULL, 
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_user_transactions FOREIGN KEY (user_id) REFERENCES users (id)
);