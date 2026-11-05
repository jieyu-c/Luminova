package cn.jieyuc.user.interfaces.controller;

import cn.jieyuc.user.application.admin.AdminUserApplicationService;
import cn.jieyuc.user.application.admin.AdminUserPage;
import cn.jieyuc.user.application.admin.AdminUserSummary;
import cn.jieyuc.user.interfaces.dto.ChangeUserStatusRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员用户管理 HTTP Controller。
 *
 * <p>只转换分页和状态变更协议，管理员角色由应用层基于账号聚合重新确认。</p>
 */
@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {
    private final AdminUserApplicationService applicationService;

    public AdminUserController(AdminUserApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /** 分页查询脱敏用户摘要。 */
    @GetMapping
    public AdminUserPage query(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "20") int size,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) Long userId,
                               @RequestParam(required = false) String phone) {
        return applicationService.query(page, size, status, userId, phone);
    }

    /** 启用或禁用目标用户。 */
    @PatchMapping("/{userId}/status")
    public AdminUserSummary changeStatus(@PathVariable long userId,
                                         @Valid @RequestBody ChangeUserStatusRequest request) {
        return applicationService.changeStatus(userId, request.getStatus(), request.getReason());
    }
}
