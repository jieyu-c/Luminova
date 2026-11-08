package cn.jieyuc.file.domain;

import java.util.Optional;

/** 上传会话聚合仓储端口。 */
public interface UploadSessionRepository {
    /** 按稳定标识查询会话。 */
    Optional<UploadSession> find(UploadSessionId id);

    /** 按幂等键查询同一上传请求。 */
    Optional<UploadSession> findByUploaderAndIdempotencyKey(long uploaderId, String idempotencyKey);

    /** 新增或按版本保存会话，并在首次保存时绑定幂等键。 */
    void save(UploadSession session, String idempotencyKey);
}
