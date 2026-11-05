package cn.jieyuc.luminovaauth.application.login;

import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.luminovaauth.application.session.IssuedSession;
import cn.jieyuc.luminovaauth.application.session.SessionIssuer;
import cn.jieyuc.luminovaauth.application.security.SecurityAuditRecorder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 统一登录管线应用测试。
 */
class LoginApplicationServiceTest {

    @Test
    @DisplayName("任意登录策略成功后都应通过统一会话端口签发 userId 会话")
    void shouldIssueSessionAfterAuthentication() {
        var authenticator = new FakeAuthenticator(new AuthenticatedPrincipal(10, 20, "USER", "ACTIVE"));
        var registry = new LoginAuthenticatorRegistry(List.of(authenticator));
        var limiter = new FakeRateLimiter();
        var session = new FakeSessionIssuer();
        var service = new LoginApplicationService(registry, limiter, session, new FakeAuditRecorder());

        LoginResult result = service.login(command());

        assertEquals(10, result.userId());
        assertEquals(10, session.issuedUserId);
        assertEquals(1, limiter.successes);
    }

    @Test
    @DisplayName("策略失败时不得签发会话并应记录身份失败次数")
    void shouldNotIssueSessionWhenAuthenticationFails() {
        LoginAuthenticator<PhonePasswordLoginCommand> authenticator = new LoginAuthenticator<>() {
            public LoginType supports() {
                return LoginType.PHONE_PASSWORD;
            }
            public Class<PhonePasswordLoginCommand> commandType() {
                return PhonePasswordLoginCommand.class;
            }
            public AuthenticatedPrincipal authenticate(PhonePasswordLoginCommand command) {
                throw new BizException("INVALID_CREDENTIALS", "手机号或密码错误");
            }
        };
        var limiter = new FakeRateLimiter();
        var session = new FakeSessionIssuer();
        var service = new LoginApplicationService(
                new LoginAuthenticatorRegistry(List.of(authenticator)), limiter, session,
                new FakeAuditRecorder());

        assertThrows(BizException.class, () -> service.login(command()));
        assertEquals(0, session.issuedUserId);
        assertEquals(1, limiter.failures);
    }

    private PhonePasswordLoginCommand command() {
        return new PhonePasswordLoginCommand("+8613800138000", "Secret123", "127.0.0.1", "test", "r1");
    }

    private static final class FakeAuthenticator implements LoginAuthenticator<PhonePasswordLoginCommand> {
        private final AuthenticatedPrincipal principal;
        private FakeAuthenticator(AuthenticatedPrincipal principal) {
            this.principal = principal;
        }
        public LoginType supports() {
            return LoginType.PHONE_PASSWORD;
        }
        public Class<PhonePasswordLoginCommand> commandType() {
            return PhonePasswordLoginCommand.class;
        }
        public AuthenticatedPrincipal authenticate(PhonePasswordLoginCommand command) {
            return principal;
        }
    }

    private static final class FakeRateLimiter implements LoginRateLimiter {
        private int successes;
        private int failures;
        public void checkAttempt(LoginType type, String identifier, String ip) {
        }
        public void onSuccess(LoginType type, String identifier) {
            successes++;
        }
        public void onFailure(LoginType type, String identifier) {
            failures++;
        }
    }

    private static final class FakeSessionIssuer implements SessionIssuer {
        private long issuedUserId;
        public IssuedSession issue(AuthenticatedPrincipal principal) {
            issuedUserId = principal.userId();
            return new IssuedSession("token", 100);
        }
        public void revokeCurrent() {
        }
        public void revokeAll(long userId) {
        }
    }

    private static final class FakeAuditRecorder implements SecurityAuditRecorder {
        public void loginSucceeded(long userId, String maskedIdentifier, String ip,
                                   String userAgent, String requestId) {
        }
        public void loginFailed(String maskedIdentifier, String ip, String userAgent,
                                String requestId) {
        }
    }
}
