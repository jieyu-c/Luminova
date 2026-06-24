package cn.jieyuc.user.infrastructure.verify;

import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.user.domain.model.user.Phone;
import cn.jieyuc.user.domain.service.RegisterVerifyCodeSender;
import cn.jieyuc.user.infrastructure.web.UserExceptionEnums;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class RedisRegisterVerifyCodeSender implements RegisterVerifyCodeSender {

    private static final String KEY_PREFIX = "user:register:verify:";
    private static final String SEND_COOLDOWN_PREFIX = "user:register:verify:cooldown:";
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRES_SECONDS = 300;
    private static final int SEND_COOLDOWN_SECONDS = 60;

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${luminova.user.verify-code.expose:false}")
    private boolean exposeVerifyCode;

    @Override
    public SendResult send(Phone phone) {
        String cooldownKey = SEND_COOLDOWN_PREFIX + phone.value();
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(cooldownKey))) {
            throw new BizException(
                    UserExceptionEnums.VERIFY_CODE_RATE_LIMIT.name(),
                    "Verify code sent too frequently, please try again later"
            );
        }

        String code = generateCode();
        String verifyKey = KEY_PREFIX + phone.value();
        stringRedisTemplate.opsForValue().set(verifyKey, code, Duration.ofSeconds(EXPIRES_SECONDS));
        stringRedisTemplate.opsForValue().set(cooldownKey, "1", SEND_COOLDOWN_SECONDS, TimeUnit.SECONDS);

        return new SendResult(exposeVerifyCode ? code : null, EXPIRES_SECONDS);
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, CODE_LENGTH);
        int code = secureRandom.nextInt(bound / 10, bound);
        return String.valueOf(code);
    }
}
