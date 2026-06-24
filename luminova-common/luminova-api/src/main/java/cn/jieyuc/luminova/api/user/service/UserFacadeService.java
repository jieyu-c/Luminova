package cn.jieyuc.luminova.api.user.service;


import cn.jieyuc.luminova.api.user.response.data.UserInfo;

public interface UserFacadeService {
    UserInfo getUserInfo(Long userId);

    UserInfo getUserInfoByPhone(String phone);

    UserInfo authenticate(String phone, String password);

}
