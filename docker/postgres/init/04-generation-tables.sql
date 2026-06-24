-- Luminova 媒体生成微服务相关表。
-- canvas_id、node_id、user_id 是外部微服务资源标识，不建立跨服务数据库外键。

CREATE TABLE IF NOT EXISTS generation_task (
    id BIGINT PRIMARY KEY,
    canvas_id BIGINT NOT NULL,
    node_id BIGINT,
    user_id BIGINT NOT NULL,
    task_type VARCHAR(32) NOT NULL,
    provider VARCHAR(64),
    model_name VARCHAR(128),
    status SMALLINT NOT NULL DEFAULT 0,
    request_params JSONB NOT NULL DEFAULT '{}'::JSONB,
    result_data JSONB NOT NULL DEFAULT '{}'::JSONB,
    error_code VARCHAR(64),
    error_message VARCHAR(1024),
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_generation_task_type
        CHECK (task_type IN ('IMAGE', 'VIDEO', 'AUDIO', 'TEXT')),
    CONSTRAINT chk_generation_task_status
        CHECK (status IN (0, 1, 2, 3, 4)),
    CONSTRAINT chk_generation_task_time
        CHECK (
            finished_at IS NULL
            OR started_at IS NULL
            OR finished_at >= started_at
        )
);

-- 兼容早期由 canvas 模块创建 generation_task 的开发库。
ALTER TABLE generation_task
    DROP CONSTRAINT IF EXISTS fk_generation_task_canvas;

ALTER TABLE generation_task
    DROP CONSTRAINT IF EXISTS fk_generation_task_node;

ALTER TABLE generation_task
    DROP CONSTRAINT IF EXISTS fk_generation_task_user;

CREATE INDEX IF NOT EXISTS idx_generation_task_canvas_created_at
    ON generation_task (canvas_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_generation_task_node_created_at
    ON generation_task (node_id, created_at DESC)
    WHERE node_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_generation_task_user_created_at
    ON generation_task (user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_generation_task_status_created_at
    ON generation_task (status, created_at);
