package cn.jieyuc.user.intra.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 用户登录日志表实体
 */
@Data
@TableName("sys_user_login_log")
public class SysUserLoginLog {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID（关联 sys_user.id）
     */
    private Long userId;

    /**
     * 登录手机号
     */
    private String phone;

    /**
     * 登录IP地址
     */
    private String loginIp;

    /**
     * 客户端User-Agent
     */
    private String userAgent;

    /**
     * 是否登录成功
     */
    private Boolean success;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 创建时间（登录时间）
     */
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}