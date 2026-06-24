CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY,
    phone VARCHAR(32),
    email VARCHAR(255),
    username VARCHAR(64),
    nickname VARCHAR(64),
    avatar_url VARCHAR(512),
    password_hash VARCHAR(255),
    status SMALLINT NOT NULL DEFAULT 1,
    last_login_at TIMESTAMPTZ,
    last_login_ip VARCHAR(45),
    password_updated_at TIMESTAMPTZ,
    phone_verified_at TIMESTAMPTZ,
    email_verified_at TIMESTAMPTZ,
    failed_login_count INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT chk_sys_user_status CHECK (status IN (0, 1, 2, 3)),
    CONSTRAINT chk_sys_user_failed_login_count CHECK (failed_login_count >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_phone_alive
    ON sys_user (phone)
    WHERE phone IS NOT NULL AND deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_email_alive
    ON sys_user (email)
    WHERE email IS NOT NULL AND deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_username_alive
    ON sys_user (username)
    WHERE username IS NOT NULL AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_sys_user_status
    ON sys_user (status)
    WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS sys_user_action_log (
    id BIGINT PRIMARY KEY,
    user_id BIGINT,
    action_type VARCHAR(32) NOT NULL,
    phone VARCHAR(32),
    action_ip VARCHAR(45),
    user_agent TEXT,
    success BOOLEAN NOT NULL DEFAULT false,
    failure_reason VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sys_user_action_log_user
        FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_sys_user_action_log_user_id_created_at
    ON sys_user_action_log (user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_sys_user_action_log_phone_created_at
    ON sys_user_action_log (phone, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_sys_user_action_log_action_type_created_at
    ON sys_user_action_log (action_type, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_sys_user_action_log_created_at
    ON sys_user_action_log (created_at DESC);
