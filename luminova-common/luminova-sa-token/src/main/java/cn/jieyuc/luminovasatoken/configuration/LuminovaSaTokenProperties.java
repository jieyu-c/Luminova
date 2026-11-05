package cn.jieyuc.luminovasatoken.configuration;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Sa-Token 接入配置。
 *
 * <p>{@code enabled} 使用基本类型，Lombok 生成 {@code isEnabled()}，与配置绑定约定一致。</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "luminova.sa-token")
public class LuminovaSaTokenProperties {
    private boolean enabled = true;

    private List<String> includePaths = new ArrayList<>(List.of("/**"));

    private List<String> excludePaths = new ArrayList<>(List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/login/password",
            "/api/v1/auth/logout",
            "/api/v1/auth/register",
            "/api/v1/auth/verification-codes",
            "/api/v1/auth/password/reset",
            "/api/v1/user/register",
            "/api/v1/user/verify-code/send",
            "/error",
            "/actuator/**"
    ));
}
