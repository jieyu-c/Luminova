package cn.jieyuc.user.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * password_credential 表的持久化 Entity。
 *
 * <p>包含密码哈希。只生成 Getter/Setter，禁止 {@code @Data} 或 {@code @ToString}。
 * 表主键是 identity_id，不是默认的 id 列。</p>
 */
@Getter
@Setter
@TableName("password_credential")
public class PasswordCredentialEntity {
    @TableId(type = IdType.INPUT)
    private Long identityId;
    private String passwordHash;
    private OffsetDateTime passwordChangedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
