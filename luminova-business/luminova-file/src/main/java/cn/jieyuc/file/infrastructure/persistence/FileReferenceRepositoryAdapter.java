package cn.jieyuc.file.infrastructure.persistence;

import cn.jieyuc.file.domain.BusinessOwnership;
import cn.jieyuc.file.domain.CloudFileId;
import cn.jieyuc.file.domain.FileReference;
import cn.jieyuc.file.domain.FileReferenceRepository;
import cn.jieyuc.file.infrastructure.persistence.conversion.FileReferencePersistenceConverter;
import cn.jieyuc.file.infrastructure.persistence.entity.FileReferenceEntity;
import cn.jieyuc.file.infrastructure.persistence.mapper.FileReferenceMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** 使用唯一引用键维护可审计引用账本。 */
@Repository
public class FileReferenceRepositoryAdapter implements FileReferenceRepository {
    private final FileReferenceMapper mapper;
    private final FileReferencePersistenceConverter converter;

    public FileReferenceRepositoryAdapter(FileReferenceMapper mapper, FileReferencePersistenceConverter converter) {
        this.mapper = mapper;
        this.converter = converter;
    }

    /** 按完整业务引用键查询。 */
    @Override
    public Optional<FileReference> find(BusinessOwnership ownership, String referenceType, String referenceKey) {
        return Optional.ofNullable(mapper.selectOne(Wrappers.<FileReferenceEntity>lambdaQuery()
                .eq(FileReferenceEntity::getBusinessContext, ownership.contextCode())
                .eq(FileReferenceEntity::getBusinessObjectType, ownership.objectType())
                .eq(FileReferenceEntity::getBusinessObjectId, ownership.objectId())
                .eq(FileReferenceEntity::getReferenceType, referenceType)
                .eq(FileReferenceEntity::getReferenceKey, referenceKey))).map(converter::toDomain);
    }

    /** 新增引用或更新释放时间。 */
    @Override
    public void save(FileReference reference) {
        FileReferenceEntity entity = converter.toEntity(reference);
        int affected = mapper.selectById(reference.id()) == null ? mapper.insert(entity) : mapper.updateById(entity);
        if (affected != 1) {
            throw new IllegalStateException("文件引用保存失败");
        }
    }

    /** 判断文件是否存在尚未释放的引用。 */
    @Override
    public boolean hasActiveReference(CloudFileId fileId) {
        return mapper.selectCount(Wrappers.<FileReferenceEntity>lambdaQuery()
                .eq(FileReferenceEntity::getFileId, fileId.value())
                .isNull(FileReferenceEntity::getReleasedAt)) > 0;
    }
}
