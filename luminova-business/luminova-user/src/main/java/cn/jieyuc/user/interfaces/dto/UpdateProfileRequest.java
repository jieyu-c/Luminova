package cn.jieyuc.user.interfaces.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 更新本人资料 HTTP 请求。
 *
 * <p>字段访问器由 Lombok 生成，校验仍由应用层和聚合完成。</p>
 */
@Getter
@Setter
public class UpdateProfileRequest {
    private String nickname;
    private String avatarUrl;
}
