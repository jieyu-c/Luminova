package cn.jieyuc.luminova.base.response;

/**
 * HTTP JSON 接口的统一响应信封。
 *
 * <p>成功时 {@code data} 可以为空但 {@code error} 必须为空；失败时 {@code data} 必须为空且
 * {@code error} 描述机器可读的错误分类和扩展信息。文件流、SSE、WebSocket 和 204 响应不使用本信封。</p>
 *
 * @param success 请求是否成功
 * @param code 稳定的机器可读响应码
 * @param message 面向调用方的安全提示
 * @param data 成功数据
 * @param error 失败详情
 * @param meta 请求关联元数据
 * @param <T> 成功数据类型
 */
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        ApiError error,
        ResponseMeta meta) {

    /**
     * 创建成功响应。
     *
     * @param data 成功数据，可以为空
     * @param meta 当前请求元数据，不能为空
     * @param <T> 成功数据类型
     * @return 满足成功不变量的统一响应
     */
    public static <T> ApiResponse<T> success(T data, ResponseMeta meta) {
        return new ApiResponse<>(true, ApiResponseCode.SUCCESS.code(),
                ApiResponseCode.SUCCESS.defaultMessage(), data, null, meta);
    }

    /**
     * 创建失败响应。
     *
     * @param code 稳定业务错误码
     * @param message 安全错误文案
     * @param error 结构化错误详情
     * @param meta 当前请求元数据
     * @return 不包含成功数据的统一响应
     */
    public static ApiResponse<Void> failure(String code, String message, ApiError error, ResponseMeta meta) {
        return new ApiResponse<>(false, code, message, null, error, meta);
    }
}
