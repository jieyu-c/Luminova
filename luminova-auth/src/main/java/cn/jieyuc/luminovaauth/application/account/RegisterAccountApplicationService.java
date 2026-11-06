package cn.jieyuc.luminovaauth.application.account;

import cn.jieyuc.luminovaauth.application.port.account.AccountGateway;

import cn.jieyuc.luminova.api.account.AccountAuthSnapshot;
import cn.jieyuc.luminovaauth.application.login.AuthenticatedPrincipal;
import cn.jieyuc.luminovaauth.application.session.IssuedSession;
import cn.jieyuc.luminovaauth.application.port.session.SessionIssuer;
import cn.jieyuc.luminovaauth.application.verification.VerificationChallengeApplicationService;
import cn.jieyuc.luminovaauth.domain.verification.VerificationPurpose;
import org.springframework.stereotype.Service;

/**
 * 公开注册用例应用服务。
 *
 * <p>按顺序消费注册验证码、调用账号上下文原子创建用户、签发会话。验证码消费成功后
 * 如果远程注册失败，用户需要重新获取验证码；这是 MVP 为避免跨服务分布式事务采用的
 * 明确取舍。</p>
 */
@Service
public class RegisterAccountApplicationService {
    private final VerificationChallengeApplicationService verificationService;
    private final AccountGateway accountGateway;
    private final SessionIssuer sessionIssuer;

    public RegisterAccountApplicationService(VerificationChallengeApplicationService verificationService,
                                             AccountGateway accountGateway,
                                             SessionIssuer sessionIssuer) {
        this.verificationService = verificationService;
        this.accountGateway = accountGateway;
        this.sessionIssuer = sessionIssuer;
    }

    /**
     * 完成手机号用户注册并建立首次会话。
     */
    public RegistrationResult register(String requestId, String phone, String code, String password) {
        verificationService.consume(requestId, phone, code, VerificationPurpose.REGISTER);
        String normalizedPhone = verificationService.normalizePhone(phone);
        AccountAuthSnapshot snapshot = accountGateway.registerPhoneUser(normalizedPhone, password);
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(snapshot.userId(), snapshot.identityId(),
                snapshot.role(), snapshot.status());
        IssuedSession session = sessionIssuer.issue(principal);
        return new RegistrationResult(session.token(), session.expiresInSeconds(),
                snapshot.userId(), snapshot.role());
    }
}
