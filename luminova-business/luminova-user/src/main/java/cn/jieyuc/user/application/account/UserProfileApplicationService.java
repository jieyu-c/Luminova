package cn.jieyuc.user.application.account;

import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.user.application.port.AuthenticatedUserProvider;
import cn.jieyuc.user.domain.account.UserAccount;
import cn.jieyuc.user.domain.account.UserAccountRepository;
import cn.jieyuc.user.domain.account.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * 用户本人资料应用服务。
 *
 * <p>只允许读取和修改当前会话对应用户的昵称与头像，不接受目标用户 ID，因此协议层无法
 * 通过篡改参数越权访问其他用户。</p>
 */
@Service
public class UserProfileApplicationService {
    private final UserAccountRepository repository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public UserProfileApplicationService(UserAccountRepository repository,
                                         AuthenticatedUserProvider authenticatedUserProvider) {
        this.repository = repository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    /** 查询当前用户资料。 */
    @Transactional(readOnly = true)
    public UserProfileView currentProfile() {
        return toView(loadCurrent());
    }

    /** 更新当前用户可自行维护的资料字段。 */
    @Transactional
    public UserProfileView updateCurrentProfile(String nickname, String avatarUrl) {
        UserAccount account = loadCurrent();
        if (nickname == null && avatarUrl == null) {
            throw new IllegalArgumentException("昵称和头像至少需要提供一项");
        }
        account.updateProfile(nickname == null ? account.nickname() : nickname,
                avatarUrl == null ? account.avatarUrl() : avatarUrl, OffsetDateTime.now());
        repository.save(account);
        return toView(account);
    }

    private UserAccount loadCurrent() {
        return repository.findById(new UserId(authenticatedUserProvider.currentUserId()))
                .orElseThrow(() -> new BizException("USER_NOT_FOUND", "用户不存在"));
    }

    private UserProfileView toView(UserAccount account) {
        String maskedPhone = account.identities().stream()
                .filter(identity -> identity.key().type().name().equals("PHONE"))
                .findFirst().map(identity -> identity.maskedIdentifier()).orElse(null);
        return new UserProfileView(account.id().value(), maskedPhone, account.nickname(),
                account.avatarUrl(), account.role().name(), account.status().name(), account.createdAt());
    }
}
