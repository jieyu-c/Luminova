package cn.jieyuc.user.service.impl;


import cn.jieyuc.user.domain.user.UserRepo;
import cn.jieyuc.user.pojo.request.LoginRequest;
import cn.jieyuc.user.pojo.response.LoginResponse;
import cn.jieyuc.user.repo.mapper.UserMapper;
import cn.jieyuc.user.pojo.entity.UserEntity;
import cn.jieyuc.user.service.UserService;
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
