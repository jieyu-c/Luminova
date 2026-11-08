package cn.jieyuc.file.application.governance;

import cn.jieyuc.file.application.port.storage.StorageProviderRegistry;
import cn.jieyuc.file.domain.CloudFile;
import cn.jieyuc.file.domain.CloudFileId;
import cn.jieyuc.file.domain.CloudFileRepository;
import cn.jieyuc.file.domain.FileReferenceRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 延迟物理清理 Worker。
 *
 * <p>只有软删除超过宽限期且无活动引用的文件才允许清理。远程删除成功后再短事务标记 PURGED；
 * 数据库确认失败时可安全重试幂等删除。</p>
 */
@Service
public class PurgeDeletedFilesApplicationService {
    private final CloudFileRepository fileRepository;
    private final FileReferenceRepository referenceRepository;
    private final StorageProviderRegistry storageRegistry;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;
    private final Duration gracePeriod;

    public PurgeDeletedFilesApplicationService(CloudFileRepository fileRepository,
                                               FileReferenceRepository referenceRepository,
                                               StorageProviderRegistry storageRegistry,
                                               TransactionTemplate transactionTemplate,
                                               Clock clock,
                                               @Value("${luminova.file.purge-grace-period:P7D}") Duration gracePeriod) {
        this.fileRepository = fileRepository;
        this.referenceRepository = referenceRepository;
        this.storageRegistry = storageRegistry;
        this.transactionTemplate = transactionTemplate;
        this.clock = clock;
        this.gracePeriod = gracePeriod;
    }

    /** 清理一批符合条件的物理对象，返回成功标记数量；调度和并发策略由宿主平台决定。 */
    public int purgeBatch(int batchSize) {
        OffsetDateTime now = OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        int purged = 0;
        for (CloudFile candidate : fileRepository.findDeletedBefore(now.minus(gracePeriod), batchSize)) {
            if (referenceRepository.hasActiveReference(candidate.id())) {
                continue;
            }
            storageRegistry.require(candidate.storageLocation().providerCode()).delete(candidate.storageLocation());
            Boolean marked = transactionTemplate.execute(status -> markPurged(candidate.id(), now));
            if (Boolean.TRUE.equals(marked)) {
                purged++;
            }
        }
        return purged;
    }

    private boolean markPurged(CloudFileId fileId, OffsetDateTime now) {
        CloudFile latest = fileRepository.find(fileId).orElseThrow(() -> new IllegalStateException("待清理文件不存在"));
        if (referenceRepository.hasActiveReference(fileId)) {
            return false;
        }
        latest.purge(now);
        fileRepository.save(latest);
        return true;
    }
}
