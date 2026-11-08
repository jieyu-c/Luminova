package cn.jieyuc.file.interfaces.controller;

import cn.jieyuc.file.application.CloudFileAccessApplicationService;
import cn.jieyuc.file.application.CloudFileView;
import cn.jieyuc.file.application.CompleteFileUploadApplicationService;
import cn.jieyuc.file.application.InitiateFileUploadApplicationService;
import cn.jieyuc.file.application.InitiatedFileUpload;
import cn.jieyuc.file.application.port.storage.PresignedAccess;
import cn.jieyuc.file.interfaces.dto.InitiateFileUploadRequest;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 云文件 HTTP 入口，只负责校验和协议转换，不直接访问存储或数据库。 */
@RestController
@RequestMapping("/api/v1/files")
public class CloudFileController {
    private final InitiateFileUploadApplicationService initiateService;
    private final CompleteFileUploadApplicationService completeService;
    private final CloudFileAccessApplicationService accessService;

    public CloudFileController(InitiateFileUploadApplicationService initiateService,
                               CompleteFileUploadApplicationService completeService,
                               CloudFileAccessApplicationService accessService) {
        this.initiateService = initiateService;
        this.completeService = completeService;
        this.accessService = accessService;
    }

    /** 创建普通上传会话并返回预签名 PUT 地址。 */
    @PostMapping("/upload-sessions")
    public ResponseEntity<InitiatedFileUpload> initiate(@Valid @RequestBody InitiateFileUploadRequest request) {
        InitiatedFileUpload result = initiateService.initiate(request.toCommand());
        return ResponseEntity.created(URI.create("/api/v1/files/upload-sessions/" + result.sessionId())).body(result);
    }

    /** 完成上传并返回稳定文件元数据。 */
    @PostMapping("/upload-sessions/{sessionId}/complete")
    public CloudFileView complete(@PathVariable long sessionId) {
        return completeService.complete(sessionId);
    }

    /** 查询经过授权的文件元数据。 */
    @GetMapping("/{fileId}")
    public CloudFileView get(@PathVariable long fileId) {
        return accessService.get(fileId);
    }

    /** 为可用文件签发短时下载地址。 */
    @PostMapping("/{fileId}/access-url")
    public PresignedAccess issueAccess(@PathVariable long fileId) {
        return accessService.issueAccess(fileId);
    }

    /** 软删除文件，物理删除交给治理任务。 */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(@PathVariable long fileId) {
        accessService.delete(fileId);
        return ResponseEntity.noContent().build();
    }
}
