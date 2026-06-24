package cn.jieyuc.user.domain.model.user;

import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    private UserId id;
    private Phone phone;
    private Email email;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String passwordHash;
    private UserStatus status;
    private OffsetDateTime lastLoginAt;
    private String lastLoginIp;

    public static User register(Phone phone, Username username, Password password) {
        return User.builder()
                .phone(phone)
                .username(username.value())
                .nickname(Nickname.ofUnchecked(username.value()).value())
                .passwordHash(password.passwordHash())
                .status(UserStatus.NORMAL)
                .build();
    }

    public void updateUsername(Username username) {
        this.username = username.value();
    }

    public void updateProfile(Username username, Nickname nickname) {
        this.username = username.value();
        this.nickname = nickname.value();
    }

    public void updateAvatar(AvatarUrl avatarUrl) {
        this.avatarUrl = avatarUrl.value();
    }

    public boolean verifyPassword(String rawPassword) {
        return Password.matches(rawPassword, this.passwordHash);
    }

    public void ensureOperational() {
        if (this.status != UserStatus.NORMAL) {
            throw new IllegalStateException("User account is unavailable");
        }
    }

    public void changePassword(Password newPassword) {
        if (!newPassword.verify()) {
            throw new IllegalArgumentException("New password is invalid");
        }
        if (Password.matches(newPassword.password(), this.passwordHash)) {
            throw new IllegalArgumentException("New password must differ from old password");
        }
        this.passwordHash = newPassword.passwordHash();
    }
}
