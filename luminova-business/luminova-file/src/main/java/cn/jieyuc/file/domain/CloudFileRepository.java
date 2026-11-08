package cn.jieyuc.file.domain;

import java.util.Optional;
import java.time.OffsetDateTime;
import java.util.List;

/** 云文件聚合仓储端口。 */
public interface CloudFileRepository {
    /** 按稳定标识查询文件。 */
    Optional<CloudFile> find(CloudFileId id);

    /** 新增或按版本保存文件聚合。 */
    void save(CloudFile file);

    /** 查询超过软删除宽限期、等待物理清理的文件。 */
    List<CloudFile> findDeletedBefore(OffsetDateTime deadline, int limit);
}
