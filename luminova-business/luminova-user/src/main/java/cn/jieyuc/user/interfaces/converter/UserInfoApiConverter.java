package cn.jieyuc.user.interfaces.converter;

import cn.jieyuc.user.domain.model.user.User;

public final class UserInfoApiConverter {

    private UserInfoApiConverter() {
    }

    public static cn.jieyuc.luminova.api.user.response.data.UserInfo toApi(User user) {
        cn.jieyuc.user.domain.model.user.UserInfo domainInfo = cn.jieyuc.user.domain.model.user.UserInfo.from(user);
        if (domainInfo == null) {
            return null;
        }

        cn.jieyuc.luminova.api.user.response.data.UserInfo apiInfo =
                new cn.jieyuc.luminova.api.user.response.data.UserInfo();
        apiInfo.setUserId(domainInfo.getUserId());
        apiInfo.setUsername(domainInfo.getUsername());
        apiInfo.setNickname(domainInfo.getNickname());
        apiInfo.setAvatarUrl(domainInfo.getAvatarUrl());
        apiInfo.setStatus(domainInfo.getStatus());
        apiInfo.setMaskedPhone(domainInfo.getMaskedPhone());
        apiInfo.setMaskedEmail(domainInfo.getMaskedEmail());
        return apiInfo;
    }
}
