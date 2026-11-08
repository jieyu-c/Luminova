package cn.jieyuc.file.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class CloudFileTest {
    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-11-08T00:00:00Z");

    @Test
    void shouldFollowFileLifecycle() {
        CloudFile file = createFile();

        file.markAvailable(NOW.plusMinutes(1));
        file.delete(NOW.plusDays(1));
        file.purge(NOW.plusDays(8));

        assertThat(file.status()).isEqualTo(CloudFileStatus.PURGED);
        assertThat(file.purgedAt()).isEqualTo(NOW.plusDays(8));
    }

    @Test
    void shouldRejectDeletingPendingFile() {
        CloudFile file = createFile();

        assertThatThrownBy(() -> file.delete(NOW)).isInstanceOf(IllegalStateException.class);
    }

    private CloudFile createFile() {
        return CloudFile.create(new CloudFileId(1), 9,
                new BusinessOwnership("USER", "PROFILE", "9"), UploadPurpose.WORKSPACE_COVER,
                new UploadDeclaration("cover.png", "image/png", 10, "a".repeat(64)),
                new StorageLocation(StorageProviderCode.RUSTFS, "bucket", "key"), NOW);
    }
}
