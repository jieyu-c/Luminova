package cn.jieyuc.file.application;

import cn.jieyuc.file.application.port.AuthenticatedFileOperator;
import cn.jieyuc.file.application.port.storage.ObjectStoragePort;
import cn.jieyuc.file.application.port.storage.StorageProviderRegistry;
import cn.jieyuc.file.application.port.storage.StoredObjectMetadata;
import cn.jieyuc.file.domain.CloudFile;
import cn.jieyuc.file.domain.CloudFileId;
import cn.jieyuc.file.domain.CloudFileRepository;
import cn.jieyuc.file.domain.UploadSession;
import cn.jieyuc.file.domain.UploadSessionId;
import cn.jieyuc.file.domain.UploadSessionRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 完成普通上传的用例服务。
 *
 * <p>先在短事务中推进到 COMPLETING，再执行 HeadObject，最后在新事务中确认文件可用，
 * 避免将不可控的远程调用包裹在数据库事务中。</p>
 */
@Service
public class CompleteFileUploadApplicationService {
    private final UploadSessionRepository sessionRepository;
    private final CloudFileRepository fileRepository;
    private final AuthenticatedFileOperator operator;
    private final StorageProviderRegistry storageRegistry;
    private final CloudFileViewConverter viewConverter;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    public CompleteFileUploadApplicationService(UploadSessionRepository sessionRepository,
                                                CloudFileRepository fileRepository,
                                                AuthenticatedFileOperator operator,
                                                StorageProviderRegistry storageRegistry,
                                                CloudFileViewConverter viewConverter,
                                                TransactionTemplate transactionTemplate,
                                                Clock clock) {
        this.sessionRepository = sessionRepository;
        this.fileRepository = fileRepository;
        this.operator = operator;
        this.storageRegistry = storageRegistry;
        this.viewConverter = viewConverter;
        this.transactionTemplate = transactionTemplate;
        this.clock = clock;
    }

    /** 完成会话并返回可用文件；对象大小、媒体类型和声明哈希不一致时拒绝完成。 */
    public CloudFileView complete(long sessionId) {
        long operatorId = operator.currentUserId();
        CompletionState state = transactionTemplate.execute(status -> start(sessionId, operatorId));
        if (state == null) {
            throw new IllegalStateException("无法推进上传会话");
        }
        ObjectStoragePort storage = storageRegistry.require(state.file().storageLocation().providerCode());
        StoredObjectMetadata metadata = storage.inspect(state.file().storageLocation());
        validateObject(state.file(), metadata);
        return transactionTemplate.execute(status -> finish(state.session().id(), state.file().id()));
    }

    private CompletionState start(long sessionId, long operatorId) {
        UploadSession session = sessionRepository.find(new UploadSessionId(sessionId))
                .orElseThrow(() -> new IllegalArgumentException("上传会话不存在"));
        CloudFile file = fileRepository.find(session.fileId())
                .orElseThrow(() -> new IllegalStateException("上传会话缺少文件记录"));
        if (session.status() == cn.jieyuc.file.domain.UploadSessionStatus.COMPLETED) {
            return new CompletionState(session, file);
        }
        session.startCompleting(operatorId, now());
        sessionRepository.save(session, null);
        return new CompletionState(session, file);
    }

    /** 比较可信 Head 元数据和不可修改的上传声明。 */
    private void validateObject(CloudFile file, StoredObjectMetadata metadata) {
        if (metadata.size() != file.declaration().size()) {
            throw new IllegalStateException("上传对象大小与声明不一致");
        }
        if (metadata.contentType() != null && !metadata.contentType().equals(file.declaration().mediaType())) {
            throw new IllegalStateException("上传对象媒体类型与声明不一致");
        }
        if (metadata.declaredSha256() != null
                && !metadata.declaredSha256().equalsIgnoreCase(file.declaration().sha256())) {
            throw new IllegalStateException("上传对象SHA-256与声明不一致");
        }
    }

    private CloudFileView finish(UploadSessionId sessionId, CloudFileId fileId) {
        UploadSession session = sessionRepository.find(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("上传会话不存在"));
        CloudFile file = fileRepository.find(fileId)
                .orElseThrow(() -> new IllegalArgumentException("云文件不存在"));
        OffsetDateTime now = now();
        session.complete(now);
        file.markAvailable(now);
        sessionRepository.save(session, null);
        fileRepository.save(file);
        return viewConverter.toView(file);
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    private record CompletionState(UploadSession session, CloudFile file) {
    }
}
