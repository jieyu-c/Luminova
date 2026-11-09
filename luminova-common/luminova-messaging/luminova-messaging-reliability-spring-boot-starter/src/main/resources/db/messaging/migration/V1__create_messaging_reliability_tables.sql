CREATE TABLE IF NOT EXISTS mq_outbox_event (
    id BIGINT PRIMARY KEY,
    event_id VARCHAR(64) NOT NULL UNIQUE,
    topic_code VARCHAR(128) NOT NULL,
    message_mode VARCHAR(32) NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    schema_version INTEGER NOT NULL,
    ordering_key VARCHAR(128),
    deliver_at TIMESTAMP WITH TIME ZONE,
    envelope_json TEXT NOT NULL,
    properties_json TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL,
    lease_owner VARCHAR(255),
    lease_until TIMESTAMP WITH TIME ZONE,
    last_error_code VARCHAR(128),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_mq_outbox_publishable
    ON mq_outbox_event(status, next_attempt_at, lease_until);

CREATE TABLE IF NOT EXISTS mq_consumed_event (
    consumer_group VARCHAR(128) NOT NULL,
    event_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    payload_digest VARCHAR(128),
    consumed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (consumer_group, event_id)
);
