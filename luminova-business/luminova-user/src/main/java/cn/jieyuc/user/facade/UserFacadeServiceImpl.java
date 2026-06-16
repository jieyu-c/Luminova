package cn.jieyuc.user.facade;

import cn.jieyuc.api.user.request.UserRegisterRequest;
import cn.jieyuc.api.user.response.UserOperateResponse;
import cn.jieyuc.api.user.service.UserFacadeService;
import cn.jieyuc.user.domain.service.UserService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService(version = "v1.0.0")
public class UserFacadeServiceImpl implements UserFacadeService {

    @Autowired
    private UserService userService;

    @Override
    public UserOperateResponse register(UserRegisterRequest request) {
        return null;
    }
}
