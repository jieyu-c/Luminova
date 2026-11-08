package cn.jieyuc.file.application.reference;

import cn.jieyuc.file.application.port.FileIdentifierGenerator;
import cn.jieyuc.file.domain.BusinessOwnership;
import cn.jieyuc.file.domain.CloudFile;
import cn.jieyuc.file.domain.CloudFileId;
import cn.jieyuc.file.domain.CloudFileRepository;
import cn.jieyuc.file.domain.CloudFileStatus;
import cn.jieyuc.file.domain.FileReference;
import cn.jieyuc.file.domain.FileReferenceRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 将可信业务事件转换为文件引用账本变更。 */
@Service
public class ChangeFileReferenceApplicationService {
    private final CloudFileRepository fileRepository;
    private final FileReferenceRepository referenceRepository;
    private final FileIdentifierGenerator identifierGenerator;
    private final Clock clock;

    public ChangeFileReferenceApplicationService(CloudFileRepository fileRepository,
                                                 FileReferenceRepository referenceRepository,
                                                 FileIdentifierGenerator identifierGenerator,
                                                 Clock clock) {
        this.fileRepository = fileRepository;
        this.referenceRepository = referenceRepository;
        this.identifierGenerator = identifierGenerator;
        this.clock = clock;
    }

    /** 在消费本地事务中幂等绑定或释放引用。 */
    @Transactional
    public void change(FileReferenceChangedEvent event) {
        BusinessOwnership ownership = new BusinessOwnership(event.businessContext(), event.businessObjectType(),
                event.businessObjectId());
        CloudFile file = fileRepository.find(new CloudFileId(event.fileId()))
                .orElseThrow(() -> new IllegalArgumentException("引用的云文件不存在"));
        if (!file.ownership().equals(ownership)) {
            throw new IllegalArgumentException("文件业务归属与引用事件不一致");
        }
        if (file.status() != CloudFileStatus.AVAILABLE && event.action() == FileReferenceChangedEvent.Action.ADDED) {
            throw new IllegalStateException("只有可用文件可以建立引用");
        }
        FileReference existing = referenceRepository.find(ownership, event.referenceType(), event.referenceKey())
                .orElse(null);
        if (event.action() == FileReferenceChangedEvent.Action.ADDED) {
            if (existing == null) {
                referenceRepository.save(FileReference.create(identifierGenerator.nextId(), file.id(), ownership,
                        event.referenceType(), event.referenceKey(), now()));
            } else if (!existing.fileId().equals(file.id()) || existing.releasedAt() != null) {
                throw new IllegalStateException("引用键已绑定其他文件或已被释放");
            }
        } else if (existing != null) {
            existing.release(now());
            referenceRepository.save(existing);
        }
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }
}
