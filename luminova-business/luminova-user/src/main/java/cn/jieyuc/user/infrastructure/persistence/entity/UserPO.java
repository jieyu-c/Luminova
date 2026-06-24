package cn.jieyuc.user.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 系统用户表持久化对象。
 */
@Data
@TableName("sys_user")
public class UserPO {
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 密码哈希
     */
    private String passwordHash;

    /**
     * 状态：0 - 禁用, 1 - 正常, 2 - 锁定, 3 - 注销
     */
    private Short status;

    /**
     * 最后登录时间
     */
    private OffsetDateTime lastLoginAt;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 密码更新时间
     */
    private OffsetDateTime passwordUpdatedAt;

    /**
     * 手机验证时间
     */
    private OffsetDateTime phoneVerifiedAt;

    /**
     * 邮箱验证时间
     */
    private OffsetDateTime emailVerifiedAt;

    /**
     * 登录失败次数
     */
    private Integer failedLoginCount;

    /**
     * 锁定截止时间
     */
    private OffsetDateTime lockedUntil;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;

    /**
     * 软删除时间
     */
    @TableLogic(value = "null", delval = "now()")
    private OffsetDateTime deletedAt;
}
