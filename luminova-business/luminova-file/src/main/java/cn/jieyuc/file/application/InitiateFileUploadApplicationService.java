package cn.jieyuc.file.application;

import cn.jieyuc.file.application.port.AuthenticatedFileOperator;
import cn.jieyuc.file.application.port.FileBusinessAuthorizationPort;
import cn.jieyuc.file.application.port.FileIdentifierGenerator;
import cn.jieyuc.file.application.port.storage.ObjectStoragePort;
import cn.jieyuc.file.application.port.storage.PresignedUpload;
import cn.jieyuc.file.application.port.storage.StorageProviderRegistry;
import cn.jieyuc.file.domain.CloudFile;
import cn.jieyuc.file.domain.CloudFileId;
import cn.jieyuc.file.domain.CloudFileRepository;
import cn.jieyuc.file.domain.StorageLocation;
import cn.jieyuc.file.domain.StorageProviderCode;
import cn.jieyuc.file.domain.UploadSession;
import cn.jieyuc.file.domain.UploadSessionId;
import cn.jieyuc.file.domain.UploadSessionRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 创建文件记录、上传会话并签发普通 PUT 地址的用例服务。 */
@Service
public class InitiateFileUploadApplicationService {
    private final CloudFileRepository fileRepository;
    private final UploadSessionRepository sessionRepository;
    private final FileIdentifierGenerator identifierGenerator;
    private final AuthenticatedFileOperator operator;
    private final FileBusinessAuthorizationPort authorization;
    private final StorageProviderRegistry storageRegistry;
    private final Clock clock;
    private final String bucket;
    private final Duration sessionValidity;

    public InitiateFileUploadApplicationService(CloudFileRepository fileRepository,
                                                UploadSessionRepository sessionRepository,
                                                FileIdentifierGenerator identifierGenerator,
                                                AuthenticatedFileOperator operator,
                                                FileBusinessAuthorizationPort authorization,
                                                StorageProviderRegistry storageRegistry,
                                                Clock clock,
                                                @Value("${luminova.file.storage.bucket:luminova-private}") String bucket,
                                                @Value("${luminova.file.upload-session-validity:PT30M}") Duration sessionValidity) {
        this.fileRepository = fileRepository;
        this.sessionRepository = sessionRepository;
        this.identifierGenerator = identifierGenerator;
        this.operator = operator;
        this.authorization = authorization;
        this.storageRegistry = storageRegistry;
        this.clock = clock;
        this.bucket = bucket;
        this.sessionValidity = sessionValidity;
    }

    /**
     * 在单一数据库事务中创建文件和会话；相同用户与幂等键返回已有会话。
     * 预签名不写远程状态，因此可以在事务内安全重新计算。
     */
    @Transactional
    public InitiatedFileUpload initiate(InitiateFileUploadCommand command) {
        long operatorId = operator.currentUserId();
        authorization.requireUploadPermission(operatorId, command.ownership(), command.purpose());
        command.purpose().validate(command.declaration());
        OffsetDateTime now = OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        UploadSession existing = sessionRepository.findByUploaderAndIdempotencyKey(operatorId, command.idempotencyKey())
                .orElse(null);
        if (existing != null) {
            CloudFile file = fileRepository.find(existing.fileId())
                    .orElseThrow(() -> new IllegalStateException("幂等上传会话缺少文件记录"));
            return signed(existing, file);
        }
        CloudFileId fileId = new CloudFileId(identifierGenerator.nextId());
        UploadSessionId sessionId = new UploadSessionId(identifierGenerator.nextId());
        StorageLocation location = new StorageLocation(StorageProviderCode.RUSTFS, bucket,
                objectKey(fileId, command.purpose(), now));
        CloudFile file = CloudFile.create(fileId, operatorId, command.ownership(), command.purpose(),
                command.declaration(), location, now);
        UploadSession session = UploadSession.create(sessionId, fileId, operatorId, now, now.plus(sessionValidity));
        fileRepository.save(file);
        sessionRepository.save(session, command.idempotencyKey());
        return signed(session, file);
    }

    private InitiatedFileUpload signed(UploadSession session, CloudFile file) {
        ObjectStoragePort storage = storageRegistry.require(file.storageLocation().providerCode());
        PresignedUpload signed = storage.issueSingleUpload(file.storageLocation(), file.declaration(), sessionValidity);
        return new InitiatedFileUpload(session.id().value(), file.id().value(), signed.url(),
                signed.requiredHeaders(), signed.expiresAt());
    }

    /** 生成不包含用户输入和隐私字段的对象键。 */
    private String objectKey(CloudFileId fileId, cn.jieyuc.file.domain.UploadPurpose purpose, OffsetDateTime now) {
        String value = Long.toString(fileId.value());
        String prefix = value.substring(0, Math.min(2, value.length()));
        return "%s/%s/%04d/%02d/%s/%s".formatted("default", purpose.name().toLowerCase(),
                now.getYear(), now.getMonthValue(), prefix, value);
    }
}
