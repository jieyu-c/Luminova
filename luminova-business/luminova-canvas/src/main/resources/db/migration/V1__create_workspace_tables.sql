CREATE TABLE creative_entry (
    id BIGINT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    parent_entry_id BIGINT NULL,
    entry_type VARCHAR(16) NOT NULL CHECK (entry_type IN ('FOLDER', 'CANVAS')),
    name VARCHAR(100) NOT NULL,
    sort_order BIGINT NOT NULL,
    custom_cover_id VARCHAR(255) NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_creative_entry_id_owner UNIQUE (id, owner_id),
    CONSTRAINT fk_creative_entry_parent_owner
        FOREIGN KEY (parent_entry_id, owner_id) REFERENCES creative_entry (id, owner_id)
);

CREATE INDEX idx_creative_entry_active_directory
    ON creative_entry (owner_id, parent_entry_id, sort_order, id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_creative_entry_active_name
    ON creative_entry (owner_id, name)
    WHERE deleted_at IS NULL;

CREATE TABLE creative_folder (
    entry_id BIGINT PRIMARY KEY REFERENCES creative_entry (id)
);

CREATE TABLE canvas (
    entry_id BIGINT PRIMARY KEY REFERENCES creative_entry (id),
    automatic_preview_id VARCHAR(255) NULL,
    canvas_status VARCHAR(32) NULL
);

CREATE TABLE recently_opened_canvas (
    user_id BIGINT NOT NULL,
    canvas_entry_id BIGINT NOT NULL REFERENCES canvas (entry_id),
    last_opened_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (user_id, canvas_entry_id)
);

CREATE INDEX idx_recently_opened_canvas_user_time
    ON recently_opened_canvas (user_id, last_opened_at DESC);
