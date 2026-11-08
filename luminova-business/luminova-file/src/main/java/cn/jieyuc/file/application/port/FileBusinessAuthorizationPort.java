package cn.jieyuc.file.application.port;

import cn.jieyuc.file.domain.BusinessOwnership;
import cn.jieyuc.file.domain.UploadPurpose;

/** 跨业务上下文的文件授权端口。 */
public interface FileBusinessAuthorizationPort {
    /** 校验操作者可以向指定业务对象上传。 */
    void requireUploadPermission(long operatorId, BusinessOwnership ownership, UploadPurpose purpose);

    /** 校验操作者可以读取指定业务对象的文件。 */
    void requireReadPermission(long operatorId, BusinessOwnership ownership);

    /** 校验操作者可以删除指定业务对象的文件。 */
    void requireDeletePermission(long operatorId, BusinessOwnership ownership);
}
