package cn.jieyuc.file.domain;

import java.util.Optional;

/** 文件引用账本仓储端口。 */
public interface FileReferenceRepository {
    /** 按业务上下文内稳定引用键查询。 */
    Optional<FileReference> find(BusinessOwnership ownership, String referenceType, String referenceKey);

    /** 保存新增或释放后的引用。 */
    void save(FileReference reference);

    /** 判断文件是否仍存在活动引用。 */
    boolean hasActiveReference(CloudFileId fileId);
}
