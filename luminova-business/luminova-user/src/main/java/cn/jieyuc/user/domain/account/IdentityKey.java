package cn.jieyuc.user.domain.account;

import java.util.Locale;
import java.util.Objects;

/**
 * 登录身份的全局唯一键。
 *
 * <p>身份类型、提供方和规范化标识共同确定唯一身份。该值对象不负责把手机号或邮箱
 * 规范化，调用方必须先使用对应的规范化模块。</p>
 */
public record IdentityKey(IdentityType type, String provider, String identifier) {

    /**
     * 校验并创建身份键。
     */
    public IdentityKey {
        Objects.requireNonNull(type, "身份类型不能为空");
        provider = requireText(provider, "身份提供方").toUpperCase(Locale.ROOT);
        identifier = requireText(identifier, "身份标识");
        if (provider.length() > 64 || identifier.length() > 255) {
            throw new IllegalArgumentException("登录身份字段长度超出限制");
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + "不能为空");
        }
        return value.trim();
    }
}
