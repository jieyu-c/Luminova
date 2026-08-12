package cn.jieyuc.canvas.domain.repo;

import cn.jieyuc.canvas.domain.model.project.CreativeProject;
import cn.jieyuc.canvas.domain.model.project.ProjectId;
import cn.jieyuc.canvas.domain.model.project.ProjectStatus;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.Optional;

public interface CreativeProjectRepo {
    /**
     * 保存项目
     * @param project 项目信息
     * @return 新的项目信息
     */
     CreativeProject save(CreativeProject project);

     Optional<CreativeProject> findById(ProjectId projectId);

    /**
     * 按项目所有者分页查询项目。
     *
     * @param ownerId 所有者 ID
     * @param status 项目状态，可为空
     * @param pageNo 页码，从 1 开始
     * @param pageSize 每页数量
     * @return 项目分页
     */
     IPage<CreativeProject> pageByOwnerId(Long ownerId, ProjectStatus status, long pageNo, long pageSize);
}
