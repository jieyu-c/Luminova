package cn.jieyuc.file.infrastructure.authorization;

import cn.jieyuc.file.application.port.FileBusinessAuthorizationPort;
import cn.jieyuc.file.domain.BusinessOwnership;
import cn.jieyuc.file.domain.UploadPurpose;
import org.springframework.stereotype.Component;

/**
 * MVP 自有资源授权 Adapter。
 *
 * <p>仅接受 USER 上下文且业务对象标识等于当前用户。画布等跨上下文资源必须增加 RPC Adapter，
 * 不允许通过客户端声明绕过权限。</p>
 */
@Component
public class SelfOwnedFileAuthorizationAdapter implements FileBusinessAuthorizationPort {
    private static final String USER_CONTEXT = "USER";

    /** 校验当前用户自有资源的上传权限。 */
    @Override
    public void requireUploadPermission(long operatorId, BusinessOwnership ownership, UploadPurpose purpose) {
        requireOwner(operatorId, ownership);
    }

    /** 校验当前用户自有资源的读取权限。 */
    @Override
    public void requireReadPermission(long operatorId, BusinessOwnership ownership) {
        requireOwner(operatorId, ownership);
    }

    /** 校验当前用户自有资源的删除权限。 */
    @Override
    public void requireDeletePermission(long operatorId, BusinessOwnership ownership) {
        requireOwner(operatorId, ownership);
    }

    private void requireOwner(long operatorId, BusinessOwnership ownership) {
        if (!USER_CONTEXT.equals(ownership.contextCode())
                || !Long.toString(operatorId).equals(ownership.objectId())) {
            throw new IllegalArgumentException("当前业务对象尚未接入文件授权");
        }
    }
}
