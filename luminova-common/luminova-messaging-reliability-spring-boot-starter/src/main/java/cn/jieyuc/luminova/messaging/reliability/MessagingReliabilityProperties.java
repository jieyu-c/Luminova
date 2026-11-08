package cn.jieyuc.luminova.messaging.reliability;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

/**
 * Outbox 与幂等消费的运行参数。
 */
@ConfigurationProperties("luminova.messaging.reliability")
@Getter
@Setter
public class MessagingReliabilityProperties {

    private boolean enabled;
    private int batchSize = 100;
    private Duration leaseDuration = Duration.ofSeconds(30);
    private Duration initialBackoff = Duration.ofSeconds(1);
    private Duration maximumBackoff = Duration.ofMinutes(30);
    private String instanceId;

}
