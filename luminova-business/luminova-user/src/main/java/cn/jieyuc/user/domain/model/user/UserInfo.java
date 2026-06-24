package cn.jieyuc.user.domain.model.user;

import lombok.Data;

@Data
public class UserInfo {
    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String status;
    private String maskedPhone;
    private String maskedEmail;

    public static UserInfo from(User user) {
        if (user == null) {
            return null;
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId() == null ? null : user.getId().value());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setStatus(user.getStatus() == null ? null : user.getStatus().name());
        userInfo.setMaskedPhone(maskPhone(user.getPhone()));
        userInfo.setMaskedEmail(maskEmail(user.getEmail()));
        return userInfo;
    }

    private static String maskPhone(Phone phone) {
        if (phone == null || phone.value() == null) {
            return null;
        }
        String value = phone.value();
        if (value.length() < 7) {
            return "****";
        }
        return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
    }

    private static String maskEmail(Email email) {
        if (email == null || email.value() == null) {
            return null;
        }
        String value = email.value();
        int atIndex = value.indexOf('@');
        if (atIndex <= 0) {
            return "****";
        }

        String local = value.substring(0, atIndex);
        String domain = value.substring(atIndex);
        if (local.length() == 1) {
            return local.charAt(0) + "****" + domain;
        }
        return local.charAt(0) + "****" + local.charAt(local.length() - 1) + domain;
    }
}
