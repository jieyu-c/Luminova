package cn.jieyuc.user.infrastructure.persistence.entity;

import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * user_account 表的持久化 Entity。
 *
 * <p>该对象只在基础设施层使用，不得直接返回给 Controller 或跨上下文接口。
 * 访问器由 Lombok 生成，不手写 Getter/Setter。</p>
 */
@Getter
@Setter
public class UserAccountEntity {
    private Long id;
    private String nickname;
    private String avatarUrl;
    private String role;
    private String status;
    private OffsetDateTime lastLoginAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Integer version;
}
