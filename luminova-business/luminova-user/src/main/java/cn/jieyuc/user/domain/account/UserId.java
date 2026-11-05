package cn.jieyuc.user.domain.account;

/**
 * 稳定的用户主体标识。
 */
public record UserId(long value) {

    /**
     * 创建合法用户 ID。
     *
     * @param value 正整数 ID
     */
    public UserId {
        if (value <= 0) {
            throw new IllegalArgumentException("用户 ID 必须为正数");
        }
    }
}
