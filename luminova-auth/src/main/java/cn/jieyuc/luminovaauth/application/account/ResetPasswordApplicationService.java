package cn.jieyuc.luminovaauth.application.account;

import cn.jieyuc.luminovaauth.application.port.account.AccountGateway;

import cn.jieyuc.luminova.api.account.AccountAuthSnapshot;
import cn.jieyuc.luminovaauth.application.port.session.SessionIssuer;
import cn.jieyuc.luminovaauth.application.verification.VerificationChallengeApplicationService;
import cn.jieyuc.luminovaauth.domain.verification.VerificationPurpose;
import org.springframework.stereotype.Service;

/**
 * 找回密码应用服务。
 *
 * <p>消费重置验证码并更新账号上下文凭证，更新成功后撤销该用户全部会话。若撤销会话
 * 临时失败，密码已经生效且调用会失败，运维告警必须介入；生产可进一步升级为可靠事件。</p>
 */
@Service
public class ResetPasswordApplicationService {
    private final VerificationChallengeApplicationService verificationService;
    private final AccountGateway accountGateway;
    private final SessionIssuer sessionIssuer;

    public ResetPasswordApplicationService(VerificationChallengeApplicationService verificationService,
                                           AccountGateway accountGateway,
                                           SessionIssuer sessionIssuer) {
        this.verificationService = verificationService;
        this.accountGateway = accountGateway;
        this.sessionIssuer = sessionIssuer;
    }

    /**
     * 验证手机号后重置密码并撤销全部会话。
     */
    public void reset(String requestId, String phone, String code, String newPassword) {
        verificationService.consume(requestId, phone, code, VerificationPurpose.RESET_PASSWORD);
        String normalizedPhone = verificationService.normalizePhone(phone);
        AccountAuthSnapshot snapshot = accountGateway.resetPhonePassword(normalizedPhone, newPassword);
        sessionIssuer.revokeAll(snapshot.userId());
    }
}
