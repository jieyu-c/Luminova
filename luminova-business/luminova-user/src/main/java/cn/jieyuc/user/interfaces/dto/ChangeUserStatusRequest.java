package cn.jieyuc.user.interfaces.dto;

import cn.jieyuc.user.domain.account.AccountStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 管理员变更用户状态 HTTP 请求。
 */
public class ChangeUserStatusRequest {
    @NotNull private AccountStatus status;
    @NotBlank private String reason;

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
