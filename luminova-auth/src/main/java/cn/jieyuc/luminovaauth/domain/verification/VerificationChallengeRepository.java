package cn.jieyuc.luminovaauth.domain.verification;

/**
 * 验证挑战仓储 seam。
 *
 * <p>实现必须原子完成摘要比较、失败计数和成功删除，不能使用“先查再删”的非原子流程。</p>
 */
public interface VerificationChallengeRepository {

    /** 保存一个带过期时间的新挑战。 */
    void save(VerificationChallenge challenge);

    /** 删除发送失败或主动失效的挑战。 */
    void delete(String requestId);

    /**
     * 原子校验并消费挑战。
     *
     * @return 是否成功消费；失败时不区分错误、过期或已使用
     */
    boolean consume(String requestId, String phoneDigest, String codeDigest,
                    VerificationPurpose purpose);
}
