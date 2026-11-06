package cn.jieyuc.user.infrastructure.persistence.entity;

import cn.jieyuc.user.infrastructure.persistence.typehandler.PostgreSqlInetTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * security_audit_log 表的追加记录。
 *
 * <p>只用于插入。IP 列是 PostgreSQL {@code inet}，必须走专用 TypeHandler，
 * 不能把普通字符串直接绑定到该列。</p>
 */
@Getter
@Setter
@TableName("security_audit_log")
public class SecurityAuditLogEntity {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String eventType;
    private Long actorUserId;
    private Long targetUserId;
    private String identityType;
    private String identifierMasked;
    private String result;
    private String reasonCode;
    @TableField(typeHandler = PostgreSqlInetTypeHandler.class)
    private String ip;
    private String userAgent;
    private String requestId;
    private OffsetDateTime createdAt;
}
