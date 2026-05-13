CREATE TABLE token (
    id SERIAL PRIMARY KEY,
    token VARCHAR(512) UNIQUE NOT NULL,
    token_type VARCHAR(50) DEFAULT 'BEARER',
    revoked BOOLEAN DEFAULT FALSE,
    expired BOOLEAN DEFAULT FALSE,
    user_id UUID NOT NULL REFERENCES users(id)
);

CREATE INDEX idx_token_user_id ON token(user_id);
CREATE INDEX idx_token_string ON token(token);
