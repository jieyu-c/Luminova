package cn.jieyuc.file.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class UploadSessionTest {
    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-11-08T00:00:00Z");

    @Test
    void shouldCompleteIdempotently() {
        UploadSession session = UploadSession.create(new UploadSessionId(1), new CloudFileId(2), 9,
                NOW, NOW.plusMinutes(30));

        session.startCompleting(9, NOW.plusMinutes(1));
        session.startCompleting(9, NOW.plusMinutes(1));
        session.complete(NOW.plusMinutes(2));
        session.complete(NOW.plusMinutes(2));

        assertThat(session.status()).isEqualTo(UploadSessionStatus.COMPLETED);
    }

    @Test
    void shouldRejectExpiredSession() {
        UploadSession session = UploadSession.create(new UploadSessionId(1), new CloudFileId(2), 9,
                NOW, NOW.plusMinutes(30));

        assertThatThrownBy(() -> session.startCompleting(9, NOW.plusHours(1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(session.status()).isEqualTo(UploadSessionStatus.EXPIRED);
    }
}
