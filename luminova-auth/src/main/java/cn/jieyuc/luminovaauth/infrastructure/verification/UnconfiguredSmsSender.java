package cn.jieyuc.luminovaauth.infrastructure.verification;

import cn.jieyuc.luminova.base.exceptions.BizException;
import cn.jieyuc.luminovaauth.application.verification.SmsSender;
import cn.jieyuc.luminovaauth.domain.verification.VerificationPurpose;

/**
 * 未配置短信供应商时的安全失败 Adapter。
 *
 * <p>项目尚未确定短信供应商，因此该实现明确失败而不是伪造“发送成功”或把验证码输出
 * 到日志。接入真实供应商后，以条件 Bean 替换本实现。</p>
 */
public class UnconfiguredSmsSender implements SmsSender {
    @Override
    public void send(String phone, String code, VerificationPurpose purpose) {
        throw new BizException("SMS_PROVIDER_NOT_CONFIGURED", "短信服务暂不可用");
    }
}
