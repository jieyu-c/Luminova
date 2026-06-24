package cn.jieyuc.luminovasatoken.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "luminova.sa-token")
public class LuminovaSaTokenProperties {

    private boolean enabled = true;

    private List<String> includePaths = new ArrayList<>(List.of("/**"));

    private List<String> excludePaths = new ArrayList<>(List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/logout",
            "/api/v1/auth/register",
            "/api/v1/user/register",
            "/api/v1/user/verify-code/send",
            "/error",
            "/actuator/**"
    ));

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getIncludePaths() {
        return includePaths;
    }

    public void setIncludePaths(List<String> includePaths) {
        this.includePaths = includePaths;
    }

    public List<String> getExcludePaths() {
        return excludePaths;
    }

    public void setExcludePaths(List<String> excludePaths) {
        this.excludePaths = excludePaths;
    }
}
