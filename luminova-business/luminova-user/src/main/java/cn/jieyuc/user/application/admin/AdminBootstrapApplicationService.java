package cn.jieyuc.user.application.admin;

import cn.jieyuc.user.application.account.RegisterUserApplicationService;
import cn.jieyuc.user.domain.account.IdentityKey;
import cn.jieyuc.user.domain.account.IdentityType;
import cn.jieyuc.user.domain.account.PhoneNumber;
import cn.jieyuc.user.domain.account.Role;
import cn.jieyuc.user.domain.account.UserAccount;
import cn.jieyuc.user.domain.account.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * 首个管理员部署初始化用例。
 *
 * <p>用例可重复执行：已有账号只提升角色，不覆盖密码；不存在时创建账号后提升角色。
 * 原始密码只来自部署密钥，不得写入日志或普通配置文件。</p>
 */
@Service
public class AdminBootstrapApplicationService {
    private final UserAccountRepository repository;
    private final RegisterUserApplicationService registerService;

    public AdminBootstrapApplicationService(UserAccountRepository repository,
                                            RegisterUserApplicationService registerService) {
        this.repository = repository;
        this.registerService = registerService;
    }

    /** 幂等初始化管理员。 */
    @Transactional
    public void initialize(String rawPhone, String rawPassword) {
        PhoneNumber phone = PhoneNumber.normalize(rawPhone);
        IdentityKey key = new IdentityKey(IdentityType.PHONE, "LOCAL", phone.value());
        UserAccount account = repository.findByIdentity(key)
                .orElseGet(() -> registerService.registerPhone(phone.value(), rawPassword));
        if (account.role() != Role.ADMIN) {
            account.grantAdminRole(OffsetDateTime.now());
            repository.save(account);
        }
    }
}
