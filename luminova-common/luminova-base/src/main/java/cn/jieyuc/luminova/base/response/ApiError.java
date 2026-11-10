package cn.jieyuc.luminova.base.response;

import java.util.Map;

/**
 * 统一响应中的结构化错误详情。
 *
 * <p>{@code details} 只能放置可安全返回给客户端的诊断信息，不得包含异常堆栈、密钥、Token、
 * 验证码或完整个人标识。</p>
 *
 * @param type 错误类别
 * @param field 单字段错误关联字段，非字段错误为空
 * @param rejectedValue 被拒绝值；敏感字段必须为空
 * @param details 扩展错误信息
 */
public record ApiError(
        ApiErrorType type,
        String field,
        Object rejectedValue,
        Map<String, Object> details) {
}
