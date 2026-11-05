package cn.jieyuc.user.infrastructure.bootstrap;

import cn.jieyuc.user.application.admin.AdminBootstrapApplicationService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 首个管理员部署初始化 Adapter。
 *
 * <p>只有显式设置 {@code LUMINOVA_ADMIN_BOOTSTRAP_ENABLED=true} 时运行。手机号和密码必须
 * 由部署密钥注入；本类不会输出这些值。初始化完成后应从部署环境移除一次性密钥。</p>
 */
@Component
@ConditionalOnProperty(prefix = "luminova.admin.bootstrap", name = "enabled", havingValue = "true")
public class AdminBootstrapRunner implements ApplicationRunner {
    private final AdminBootstrapApplicationService applicationService;
    private final String phone;
    private final String password;

    public AdminBootstrapRunner(AdminBootstrapApplicationService applicationService,
                                @Value("${luminova.admin.bootstrap.phone}") String phone,
                                @Value("${luminova.admin.bootstrap.password}") String password) {
        this.applicationService = applicationService;
        this.phone = phone;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        applicationService.initialize(phone, password);
    }
}
