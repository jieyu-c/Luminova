package cn.jieyuc.user.interfaces.dto;

/**
 * 更新本人资料 HTTP 请求。
 */
public class UpdateProfileRequest {
    private String nickname;
    private String avatarUrl;

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
