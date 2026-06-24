package cn.jieyuc.luminovaauth.pojo.response;

import cn.jieyuc.luminova.base.response.BaseResponse;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LoginResponse extends BaseResponse {

    private String token;

    private Long userId;

    private String username;

    private String avatarUrl;

    public LoginResponse(String token, Long userId, String username, String avatarUrl) {
        setSuccess(true);
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.avatarUrl = avatarUrl;
    }
}
