package cn.jieyuc.file.infrastructure.persistence;

import cn.jieyuc.file.domain.UploadSession;
import cn.jieyuc.file.domain.UploadSessionId;
import cn.jieyuc.file.domain.UploadSessionRepository;
import cn.jieyuc.file.infrastructure.persistence.conversion.UploadSessionPersistenceConverter;
import cn.jieyuc.file.infrastructure.persistence.entity.UploadSessionEntity;
import cn.jieyuc.file.infrastructure.persistence.mapper.UploadSessionMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** 上传会话仓储，使用数据库唯一键作为并发幂等的最终防线。 */
@Repository
public class UploadSessionRepositoryAdapter implements UploadSessionRepository {
    private final UploadSessionMapper mapper;
    private final UploadSessionPersistenceConverter converter;

    public UploadSessionRepositoryAdapter(UploadSessionMapper mapper, UploadSessionPersistenceConverter converter) {
        this.mapper = mapper;
        this.converter = converter;
    }

    /** 按主键查询上传会话。 */
    @Override
    public Optional<UploadSession> find(UploadSessionId id) {
        return Optional.ofNullable(mapper.selectById(id.value())).map(converter::toDomain);
    }

    /** 按用户和幂等键查询首次创建的会话。 */
    @Override
    public Optional<UploadSession> findByUploaderAndIdempotencyKey(long uploaderId, String idempotencyKey) {
        return Optional.ofNullable(mapper.selectOne(Wrappers.<UploadSessionEntity>lambdaQuery()
                .eq(UploadSessionEntity::getUploaderId, uploaderId)
                .eq(UploadSessionEntity::getIdempotencyKey, idempotencyKey))).map(converter::toDomain);
    }

    /** 插入时写入幂等键，更新时保留原键并依赖乐观锁。 */
    @Override
    public void save(UploadSession session, String idempotencyKey) {
        UploadSessionEntity existing = mapper.selectById(session.id().value());
        UploadSessionEntity entity = converter.toEntity(session);
        int affected;
        if (existing == null) {
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                throw new IllegalArgumentException("新上传会话必须提供幂等键");
            }
            entity.setIdempotencyKey(idempotencyKey);
            affected = mapper.insert(entity);
        } else {
            entity.setIdempotencyKey(existing.getIdempotencyKey());
            affected = mapper.updateById(entity);
        }
        if (affected != 1) {
            throw new IllegalStateException("上传会话发生并发修改");
        }
    }
}
