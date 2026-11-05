package cn.jieyuc.user.infrastructure.persistence.entity;

import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * user_identity 表的持久化 Entity。
 *
 * <p>identifier 含完整登录标识。只生成 Getter/Setter，不生成 {@code toString()}，
 * 避免完整标识进入日志。</p>
 */
@Getter
@Setter
public class LoginIdentityEntity {
    private Long id;
    private Long userId;
    private String identityType;
    private String provider;
    private String identifier;
    private String identifierMasked;
    private OffsetDateTime verifiedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
