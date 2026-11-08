package cn.jieyuc.file.interfaces.dto;

import cn.jieyuc.file.application.InitiateFileUploadCommand;
import cn.jieyuc.file.domain.BusinessOwnership;
import cn.jieyuc.file.domain.UploadDeclaration;
import cn.jieyuc.file.domain.UploadPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** 创建上传会话的 HTTP 请求。 */
public record InitiateFileUploadRequest(@NotNull UploadPurpose purpose,
                                        @NotBlank @Size(max = 64) String businessContext,
                                        @NotBlank @Size(max = 64) String businessObjectType,
                                        @NotBlank @Size(max = 128) String businessObjectId,
                                        @NotBlank @Size(max = 255) String originalFileName,
                                        @NotBlank @Size(max = 128) String mediaType,
                                        @Positive long size,
                                        @NotBlank @Pattern(regexp = "[0-9a-fA-F]{64}") String sha256,
                                        @NotBlank @Size(max = 128) String idempotencyKey) {
    /** 转换为不含 HTTP 注解的应用命令。 */
    public InitiateFileUploadCommand toCommand() {
        return new InitiateFileUploadCommand(purpose,
                new BusinessOwnership(businessContext, businessObjectType, businessObjectId),
                new UploadDeclaration(originalFileName, mediaType, size, sha256), idempotencyKey);
    }
}
