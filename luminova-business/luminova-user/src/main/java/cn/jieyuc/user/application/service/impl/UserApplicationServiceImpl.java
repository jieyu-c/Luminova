package cn.jieyuc.user.application.service.impl;

import cn.hutool.core.lang.Assert;
import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.luminova.base.exceptions.BizExceptionEnums;
import cn.jieyuc.luminova.lock.annotation.DistributeLock;
import cn.jieyuc.user.application.service.UserApplicationService;
import cn.jieyuc.user.domain.model.actionlog.UserActionLog;
import cn.jieyuc.user.domain.model.actionlog.UserActionType;
import cn.jieyuc.user.domain.model.user.*;
import cn.jieyuc.user.domain.repository.UserActionLogRepository;
import cn.jieyuc.user.domain.repository.UserRepository;
import cn.jieyuc.user.domain.service.RegisterVerifyCodeSender;
import cn.jieyuc.user.domain.service.RegisterVerifyCodeVerifier;
import cn.jieyuc.user.domain.service.UsernameGenerator;
import cn.jieyuc.user.domain.service.UsernameUniquenessChecker;
import cn.jieyuc.user.infrastructure.audit.UserActionLogRecorder;
import cn.jieyuc.user.infrastructure.ratelimit.RegisterRateLimiter;
import cn.jieyuc.user.infrastructure.web.UserExceptionEnums;
import cn.jieyuc.user.infrastructure.web.UserSessionInvalidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserApplicationServiceImpl implements UserApplicationService {

    private static final int REGISTER_USERNAME_RETRY = 3;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserActionLogRepository userActionLogRepository;

    @Autowired
    private UserActionLogRecorder userActionLogRecorder;

    @Autowired
    private UsernameGenerator usernameGenerator;

    @Autowired
    private UsernameUniquenessChecker usernameUniquenessChecker;

    @Autowired
    private RegisterVerifyCodeSender registerVerifyCodeSender;

    @Autowired
    private RegisterVerifyCodeVerifier registerVerifyCodeVerifier;

    @Autowired
    private RegisterRateLimiter registerRateLimiter;

    @Autowired
    private UserSessionInvalidator userSessionInvalidator;

    @Override
    public RegisterVerifyCodeSender.SendResult sendRegisterVerifyCode(String phone) {
        Phone registerPhone = Phone.of(phone);
        return registerVerifyCodeSender.send(registerPhone);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributeLock(keyExp = "#phone", scene = "user_register")
    public User register(String phone, String password, String verifyCode) {
        Phone registerPhone = Phone.of(phone);
        registerRateLimiter.check(registerPhone);
        registerVerifyCodeVerifier.verify(registerPhone, verifyCode);

        User existingUser = userRepository.findByPhone(registerPhone).orElse(null);
        if (existingUser != null) {
            userActionLogRecorder.record(
                    UserActionLog.failure(null, UserActionType.REGISTER, registerPhone, "Phone has been register")
            );
            throw new BizException(UserExceptionEnums.USER_PHONE_EXIST.name(), "Phone has been register");
        }

        Password registerPassword = Password.of(password);
        if (!registerPassword.verify()) {
            userActionLogRecorder.record(
                    UserActionLog.failure(null, UserActionType.REGISTER, registerPhone, "Password is invalid")
            );
            throw new BizException(UserExceptionEnums.USER_PASSWORD_ERROR.name(), "Password is invalid");
        }

        for (int attempt = 0; attempt < REGISTER_USERNAME_RETRY; attempt++) {
            Username temporaryUsername = usernameGenerator.generateTemporary();
            try {
                User registerUser = User.register(registerPhone, temporaryUsername, registerPassword);
                registerUser = userRepository.save(registerUser);
                usernameUniquenessChecker.markUsed(temporaryUsername);
                userActionLogRepository.save(
                        UserActionLog.success(registerUser.getId(), UserActionType.REGISTER, registerPhone)
                );
                return registerUser;
            } catch (DuplicateKeyException ex) {
                usernameUniquenessChecker.markUsed(temporaryUsername);
                if (userRepository.findByPhone(registerPhone).isPresent()) {
                    userActionLogRecorder.record(
                            UserActionLog.failure(null, UserActionType.REGISTER, registerPhone, "Phone has been register")
                    );
                    throw new BizException(UserExceptionEnums.USER_PHONE_EXIST.name(), "Phone has been register");
                }
            }
        }
        throw new BizException(BizExceptionEnums.GENERIC_ERROR.name(), "Register is failed");
    }

    @Override
    public User queryById(UserId userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateProfile(UserId userId, String username, String nickname) {
        UsernameUpdateContext context = prepareUsernameUpdate(userId, username);
        User user = context.user();
        Username newUsername = context.newUsername();

        user.updateProfile(newUsername, Nickname.of(nickname));
        try {
            User updatedUser = userRepository.save(user);
            usernameUniquenessChecker.markUsed(newUsername);
            userActionLogRepository.save(UserActionLog.success(userId, UserActionType.PROFILE_UPDATE, user.getPhone()));
            return updatedUser;
        } catch (DuplicateKeyException ex) {
            throw new BizException(UserExceptionEnums.USERNAME_TAKEN.name(), "Username is taken");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateUsername(UserId userId, String username) {
        UsernameUpdateContext context = prepareUsernameUpdate(userId, username);
        User user = context.user();
        Username newUsername = context.newUsername();

        user.updateUsername(newUsername);
        try {
            User updatedUser = userRepository.save(user);
            usernameUniquenessChecker.markUsed(newUsername);
            userActionLogRepository.save(UserActionLog.success(userId, UserActionType.PROFILE_UPDATE, user.getPhone()));
            return updatedUser;
        } catch (DuplicateKeyException ex) {
            throw new BizException(UserExceptionEnums.USERNAME_TAKEN.name(), "Username is taken");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateAvatar(UserId userId, String avatarUrl) {
        User user = requireOperationalUser(userId);
        user.updateAvatar(AvatarUrl.of(avatarUrl));
        User updatedUser = userRepository.save(user);
        userActionLogRepository.save(UserActionLog.success(userId, UserActionType.PROFILE_UPDATE, user.getPhone()));
        return updatedUser;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(UserId userId, String oldPassword, String newPassword) {
        User user = requireOperationalUser(userId);

        if (!user.verifyPassword(oldPassword)) {
            userActionLogRecorder.record(
                    UserActionLog.failure(userId, UserActionType.PASSWORD_CHANGE, user.getPhone(), "Old password wrong")
            );
            throw new BizException(UserExceptionEnums.OLD_PASSWORD_WRONG.name(), "Old password is wrong");
        }

        Password newPasswordValue = Password.of(newPassword);
        try {
            user.changePassword(newPasswordValue);
        } catch (IllegalArgumentException ex) {
            userActionLogRecorder.record(
                    UserActionLog.failure(userId, UserActionType.PASSWORD_CHANGE, user.getPhone(), ex.getMessage())
            );
            throw new BizException(UserExceptionEnums.USER_PASSWORD_ERROR.name(), ex.getMessage());
        }

        userRepository.save(user);
        userActionLogRepository.save(UserActionLog.success(userId, UserActionType.PASSWORD_CHANGE, user.getPhone()));
        userSessionInvalidator.invalidateAllSessions(userId);
    }

    private UsernameUpdateContext prepareUsernameUpdate(UserId userId, String username) {
        User user = requireOperationalUser(userId);

        Username newUsername = Username.of(username);
        Assert.isTrue(
                usernameUniquenessChecker.isAvailable(newUsername, userId),
                () -> new BizException(UserExceptionEnums.USERNAME_TAKEN.name(), "Username is taken")
        );
        return new UsernameUpdateContext(user, newUsername);
    }

    private User requireOperationalUser(UserId userId) {
        User user = userRepository.findById(userId).orElse(null);
        Assert.notNull(user, () -> new BizException(UserExceptionEnums.USER_NOT_EXIST.name(), "User not exist"));
        try {
            user.ensureOperational();
        } catch (IllegalStateException ex) {
            throw new BizException(UserExceptionEnums.USER_ACCOUNT_UNAVAILABLE.name(), "User account is unavailable");
        }
        return user;
    }

    private record UsernameUpdateContext(User user, Username newUsername) {
    }
}
