package cn.jieyuc.user.infrastructure.verify;

import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.user.domain.model.user.Phone;
import cn.jieyuc.user.domain.service.RegisterVerifyCodeVerifier;
import cn.jieyuc.user.infrastructure.web.UserExceptionEnums;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisRegisterVerifyCodeVerifier implements RegisterVerifyCodeVerifier {

    private static final String KEY_PREFIX = "user:register:verify:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${luminova.user.verify-code.mock-enabled:false}")
    private boolean mockEnabled;

    @Value("${luminova.user.verify-code.mock-code:888888}")
    private String mockCode;

    @Override
    public void verify(Phone phone, String verifyCode) {
        if (verifyCode == null || verifyCode.isBlank()) {
            throw new BizException(UserExceptionEnums.USER_PHONE_VERIFY_ERROR.name(), "Verify code is required");
        }
        String trimmedCode = verifyCode.trim();
        if (mockEnabled && mockCode.equals(trimmedCode)) {
            return;
        }
        String key = KEY_PREFIX + phone.value();
        String expectedCode = stringRedisTemplate.opsForValue().get(key);
        if (expectedCode == null || !expectedCode.equals(trimmedCode)) {
            throw new BizException(UserExceptionEnums.USER_PHONE_VERIFY_ERROR.name(), "Verify code is invalid");
        }
        stringRedisTemplate.delete(key);
    }
}
