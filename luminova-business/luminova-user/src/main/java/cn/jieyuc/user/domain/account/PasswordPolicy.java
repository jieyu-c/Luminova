package cn.jieyuc.user.domain.account;

/**
 * 本地密码业务规则。
 *
 * <p>该领域模块只判断原始密码是否满足业务要求，不执行哈希，也不读取配置中心。</p>
 */
public final class PasswordPolicy {

    /**
     * 校验密码长度和组成。
     *
     * @param rawPassword 原始密码；禁止记录日志
     */
    public void validate(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 8 || rawPassword.length() > 64) {
            throw new IllegalArgumentException("密码长度必须为 8 到 64 个字符");
        }
        boolean hasLetter = rawPassword.codePoints().anyMatch(Character::isLetter);
        boolean hasDigit = rawPassword.codePoints().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new IllegalArgumentException("密码必须同时包含字母和数字");
        }
    }
}
