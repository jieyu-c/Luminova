package cn.jieyuc.file.infrastructure.persistence;

import cn.jieyuc.file.domain.CloudFile;
import cn.jieyuc.file.domain.CloudFileId;
import cn.jieyuc.file.domain.CloudFileRepository;
import cn.jieyuc.file.infrastructure.persistence.conversion.CloudFilePersistenceConverter;
import cn.jieyuc.file.infrastructure.persistence.entity.CloudFileEntity;
import cn.jieyuc.file.infrastructure.persistence.mapper.CloudFileMapper;
import java.util.Optional;
import java.time.OffsetDateTime;
import java.util.List;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Repository;

/** 使用 MyBatis-Plus 保存云文件聚合，并将乐观锁冲突显式暴露。 */
@Repository
public class CloudFileRepositoryAdapter implements CloudFileRepository {
    private final CloudFileMapper mapper;
    private final CloudFilePersistenceConverter converter;

    public CloudFileRepositoryAdapter(CloudFileMapper mapper, CloudFilePersistenceConverter converter) {
        this.mapper = mapper;
        this.converter = converter;
    }

    /** 按主键读取并转换聚合。 */
    @Override
    public Optional<CloudFile> find(CloudFileId id) {
        return Optional.ofNullable(mapper.selectById(id.value())).map(converter::toDomain);
    }

    /** 首次插入，后续按版本更新；版本冲突不覆盖并发结果。 */
    @Override
    public void save(CloudFile file) {
        CloudFileEntity entity = converter.toEntity(file);
        int affected = mapper.selectById(file.id().value()) == null ? mapper.insert(entity) : mapper.updateById(entity);
        if (affected != 1) {
            throw new IllegalStateException("云文件发生并发修改");
        }
    }

    /** 使用分页能力限制单批清理量，避免一次装载全部历史文件。 */
    @Override
    public List<CloudFile> findDeletedBefore(OffsetDateTime deadline, int limit) {
        return mapper.selectPage(Page.of(1, Math.max(1, Math.min(limit, 500))),
                        Wrappers.<CloudFileEntity>lambdaQuery()
                        .eq(CloudFileEntity::getStatus, cn.jieyuc.file.domain.CloudFileStatus.DELETED.name())
                        .le(CloudFileEntity::getDeletedAt, deadline)
                        .orderByAsc(CloudFileEntity::getDeletedAt))
                .getRecords().stream().map(converter::toDomain).toList();
    }
}
