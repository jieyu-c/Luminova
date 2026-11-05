CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT PRIMARY KEY,
    nickname VARCHAR(30) NOT NULL,
    avatar_url VARCHAR(500),
    role VARCHAR(16) NOT NULL DEFAULT 'USER',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT ck_user_account_role CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT ck_user_account_status CHECK (status IN ('ACTIVE', 'DISABLED'))
);

CREATE INDEX IF NOT EXISTS idx_user_account_status_created
    ON user_account (status, created_at DESC, id DESC);

CREATE TABLE IF NOT EXISTS user_identity (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES user_account(id),
    identity_type VARCHAR(32) NOT NULL,
    provider VARCHAR(64) NOT NULL,
    identifier VARCHAR(255) NOT NULL,
    identifier_masked VARCHAR(255) NOT NULL,
    verified_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_user_identity UNIQUE (identity_type, provider, identifier)
);

CREATE INDEX IF NOT EXISTS idx_user_identity_user_id ON user_identity (user_id);

CREATE TABLE IF NOT EXISTS password_credential (
    identity_id BIGINT PRIMARY KEY REFERENCES user_identity(id) ON DELETE CASCADE,
    password_hash VARCHAR(100) NOT NULL,
    password_changed_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS security_audit_log (
    id BIGINT PRIMARY KEY,
    event_type VARCHAR(32) NOT NULL,
    actor_user_id BIGINT,
    target_user_id BIGINT,
    identity_type VARCHAR(32),
    identifier_masked VARCHAR(255),
    result VARCHAR(16) NOT NULL,
    reason_code VARCHAR(32),
    ip INET,
    user_agent VARCHAR(300),
    request_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_security_audit_created
    ON security_audit_log (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_security_audit_target
    ON security_audit_log (target_user_id, created_at DESC);
