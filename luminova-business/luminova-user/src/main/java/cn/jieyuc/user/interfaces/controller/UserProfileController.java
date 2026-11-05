package cn.jieyuc.user.interfaces.controller;

import cn.jieyuc.user.application.account.ChangePasswordApplicationService;
import cn.jieyuc.user.application.account.UserProfileApplicationService;
import cn.jieyuc.user.application.account.UserProfileView;
import cn.jieyuc.user.interfaces.dto.ChangePasswordRequest;
import cn.jieyuc.user.interfaces.dto.UpdateProfileRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户本人资料 HTTP Controller。
 *
 * <p>目标用户由应用层从可信会话中取得，本 Controller 不接受 userId，因此无法通过请求
 * 参数访问其他用户。</p>
 */
@RestController
@RequestMapping("/api/v1/users/me")
public class UserProfileController {
    private final UserProfileApplicationService profileService;
    private final ChangePasswordApplicationService passwordService;

    public UserProfileController(UserProfileApplicationService profileService,
                                 ChangePasswordApplicationService passwordService) {
        this.profileService = profileService;
        this.passwordService = passwordService;
    }

    /** 查询本人资料。 */
    @GetMapping
    public UserProfileView getProfile() {
        return profileService.currentProfile();
    }

    /** 更新本人昵称或头像。 */
    @PatchMapping
    public UserProfileView updateProfile(@RequestBody UpdateProfileRequest request) {
        return profileService.updateCurrentProfile(request.getNickname(), request.getAvatarUrl());
    }

    /** 修改密码并撤销全部会话。 */
    @PostMapping("/password/change")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        passwordService.change(request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }
}
