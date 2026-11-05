package cn.jieyuc.luminovaauth.application.verification;

import cn.jieyuc.luminovaauth.domain.verification.VerificationPurpose;

/**
 * 短信发送端口。
 *
 * <p>端口只负责发送指定验证码，不生成验证码，也不决定发送频率。实现不得记录完整手机号
 * 和明文验证码。</p>
 */
public interface SmsSender {

    /**
     * 发送一次性验证码。
     *
     * @param phone   规范化手机号，属于敏感个人信息
     * @param code    明文验证码，发送完成后不得持久化
     * @param purpose 验证用途
     */
    void send(String phone, String code, VerificationPurpose purpose);
}
