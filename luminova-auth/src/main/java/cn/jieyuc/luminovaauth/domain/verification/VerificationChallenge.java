package cn.jieyuc.luminovaauth.domain.verification;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * 一次性验证挑战聚合根。
 *
 * <p>聚合保存手机号摘要、验证码摘要、用途和过期时间，不保存明文验证码或手机号。
 * 原子校验与消费由仓储 Adapter 完成，避免并发请求重复使用同一验证码。</p>
 */
public record VerificationChallenge(String requestId, String phoneDigest, String codeDigest,
                                    VerificationPurpose purpose, OffsetDateTime expiresAt,
                                    int maxFailures) {

    /** 校验并创建挑战。 */
    public VerificationChallenge {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("验证请求 ID 不能为空");
        }
        Objects.requireNonNull(phoneDigest, "手机号摘要不能为空");
        Objects.requireNonNull(codeDigest, "验证码摘要不能为空");
        Objects.requireNonNull(purpose, "验证用途不能为空");
        Objects.requireNonNull(expiresAt, "过期时间不能为空");
        if (maxFailures < 1) {
            throw new IllegalArgumentException("最大失败次数必须为正数");
        }
    }
}
