package cn.jieyuc.file.application.port.storage;

import cn.jieyuc.file.domain.StorageLocation;
import cn.jieyuc.file.domain.StorageProviderCode;
import cn.jieyuc.file.domain.UploadDeclaration;
import java.time.Duration;

/**
 * 对象存储端口。
 *
 * <p>接口只暴露 Luminova 自有模型；实现负责供应商 SDK、超时、签名和异常映射。</p>
 */
public interface ObjectStoragePort {
    /** 返回当前 Adapter 对应的提供方。 */
    StorageProviderCode providerCode();

    /** 为普通 PUT 上传签发短时凭证。 */
    PresignedUpload issueSingleUpload(StorageLocation location, UploadDeclaration declaration, Duration validity);

    /** 查询对象元数据，用于完成上传时校验声明。 */
    StoredObjectMetadata inspect(StorageLocation location);

    /** 为私有对象签发短时 GET 地址。 */
    PresignedAccess issueAccess(StorageLocation location, String downloadName, Duration validity);

    /** 幂等删除物理对象。 */
    void delete(StorageLocation location);
}
