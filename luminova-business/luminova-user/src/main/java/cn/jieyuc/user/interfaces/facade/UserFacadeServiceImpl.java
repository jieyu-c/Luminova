package cn.jieyuc.user.interfaces.facade;

import cn.hutool.core.util.StrUtil;
import cn.jieyuc.luminova.api.user.response.data.UserInfo;
import cn.jieyuc.luminova.api.user.service.UserFacadeService;
import cn.jieyuc.user.application.service.UserApplicationService;
import cn.jieyuc.user.domain.model.user.Phone;
import cn.jieyuc.user.domain.model.user.User;
import cn.jieyuc.user.domain.model.user.UserId;
import cn.jieyuc.user.domain.repository.UserRepository;
import cn.jieyuc.user.interfaces.converter.UserInfoApiConverter;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService(version = "v1.0.0")
public class UserFacadeServiceImpl implements UserFacadeService {

    @Autowired
    private UserApplicationService userApplicationService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserInfo getUserInfo(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userApplicationService.queryById(UserId.of(userId));
        return UserInfoApiConverter.toApi(user);
    }

    @Override
    public UserInfo getUserInfoByPhone(String phone) {
        if (StrUtil.isBlank(phone)) {
            return null;
        }
        User user = userRepository.findByPhone(Phone.of(phone)).orElse(null);
        return UserInfoApiConverter.toApi(user);
    }

    @Override
    public UserInfo authenticate(String phone, String password) {
        if (StrUtil.isBlank(phone) || StrUtil.isBlank(password)) {
            return null;
        }
        User user = userRepository.findByPhone(Phone.of(phone)).orElse(null);
        if (user == null || !user.verifyPassword(password)) {
            return null;
        }
        user.ensureOperational();
        return UserInfoApiConverter.toApi(user);
    }
}
