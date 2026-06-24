package cn.jieyuc.luminova.api.user.response.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserInfo  implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 可直接返回：用户唯一标识。
     */
    private Long userId;

    /**
     * 可直接返回：公开展示资料。
     */
    private String username;
    private String nickname;
    private String avatarUrl;
    private String status;

    /**
     * 脱敏后返回：手机号、邮箱只用于前端展示和用户确认。
     *
     * 不返回内容：明文手机号、明文邮箱、密码、密码哈希、登录 IP、登录失败次数、锁定/删除等安全内部字段。
     */
    private String maskedPhone;
    private String maskedEmail;
}
