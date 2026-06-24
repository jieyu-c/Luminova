package cn.jieyuc.luminovaauth.controller;

import cn.jieyuc.luminovaauth.pojo.request.LoginRequest;
import cn.jieyuc.luminovaauth.pojo.response.LoginResponse;
import cn.jieyuc.luminovaauth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

}
