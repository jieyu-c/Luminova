package cn.jieyuc.user.interfaces.dto;

import cn.jieyuc.user.domain.account.AccountStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 管理员变更用户状态 HTTP 请求。
 */
@Getter
@Setter
public class ChangeUserStatusRequest {
    @NotNull
    private AccountStatus status;
    @NotBlank
    private String reason;
}
