-- Luminova 创作项目与节点画布相关表。
-- 主键由应用侧 MyBatis-Plus ASSIGN_ID 生成。

CREATE TABLE IF NOT EXISTS creative_project (
    id BIGINT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    project_type VARCHAR(32) NOT NULL,
    description TEXT,
    cover_url VARCHAR(512),
    aspect_ratio VARCHAR(16),
    target_duration_ms INTEGER,
    status SMALLINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_creative_project_owner
        FOREIGN KEY (owner_id) REFERENCES sys_user (id),
    CONSTRAINT chk_creative_project_type
        CHECK (project_type IN (
            'GENERAL',
            'SHORT_DRAMA',
            'COMIC',
            'AD',
            'TALKING_HEAD'
        )),
    CONSTRAINT chk_creative_project_status
        CHECK (status IN (0, 1, 2, 3)),
    CONSTRAINT chk_creative_project_duration
        CHECK (target_duration_ms IS NULL OR target_duration_ms >= 0)
);

CREATE INDEX IF NOT EXISTS idx_creative_project_owner_updated_at
    ON creative_project (owner_id, updated_at DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_creative_project_status
    ON creative_project (status)
    WHERE deleted_at IS NULL;


CREATE TABLE IF NOT EXISTS project_episode (
    id BIGINT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    season_no INTEGER NOT NULL DEFAULT 1,
    episode_no INTEGER NOT NULL,
    title VARCHAR(128),
    duration_ms INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_project_episode_project
        FOREIGN KEY (project_id) REFERENCES creative_project (id),
    CONSTRAINT chk_project_episode_no
        CHECK (season_no > 0 AND episode_no > 0),
    CONSTRAINT chk_project_episode_duration
        CHECK (duration_ms IS NULL OR duration_ms >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_project_episode_alive
    ON project_episode (project_id, season_no, episode_no)
    WHERE deleted_at IS NULL;


CREATE TABLE IF NOT EXISTS canvas (
    id BIGINT PRIMARY KEY,
    project_id BIGINT,
    episode_id BIGINT,
    owner_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    canvas_type VARCHAR(32) NOT NULL DEFAULT 'MAIN',
    status SMALLINT NOT NULL DEFAULT 0,
    cover_url VARCHAR(512),
    viewport_x NUMERIC(14, 4) NOT NULL DEFAULT 0,
    viewport_y NUMERIC(14, 4) NOT NULL DEFAULT 0,
    viewport_zoom NUMERIC(6, 4) NOT NULL DEFAULT 1,
    version BIGINT NOT NULL DEFAULT 0,
    last_edited_by BIGINT,
    last_edited_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_canvas_project
        FOREIGN KEY (project_id) REFERENCES creative_project (id),
    CONSTRAINT fk_canvas_episode
        FOREIGN KEY (episode_id) REFERENCES project_episode (id),
    CONSTRAINT fk_canvas_owner
        FOREIGN KEY (owner_id) REFERENCES sys_user (id),
    CONSTRAINT fk_canvas_last_editor
        FOREIGN KEY (last_edited_by) REFERENCES sys_user (id),
    CONSTRAINT chk_canvas_type
        CHECK (canvas_type IN (
            'MAIN',
            'STORYBOARD',
            'CHARACTER_TEST',
            'STYLE_TEST',
            'ATMOSPHERE_TEST'
        )),
    CONSTRAINT chk_canvas_status
        CHECK (status IN (0, 1, 2, 3, 4)),
    CONSTRAINT chk_canvas_zoom
        CHECK (viewport_zoom BETWEEN 0.1 AND 5),
    CONSTRAINT chk_canvas_version
        CHECK (version >= 0)
);

CREATE INDEX IF NOT EXISTS idx_canvas_owner_updated_at
    ON canvas (owner_id, updated_at DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_canvas_project_updated_at
    ON canvas (project_id, updated_at DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_canvas_episode
    ON canvas (episode_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_canvas_status
    ON canvas (status)
    WHERE deleted_at IS NULL;


CREATE TABLE IF NOT EXISTS canvas_member (
    id BIGINT PRIMARY KEY,
    canvas_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    member_role VARCHAR(16) NOT NULL DEFAULT 'VIEWER',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_canvas_member_canvas
        FOREIGN KEY (canvas_id) REFERENCES canvas (id) ON DELETE CASCADE,
    CONSTRAINT fk_canvas_member_user
        FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT chk_canvas_member_role
        CHECK (member_role IN ('OWNER', 'EDITOR', 'VIEWER'))
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_canvas_member_alive
    ON canvas_member (canvas_id, user_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_canvas_member_user
    ON canvas_member (user_id, updated_at DESC)
    WHERE deleted_at IS NULL;


CREATE TABLE IF NOT EXISTS canvas_node (
    id BIGINT PRIMARY KEY,
    canvas_id BIGINT NOT NULL,
    node_key VARCHAR(64) NOT NULL,
    node_type VARCHAR(32) NOT NULL,
    title VARCHAR(128),
    status SMALLINT NOT NULL DEFAULT 0,
    position_x NUMERIC(14, 4) NOT NULL,
    position_y NUMERIC(14, 4) NOT NULL,
    width NUMERIC(12, 4),
    height NUMERIC(12, 4),
    z_index INTEGER NOT NULL DEFAULT 0,
    content JSONB NOT NULL DEFAULT '{}'::JSONB,
    presentation JSONB NOT NULL DEFAULT '{}'::JSONB,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_canvas_node_canvas
        FOREIGN KEY (canvas_id) REFERENCES canvas (id) ON DELETE CASCADE,
    CONSTRAINT chk_canvas_node_type
        CHECK (node_type IN (
            'SHOT',
            'CHARACTER',
            'SCENE',
            'DIALOGUE',
            'STYLE',
            'SHOT_CONTEXT',
            'ASSET',
            'MUSIC',
            'EFFECT',
            'TEXT'
        )),
    CONSTRAINT chk_canvas_node_status
        CHECK (status IN (0, 1, 2, 3, 4)),
    CONSTRAINT chk_canvas_node_size
        CHECK (
            (width IS NULL OR width >= 0)
            AND (height IS NULL OR height >= 0)
        ),
    CONSTRAINT chk_canvas_node_version
        CHECK (version >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_canvas_node_key_alive
    ON canvas_node (canvas_id, node_key)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_canvas_node_canvas_type
    ON canvas_node (canvas_id, node_type)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_canvas_node_content_gin
    ON canvas_node USING GIN (content);


CREATE TABLE IF NOT EXISTS canvas_edge (
    id BIGINT PRIMARY KEY,
    canvas_id BIGINT NOT NULL,
    edge_key VARCHAR(64) NOT NULL,
    source_node_id BIGINT NOT NULL,
    target_node_id BIGINT NOT NULL,
    source_handle VARCHAR(64),
    target_handle VARCHAR(64),
    edge_type VARCHAR(32) NOT NULL DEFAULT 'DEFAULT',
    label VARCHAR(128),
    config JSONB NOT NULL DEFAULT '{}'::JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_canvas_edge_canvas
        FOREIGN KEY (canvas_id) REFERENCES canvas (id) ON DELETE CASCADE,
    CONSTRAINT fk_canvas_edge_source
        FOREIGN KEY (source_node_id) REFERENCES canvas_node (id) ON DELETE CASCADE,
    CONSTRAINT fk_canvas_edge_target
        FOREIGN KEY (target_node_id) REFERENCES canvas_node (id) ON DELETE CASCADE,
    CONSTRAINT chk_canvas_edge_type
        CHECK (edge_type IN ('DEFAULT', 'REFERENCE', 'SEQUENCE', 'CONSTRAINT')),
    CONSTRAINT chk_canvas_edge_self
        CHECK (source_node_id <> target_node_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_canvas_edge_key_alive
    ON canvas_edge (canvas_id, edge_key)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_canvas_edge_source
    ON canvas_edge (source_node_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_canvas_edge_target
    ON canvas_edge (target_node_id)
    WHERE deleted_at IS NULL;


CREATE TABLE IF NOT EXISTS canvas_revision (
    id BIGINT PRIMARY KEY,
    canvas_id BIGINT NOT NULL,
    revision_no BIGINT NOT NULL,
    canvas_version BIGINT NOT NULL,
    snapshot JSONB NOT NULL,
    change_summary VARCHAR(255),
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_canvas_revision_canvas
        FOREIGN KEY (canvas_id) REFERENCES canvas (id) ON DELETE CASCADE,
    CONSTRAINT fk_canvas_revision_creator
        FOREIGN KEY (created_by) REFERENCES sys_user (id),
    CONSTRAINT chk_canvas_revision_no
        CHECK (revision_no > 0),
    CONSTRAINT chk_canvas_revision_version
        CHECK (canvas_version >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_canvas_revision_no
    ON canvas_revision (canvas_id, revision_no);

CREATE INDEX IF NOT EXISTS idx_canvas_revision_created_at
    ON canvas_revision (canvas_id, created_at DESC);
