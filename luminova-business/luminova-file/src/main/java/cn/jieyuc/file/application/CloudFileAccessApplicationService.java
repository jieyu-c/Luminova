package cn.jieyuc.file.application;

import cn.jieyuc.file.application.port.AuthenticatedFileOperator;
import cn.jieyuc.file.application.port.FileBusinessAuthorizationPort;
import cn.jieyuc.file.application.port.storage.PresignedAccess;
import cn.jieyuc.file.application.port.storage.StorageProviderRegistry;
import cn.jieyuc.file.domain.CloudFile;
import cn.jieyuc.file.domain.CloudFileId;
import cn.jieyuc.file.domain.CloudFileRepository;
import cn.jieyuc.file.domain.CloudFileStatus;
import java.time.Duration;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 查询文件元数据、签发访问地址和执行软删除的用例服务。 */
@Service
public class CloudFileAccessApplicationService {
    private final CloudFileRepository repository;
    private final AuthenticatedFileOperator operator;
    private final FileBusinessAuthorizationPort authorization;
    private final StorageProviderRegistry storageRegistry;
    private final CloudFileViewConverter viewConverter;
    private final Duration accessValidity;
    private final Clock clock;

    public CloudFileAccessApplicationService(CloudFileRepository repository,
                                             AuthenticatedFileOperator operator,
                                             FileBusinessAuthorizationPort authorization,
                                             StorageProviderRegistry storageRegistry,
                                             CloudFileViewConverter viewConverter,
                                             Clock clock,
                                             @Value("${luminova.file.access-validity:PT10M}") Duration accessValidity) {
        this.repository = repository;
        this.operator = operator;
        this.authorization = authorization;
        this.storageRegistry = storageRegistry;
        this.viewConverter = viewConverter;
        this.clock = clock;
        this.accessValidity = accessValidity;
    }

    /** 查询经过业务授权的文件公开元数据。 */
    @Transactional(readOnly = true)
    public CloudFileView get(long fileId) {
        CloudFile file = requireFile(fileId);
        authorization.requireReadPermission(operator.currentUserId(), file.ownership());
        return viewConverter.toView(file);
    }

    /** 为可用文件签发短时访问地址，不返回永久对象地址。 */
    @Transactional(readOnly = true)
    public PresignedAccess issueAccess(long fileId) {
        CloudFile file = requireFile(fileId);
        authorization.requireReadPermission(operator.currentUserId(), file.ownership());
        if (file.status() != CloudFileStatus.AVAILABLE) {
            throw new IllegalStateException("文件当前不可访问");
        }
        return storageRegistry.require(file.storageLocation().providerCode())
                .issueAccess(file.storageLocation(), file.declaration().originalFileName(), accessValidity);
    }

    /** 软删除文件；物理对象由宽限期清理任务处理。 */
    @Transactional
    public void delete(long fileId) {
        CloudFile file = requireFile(fileId);
        authorization.requireDeletePermission(operator.currentUserId(), file.ownership());
        file.delete(OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC));
        repository.save(file);
    }

    private CloudFile requireFile(long fileId) {
        return repository.find(new CloudFileId(fileId))
                .orElseThrow(() -> new IllegalArgumentException("云文件不存在"));
    }
}
