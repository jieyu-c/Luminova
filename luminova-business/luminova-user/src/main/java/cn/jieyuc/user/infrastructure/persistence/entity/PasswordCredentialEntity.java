package cn.jieyuc.user.infrastructure.persistence.entity;

import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * password_credential 表的持久化 Entity。
 *
 * <p>包含密码哈希。只生成 Getter/Setter，禁止 {@code @Data} 或 {@code @ToString}，
 * 避免哈希进入日志或异常。</p>
 */
@Getter
@Setter
public class PasswordCredentialEntity {
    private Long identityId;
    private String passwordHash;
    private OffsetDateTime passwordChangedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
