package cn.jieyuc.user.infrastructure.persistence.entity;

import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 管理员分页查询的只读投影。
 *
 * <p>投影只包含脱敏手机号，不得增加完整 identifier 字段。访问器由 Lombok 生成，
 * Setter 仅供 MyBatis 映射使用。</p>
 */
@Getter
@Setter
public class AdminUserRow {
    private Long userId;
    private String maskedPhone;
    private String nickname;
    private String role;
    private String status;
    private OffsetDateTime createdAt;
}
