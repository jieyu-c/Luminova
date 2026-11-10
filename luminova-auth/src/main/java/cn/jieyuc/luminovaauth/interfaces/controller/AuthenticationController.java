package cn.jieyuc.luminovaauth.interfaces.controller;

import cn.jieyuc.luminovaauth.application.account.RegisterAccountApplicationService;
import cn.jieyuc.luminovaauth.application.account.RegistrationResult;
import cn.jieyuc.luminovaauth.application.account.ResetPasswordApplicationService;
import cn.jieyuc.luminovaauth.application.login.LoginApplicationService;
import cn.jieyuc.luminovaauth.application.login.LoginResult;
import cn.jieyuc.luminovaauth.application.login.PhonePasswordLoginCommand;
import cn.jieyuc.luminovaauth.application.session.SessionApplicationService;
import cn.jieyuc.luminovaauth.application.verification.VerificationChallengeApplicationService;
import cn.jieyuc.luminovaauth.application.verification.VerificationCodeResult;
import cn.jieyuc.luminovaauth.interfaces.dto.AuthSessionResponse;
import cn.jieyuc.luminovaauth.interfaces.dto.PhonePasswordLoginRequest;
import cn.jieyuc.luminovaauth.interfaces.dto.RegisterRequest;
import cn.jieyuc.luminovaauth.interfaces.dto.ResetPasswordRequest;
import cn.jieyuc.luminovaauth.interfaces.dto.SendVerificationCodeRequest;
import cn.jieyuc.luminovaauth.interfaces.dto.VerificationCodeResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 认证上下文 HTTP Controller。
 *
 * <p>只完成请求校验、协议元数据提取和响应映射，不查询数据库、不操作 Redis，
 * 也不直接调用 Sa-Token。</p>
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    private final LoginApplicationService loginService;
    private final RegisterAccountApplicationService registerService;
    private final ResetPasswordApplicationService resetPasswordService;
    private final VerificationChallengeApplicationService verificationService;
    private final SessionApplicationService sessionService;

    public AuthenticationController(LoginApplicationService loginService,
                                    RegisterAccountApplicationService registerService,
                                    ResetPasswordApplicationService resetPasswordService,
                                    VerificationChallengeApplicationService verificationService,
                                    SessionApplicationService sessionService) {
        this.loginService = loginService;
        this.registerService = registerService;
        this.resetPasswordService = resetPasswordService;
        this.verificationService = verificationService;
        this.sessionService = sessionService;
    }

    /** 发送注册或重置密码验证码。 */
    @PostMapping("/verification-codes")
    public ResponseEntity<VerificationCodeResponse> sendCode(
            @Valid @RequestBody SendVerificationCodeRequest body,
            HttpServletRequest request) {
        VerificationCodeResult result = verificationService.send(
                body.getPhone(), body.getPurpose(), clientIp(request));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new VerificationCodeResponse(
                result.requestId(), result.expiresInSeconds(), result.retryAfterSeconds()));
    }

    /** 使用验证码注册手机号用户。 */
    @PostMapping("/register")
    public AuthSessionResponse register(@Valid @RequestBody RegisterRequest body) {
        RegistrationResult result = registerService.register(body.getRequestId(), body.getPhone(),
                body.getCode(), body.getPassword());
        return new AuthSessionResponse(result.token(), result.expiresInSeconds(),
                result.userId(), result.role());
    }

    /** 使用手机号密码登录。 */
    @PostMapping("/login/password")
    public AuthSessionResponse passwordLogin(@Valid @RequestBody PhonePasswordLoginRequest body,
                                             HttpServletRequest request) {
        LoginResult result = loginService.login(new PhonePasswordLoginCommand(
                body.getPhone(), body.getPassword(), clientIp(request),
                request.getHeader("User-Agent"), requestId(request)));
        return new AuthSessionResponse(result.token(), result.expiresInSeconds(),
                result.userId(), result.role());
    }

    /** 幂等注销当前会话。 */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        sessionService.logout();
        return ResponseEntity.noContent().build();
    }

    /** 使用短信挑战找回密码。 */
    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest body) {
        resetPasswordService.reset(body.getRequestId(), body.getPhone(),
                body.getCode(), body.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    private String requestId(HttpServletRequest request) {
        String value = request.getHeader("X-Request-Id");
        return value == null || value.isBlank() ? "unknown" : value;
    }

    private String clientIp(HttpServletRequest request) {
        // 服务仅通过受信任 Gateway 暴露时，Gateway 应先清洗 X-Forwarded-For；否则使用远端地址。
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",", 2)[0].trim();
        }
        return request.getRemoteAddr();
    }
}
