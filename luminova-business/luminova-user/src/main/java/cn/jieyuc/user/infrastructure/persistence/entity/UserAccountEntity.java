package cn.jieyuc.user.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * user_account 表的持久化 Entity。
 *
 * <p>该对象只在基础设施层使用，不得直接返回给 Controller 或跨上下文接口。
 * 主键由领域分配，因此使用 {@link IdType#INPUT}。{@code version} 交给 MyBatis-Plus
 * 乐观锁插件，更新时自动带上版本条件。头像和最近登录时间允许写成 null。</p>
 */
@Getter
@Setter
@TableName("user_account")
public class UserAccountEntity {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String nickname;
    @TableField(insertStrategy = FieldStrategy.ALWAYS, updateStrategy = FieldStrategy.ALWAYS)
    private String avatarUrl;
    private String role;
    private String status;
    @TableField(insertStrategy = FieldStrategy.ALWAYS, updateStrategy = FieldStrategy.ALWAYS)
    private OffsetDateTime lastLoginAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    @Version
    private Integer version;
}
