package cn.jieyuc.luminovaauth.application.verification;

import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.luminovaauth.domain.verification.VerificationChallenge;
import cn.jieyuc.luminovaauth.domain.verification.VerificationChallengeRepository;
import cn.jieyuc.luminovaauth.domain.verification.VerificationPurpose;
import cn.jieyuc.luminovaauth.application.security.SecureDigester;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 验证挑战应用服务。
 *
 * <p>负责生成安全随机验证码、保存摘要、发送短信和原子消费挑战。不创建用户或签发会话。
 * 短信发送失败时会删除/失效挑战的能力由仓储保存顺序和短 TTL 控制；调用失败不返回成功。</p>
 */
@Service
public class VerificationChallengeApplicationService {
    private static final Duration TTL = Duration.ofMinutes(5);
    private static final int MAX_FAILURES = 5;

    private final VerificationChallengeRepository repository;
    private final VerificationCodeRateLimiter rateLimiter;
    private final SmsSender smsSender;
    private final SecureDigester digester;
    private final SecureRandom random;
    private final Clock clock;

    public VerificationChallengeApplicationService(VerificationChallengeRepository repository,
                                                   VerificationCodeRateLimiter rateLimiter,
                                                   SmsSender smsSender,
                                                   SecureDigester digester) {
        this(repository, rateLimiter, smsSender, digester, new SecureRandom(), Clock.systemUTC());
    }

    VerificationChallengeApplicationService(VerificationChallengeRepository repository,
                                            VerificationCodeRateLimiter rateLimiter,
                                            SmsSender smsSender, SecureDigester digester,
                                            SecureRandom random, Clock clock) {
        this.repository = repository;
        this.rateLimiter = rateLimiter;
        this.smsSender = smsSender;
        this.digester = digester;
        this.random = random;
        this.clock = clock;
    }

    /**
     * 创建并发送一次验证挑战。
     */
    public VerificationCodeResult send(String phone, VerificationPurpose purpose, String clientIp) {
        String normalized = normalizePhone(phone);
        rateLimiter.checkAndRecord(normalized, clientIp);
        String requestId = UUID.randomUUID().toString();
        String code = "%06d".formatted(random.nextInt(1_000_000));
        String phoneDigest = digester.digest(normalized);
        String codeDigest = codeDigest(phoneDigest, purpose, requestId, code);
        repository.save(new VerificationChallenge(requestId, phoneDigest, codeDigest, purpose,
                OffsetDateTime.now(clock).plus(TTL), MAX_FAILURES));
        try {
            smsSender.send(normalized, code, purpose);
        } catch (RuntimeException exception) {
            // 短信没有成功交付时立即删除挑战，避免用户持有一个永远无法获知的有效验证码。
            repository.delete(requestId);
            throw exception;
        }
        return new VerificationCodeResult(requestId, TTL.toSeconds(), 60);
    }

    /**
     * 原子校验并消费验证码。
     *
     * @throws BizException 验证失败、过期或已经使用时统一抛出
     */
    public void consume(String requestId, String phone, String code, VerificationPurpose purpose) {
        String normalized = normalizePhone(phone);
        String phoneDigest = digester.digest(normalized);
        String codeDigest = codeDigest(phoneDigest, purpose, requestId, code);
        if (!repository.consume(requestId, phoneDigest, codeDigest, purpose)) {
            throw new BizException("VERIFICATION_CODE_INVALID", "验证码无效或已过期");
        }
    }

    /**
     * 规范化中国大陆手机号。
     */
    public String normalizePhone(String phone) {
        if (phone == null) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        String compact = phone.trim().replace(" ", "").replace("-", "");
        String local = compact.startsWith("+86") ? compact.substring(3) : compact;
        if (!local.matches("1[3-9]\\d{9}")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        return "+86" + local;
    }

    private String codeDigest(String phoneDigest, VerificationPurpose purpose,
                              String requestId, String code) {
        return digester.digest(phoneDigest + "|" + purpose.name() + "|" + requestId + "|" + code);
    }
}
