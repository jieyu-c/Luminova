package cn.jieyuc.luminovaauth.domain.verification;

/**
 * 验证挑战用途。
 *
 * <p>不同用途的验证码不能互相复用。</p>
 */
public enum VerificationPurpose {
    REGISTER,
    RESET_PASSWORD
}
