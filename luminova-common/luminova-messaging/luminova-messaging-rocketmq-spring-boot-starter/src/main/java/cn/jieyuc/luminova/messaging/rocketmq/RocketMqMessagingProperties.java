package cn.jieyuc.luminova.messaging.rocketmq;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RocketMQ Adapter 启动配置。
 *
 * <p>凭证应由环境变量或 Secret 系统注入，不得记录到日志。</p>
 */
@ConfigurationProperties("luminova.messaging.rocketmq")
public class RocketMqMessagingProperties {

    private boolean enabled;
    private String endpoints;
    private String namespace;
    private boolean sslEnabled;
    private Duration requestTimeout = Duration.ofSeconds(3);
    private int maximumAttempts = 3;
    private int maximumMessageBytes = 4 * 1024 * 1024;
    private Map<String, String> topics = new LinkedHashMap<>();
    private Map<String, String> consumerGroups = new LinkedHashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(String endpoints) {
        this.endpoints = endpoints;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public int getMaximumAttempts() {
        return maximumAttempts;
    }

    public void setMaximumAttempts(int maximumAttempts) {
        this.maximumAttempts = maximumAttempts;
    }

    public int getMaximumMessageBytes() {
        return maximumMessageBytes;
    }

    public void setMaximumMessageBytes(int maximumMessageBytes) {
        this.maximumMessageBytes = maximumMessageBytes;
    }

    public Map<String, String> getTopics() {
        return topics;
    }

    public void setTopics(Map<String, String> topics) {
        this.topics = topics;
    }

    public Map<String, String> getConsumerGroups() {
        return consumerGroups;
    }

    public void setConsumerGroups(Map<String, String> consumerGroups) {
        this.consumerGroups = consumerGroups;
    }
}
