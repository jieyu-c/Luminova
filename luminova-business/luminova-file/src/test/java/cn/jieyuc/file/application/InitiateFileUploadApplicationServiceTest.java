package cn.jieyuc.file.application;

import static org.assertj.core.api.Assertions.assertThat;

import cn.jieyuc.file.application.port.FileBusinessAuthorizationPort;
import cn.jieyuc.file.application.port.storage.ObjectStoragePort;
import cn.jieyuc.file.application.port.storage.PresignedAccess;
import cn.jieyuc.file.application.port.storage.PresignedUpload;
import cn.jieyuc.file.application.port.storage.StorageProviderRegistry;
import cn.jieyuc.file.application.port.storage.StoredObjectMetadata;
import cn.jieyuc.file.domain.BusinessOwnership;
import cn.jieyuc.file.domain.CloudFile;
import cn.jieyuc.file.domain.CloudFileId;
import cn.jieyuc.file.domain.CloudFileRepository;
import cn.jieyuc.file.domain.StorageLocation;
import cn.jieyuc.file.domain.StorageProviderCode;
import cn.jieyuc.file.domain.UploadDeclaration;
import cn.jieyuc.file.domain.UploadPurpose;
import cn.jieyuc.file.domain.UploadSession;
import cn.jieyuc.file.domain.UploadSessionId;
import cn.jieyuc.file.domain.UploadSessionRepository;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class InitiateFileUploadApplicationServiceTest {
    @Test
    void shouldReuseSessionForSameIdempotencyKey() {
        InMemoryFiles files = new InMemoryFiles();
        InMemorySessions sessions = new InMemorySessions();
        AtomicLong ids = new AtomicLong(100);
        FileBusinessAuthorizationPort authorization = new AllowAuthorization();
        InitiateFileUploadApplicationService service = new InitiateFileUploadApplicationService(files, sessions,
                ids::incrementAndGet, () -> 9L, authorization,
                new StorageProviderRegistry(List.of(new FakeStorage())),
                Clock.fixed(Instant.parse("2026-11-08T00:00:00Z"), ZoneOffset.UTC),
                "bucket", Duration.ofMinutes(30));
        InitiateFileUploadCommand command = new InitiateFileUploadCommand(UploadPurpose.WORKSPACE_COVER,
                new BusinessOwnership("USER", "PROFILE", "9"),
                new UploadDeclaration("a.png", "image/png", 10, "a".repeat(64)), "same");

        InitiatedFileUpload first = service.initiate(command);
        InitiatedFileUpload second = service.initiate(command);

        assertThat(second.sessionId()).isEqualTo(first.sessionId());
        assertThat(files.values).hasSize(1);
    }

    private static final class InMemoryFiles implements CloudFileRepository {
        private final Map<Long, CloudFile> values = new HashMap<>();

        @Override
        public Optional<CloudFile> find(CloudFileId id) {
            return Optional.ofNullable(values.get(id.value()));
        }

        @Override
        public void save(CloudFile file) {
            values.put(file.id().value(), file);
        }

        @Override
        public java.util.List<CloudFile> findDeletedBefore(OffsetDateTime deadline, int limit) {
            return new ArrayList<>();
        }
    }

    private static final class InMemorySessions implements UploadSessionRepository {
        private final Map<Long, UploadSession> values = new HashMap<>();
        private final Map<String, Long> keys = new HashMap<>();

        @Override
        public Optional<UploadSession> find(UploadSessionId id) {
            return Optional.ofNullable(values.get(id.value()));
        }

        @Override
        public Optional<UploadSession> findByUploaderAndIdempotencyKey(long uploaderId, String idempotencyKey) {
            Long id = keys.get(uploaderId + ":" + idempotencyKey);
            return id == null ? Optional.empty() : Optional.of(values.get(id));
        }

        @Override
        public void save(UploadSession session, String idempotencyKey) {
            values.put(session.id().value(), session);
            if (idempotencyKey != null) {
                keys.put(session.uploaderId() + ":" + idempotencyKey, session.id().value());
            }
        }
    }

    private static final class AllowAuthorization implements FileBusinessAuthorizationPort {
        @Override
        public void requireUploadPermission(long operatorId, BusinessOwnership ownership, UploadPurpose purpose) {
        }

        @Override
        public void requireReadPermission(long operatorId, BusinessOwnership ownership) {
        }

        @Override
        public void requireDeletePermission(long operatorId, BusinessOwnership ownership) {
        }
    }

    private static final class FakeStorage implements ObjectStoragePort {
        @Override
        public StorageProviderCode providerCode() {
            return StorageProviderCode.RUSTFS;
        }

        @Override
        public PresignedUpload issueSingleUpload(StorageLocation location, UploadDeclaration declaration,
                                                 Duration validity) {
            return new PresignedUpload(URI.create("https://storage/upload"), Map.of(),
                    OffsetDateTime.parse("2026-11-08T00:30:00Z"));
        }

        @Override
        public StoredObjectMetadata inspect(StorageLocation location) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PresignedAccess issueAccess(StorageLocation location, String downloadName, Duration validity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(StorageLocation location) {
        }
    }
}
