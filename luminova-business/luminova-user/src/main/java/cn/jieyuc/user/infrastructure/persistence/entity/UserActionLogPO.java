package cn.jieyuc.user.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 用户行为日志表持久化对象。
 */
@Data
@TableName("sys_user_action_log")
public class UserActionLogPO {

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
     * 用户行为类型：REGISTER、LOGIN、LOGOUT 等用户相关行为
     */
    private String actionType;

    /**
     * 行为关联手机号
     */
    private String phone;

    /**
     * 行为发生IP地址
     */
    private String actionIp;

    /**
     * 客户端User-Agent
     */
    private String userAgent;

    /**
     * 行为是否成功
     */
    private Boolean success;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 创建时间（行为发生时间）
     */
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
