package cn.jieyuc.canvas.infrastructure.persistence.cahce;

import cn.jieyuc.canvas.domain.model.project.CreativeProject;
import cn.jieyuc.canvas.domain.model.project.ProjectId;
import cn.jieyuc.canvas.domain.model.project.ProjectStatus;
import cn.jieyuc.canvas.infrastructure.persistence.converter.ProjectConverter;
import cn.jieyuc.canvas.infrastructure.persistence.entity.CreativeProjectPO;
import cn.jieyuc.canvas.infrastructure.persistence.mapper.CreativeProjectMapper;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CacheProjectQuery {

    private static final int CACHE_EXPIRE_SECONDS = 1800;

    @Autowired
    private CreativeProjectMapper creativeProjectMapper;

    private final ProjectConverter converter = ProjectConverter.INSTANCE;

    @Cached(
            name = ProjectCacheNames.BY_ID,
            key = "#projectId.value",
            expire = CACHE_EXPIRE_SECONDS,
            cacheType = CacheType.BOTH,
            cacheNullValue = true
    )
    public CreativeProject findById(ProjectId projectId) {
        CreativeProjectPO projectPO = creativeProjectMapper.selectById(projectId.value());
        return projectPO == null ? null : converter.toDomain(projectPO);
    }

    @Cached(
            name = ProjectCacheNames.BY_OWNER_STATUS,
            key = "#ownerId + ':' + (#status == null ? 'ALL' : #status.name())",
            expire = CACHE_EXPIRE_SECONDS,
            cacheType = CacheType.BOTH,
            cacheNullValue = true
    )
    public List<CreativeProject> listByOwnerId(Long ownerId, ProjectStatus status) {
        LambdaQueryWrapper<CreativeProjectPO> queryWrapper = new LambdaQueryWrapper<CreativeProjectPO>()
                .eq(CreativeProjectPO::getOwnerId, ownerId)
                .isNull(CreativeProjectPO::getDeletedAt)
                .orderByDesc(CreativeProjectPO::getUpdatedAt);
        if (status != null) {
            queryWrapper.eq(CreativeProjectPO::getStatus, status.code());
        }
        return creativeProjectMapper.selectList(queryWrapper).stream()
                .map(converter::toDomain)
                .toList();
    }

    @CacheInvalidate(name = ProjectCacheNames.BY_ID, key = "#projectId.value")
    public void evictById(ProjectId projectId) {
    }

    @CacheInvalidate(name = ProjectCacheNames.BY_OWNER_STATUS, key = "#ownerId + ':ALL'")
    public void evictOwnerProjects(Long ownerId) {
    }

    @CacheInvalidate(name = ProjectCacheNames.BY_OWNER_STATUS, key = "#ownerId + ':' + #status.name()")
    public void evictOwnerProjectsByStatus(Long ownerId, ProjectStatus status) {
    }
}
