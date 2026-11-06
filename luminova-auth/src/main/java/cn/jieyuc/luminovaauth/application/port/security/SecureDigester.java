package cn.jieyuc.luminovaauth.application.port.security;

/**
 * 敏感标识安全摘要端口。
 *
 * <p>摘要用于缓存键与一次性验证码比较，具体 HMAC 算法和密钥来源由基础设施隐藏。</p>
 */
public interface SecureDigester {

    /** 计算不可逆摘要。 */
    String digest(String value);
}
