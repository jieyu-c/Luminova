package cn.jieyuc.user.domain.service.impl;


import cn.jieyuc.user.domain.repo.UserRepo;
import cn.jieyuc.user.pojo.request.LoginRequest;
import cn.jieyuc.user.pojo.response.LoginResponse;
import cn.jieyuc.user.repo.mapper.UserMapper;
import cn.jieyuc.user.intra.entity.UserEntity;
import cn.jieyuc.user.domain.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {
    @Autowired
    private UserRepo userRepo;
    @Override
    public LoginResponse login(LoginRequest request) {

        return null;
    }
}
