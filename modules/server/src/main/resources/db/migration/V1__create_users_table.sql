CREATE SCHEMA IF NOT EXISTS demo_chat;

CREATE TABLE demo_chat.users (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    phone      VARCHAR(20),
    login      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL
);
