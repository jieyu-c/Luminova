package cn.jieyuc.luminovaauth.infrastructure.security;

import cn.jieyuc.luminovaauth.application.port.security.SecureDigester;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

/**
 * 敏感标识 HMAC 摘要模块。
 *
 * <p>用于 Redis key 和验证码摘要，避免在运维界面暴露完整手机号。密钥必须由部署环境
 * 注入并在生产环境使用高强度随机值。</p>
 */
@Component
public class IdentifierDigester implements SecureDigester {
    private final byte[] secret;

    public IdentifierDigester(@Value("${luminova.security.hmac-secret:development-only-change-me}") String secret) {
        if (secret == null || secret.length() < 16) {
            throw new IllegalArgumentException("HMAC 密钥长度不能少于 16 个字符");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 计算不可逆 HMAC-SHA-256 十六进制摘要。
     */
    @Override
    public String digest(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("无法计算安全摘要", exception);
        }
    }
}
