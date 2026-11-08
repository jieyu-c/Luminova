CREATE TABLE cloud_file (
    id BIGINT PRIMARY KEY,
    uploader_id BIGINT NOT NULL,
    business_context VARCHAR(64) NOT NULL,
    business_object_type VARCHAR(64) NOT NULL,
    business_object_id VARCHAR(128) NOT NULL,
    upload_purpose VARCHAR(64) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    media_type VARCHAR(128) NOT NULL,
    file_size BIGINT NOT NULL,
    sha256_digest CHAR(64) NOT NULL,
    storage_provider VARCHAR(32) NOT NULL,
    storage_bucket VARCHAR(128) NOT NULL,
    storage_object_key VARCHAR(512) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    available_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    purged_at TIMESTAMPTZ,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uk_cloud_file_storage UNIQUE (storage_provider, storage_bucket, storage_object_key),
    CONSTRAINT ck_cloud_file_size CHECK (file_size > 0)
);

CREATE INDEX idx_cloud_file_ownership
    ON cloud_file (business_context, business_object_type, business_object_id, status);

CREATE TABLE file_upload_session (
    id BIGINT PRIMARY KEY,
    file_id BIGINT NOT NULL REFERENCES cloud_file(id),
    uploader_id BIGINT NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uk_file_upload_idempotency UNIQUE (uploader_id, idempotency_key)
);

CREATE INDEX idx_file_upload_expiry ON file_upload_session (status, expires_at);

CREATE TABLE file_reference (
    id BIGINT PRIMARY KEY,
    file_id BIGINT NOT NULL REFERENCES cloud_file(id),
    business_context VARCHAR(64) NOT NULL,
    business_object_type VARCHAR(64) NOT NULL,
    business_object_id VARCHAR(128) NOT NULL,
    reference_type VARCHAR(64) NOT NULL,
    reference_key VARCHAR(128) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    released_at TIMESTAMPTZ,
    CONSTRAINT uk_file_reference_business_key UNIQUE (
        business_context, business_object_type, business_object_id, reference_type, reference_key
    )
);

CREATE INDEX idx_file_reference_active ON file_reference (file_id) WHERE released_at IS NULL;
