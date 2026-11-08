package cn.jieyuc.file.application;

import cn.jieyuc.file.domain.BusinessOwnership;
import cn.jieyuc.file.domain.UploadDeclaration;
import cn.jieyuc.file.domain.UploadPurpose;

/** 创建普通上传会话的命令。 */
public record InitiateFileUploadCommand(UploadPurpose purpose, BusinessOwnership ownership,
                                        UploadDeclaration declaration, String idempotencyKey) {
    /** 校验命令必填字段。 */
    public InitiateFileUploadCommand {
        if (purpose == null || ownership == null || declaration == null || idempotencyKey == null
                || idempotencyKey.isBlank() || idempotencyKey.length() > 128) {
            throw new IllegalArgumentException("上传命令不完整");
        }
    }
}
