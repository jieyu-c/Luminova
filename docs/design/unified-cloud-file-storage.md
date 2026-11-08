# 统一云文件存储技术设计

## 0. 实现状态（2026-11-08）

已完成首个可编译、可测试的后端增量：

- 新增独立模块 `luminova-business/luminova-file`，遵守 `interfaces -> application -> domain` 依赖方向。
- 完成普通预签名 PUT 上传、HeadObject 完成校验、文件元数据查询、私有访问 URL 和软删除。
- 完成 `CloudFile`、`UploadSession` 状态机及数据库乐观锁持久化。
- 完成 RustFS 的 AWS SDK v2 S3 Client/Presigner Adapter，凭证仅通过外部配置注入。
- 完成文件引用账本、`FileReferenceChanged` 消息 Handler，以及“宽限期 + 无活动引用”物理清理 Worker。
- 完成 Flyway 表结构、领域测试、应用测试和 ArchUnit 分层测试。

当前增量对应 Phase 1，并提前落地部分 Phase 3 治理骨架。以下能力仍在后续增量：

- Multipart 初始化、分片签名、分片登记、完成和中止。
- 画布上下文 RPC 授权 Adapter；当前仅开放 `USER` 自有业务对象，未知上下文默认拒绝。
- RocketMQ Starter 与可靠消费运行配置、业务生产方 Outbox 接入。
- RustFS 真实环境契约测试、调度平台接入、双向对账、指标和告警。

## 1. 设计结论

新增独立业务模块 `luminova-business/luminova-file`，作为“云文件存储”限界上下文和系统唯一上传入口。MVP 通过 AWS SDK for Java v2 的 S3 Client/Presigner 对接 RustFS；RustFS 官方将其定义为 S3 兼容存储，并验证了 Presigned GET/PUT 与核心 Multipart 流程，因此不引入 RustFS 专属 SDK。

公共层只暴露稳定的文件用例和跨上下文契约。供应商差异封装在 `ObjectStoragePort` 的基础设施 Adapter 中。未来接入 MinIO 时可复用 S3 Adapter 配置；阿里云 OSS、火山 TOS 根据兼容性选择 S3 Adapter 或独立 Adapter，不修改领域层。

## 2. 架构边界

### 2.1 限界上下文职责

文件上下文负责：

- 上传会话生命周期与幂等。
- 云文件元数据、业务归属和状态。
- 上传策略、权限协调和访问签名。
- 对象存储操作的统一端口。
- 软删除、过期会话与物理对象清理。
- 提供方能力、健康和错误映射。
- 文件引用账本、孤岛识别、宽限期和双向对账。
- 幂等消费业务模块发出的文件引用变更事件。

文件上下文不负责：

- 业务对象本身的创建和权限规则。
- 图片/视频转码、缩略图、内容审核。
- 用户网盘目录、分享与协作。
- 对象存储集群运维和 Bucket 生命周期配置。

### 2.2 依赖方向

```text
HTTP / RPC
    |
interfaces
    |
application ------> application.port.authorization
    |               application.port.storage
    |               application.port.clock/idempotency
    v
domain
    ^
    |
infrastructure
    |- persistence (MyBatis-Plus + PostgreSQL)
    |- storage/rustfs (AWS SDK v2 S3 Adapter)
    |- session (Sa-Token current user)
    `- scheduling (cleanup worker)
```

`domain` 不依赖 Spring、MyBatis、AWS SDK、RustFS 或 HTTP。Controller 不接触 Mapper、S3 Client 或存储配置。

## 3. 领域模型

### 3.1 UploadSession 聚合

关键字段：

- `UploadSessionId`
- `CloudFileId`
- `UploaderId`
- `BusinessOwnership`
- `UploadPurpose`
- `UploadMode`：`SINGLE_PUT` / `MULTIPART`
- `UploadDeclaration`：文件名、媒体类型、大小、SHA-256
- `StorageLocation`：提供方、Bucket、Object Key（只在内层使用）
- `StorageUploadId`：Multipart 时存在
- `UploadSessionStatus`
- `expiresAt`、`createdAt`、`completedAt`
- `version`

不变量：

- 文件声明创建后不可修改。
- 只有活跃状态可以签发上传 URL。
- Multipart 分片号必须在计划范围内，且同一分片只保留最新登记结果。
- 完成前所有分片必须齐全，完成成功只能发生一次。
- 会话过期后不能恢复，只能新建会话。

### 3.2 CloudFile 聚合

关键字段：

- `CloudFileId`
- `UploaderId`
- `BusinessOwnership`
- `UploadPurpose`
- `OriginalFileName`
- `MediaType`
- `FileSize`
- `Sha256Digest`
- `StorageLocation`
- `CloudFileStatus`
- `createdAt`、`availableAt`、`deletedAt`、`purgedAt`
- `version`

不变量：

- 只有通过完成校验的文件可以进入 `AVAILABLE`。
- `DELETED` 后不再签发 URL或建立新引用。
- `PURGED` 不可恢复。
- 业务归属变更必须通过显式行为并重新授权，不能由通用字段更新完成。

### 3.3 值对象与枚举

- `BusinessOwnership(contextCode, objectType, objectId)`
- `UploadDeclaration(originalFileName, mediaType, size, sha256)`
- `StorageLocation(providerCode, bucket, objectKey)`
- `UploadPurpose`：封闭枚举并携带大小、类型、分片阈值等策略。
- `StorageProviderCode`：MVP 仅 `RUSTFS`，预留 `MINIO`、`ALIYUN_OSS`、`VOLCENGINE_TOS`。
- `StorageCapabilities`：预签名 PUT/GET、Multipart、Checksum、Range、SSE 等能力集合。

## 4. 应用用例

建议使用具体用例服务，避免笼统 `FileService`：

- `InitiateFileUploadApplicationService`
- `IssueUploadPartUrlApplicationService`
- `RegisterUploadedPartApplicationService`
- `CompleteFileUploadApplicationService`
- `AbortFileUploadApplicationService`
- `GetCloudFileAccessApplicationService`
- `DeleteCloudFileApplicationService`
- `CleanupExpiredUploadsApplicationService`
- `PurgeDeletedCloudFilesApplicationService`

创建和完成会话是数据库事务。预签名是无状态远程计算，可在短事务外完成；CompleteMultipart 是远程副作用，采用“条件推进到 `COMPLETING` -> 调用存储 -> Head 校验 -> 新事务确认”的状态机，不在一个数据库事务中假设远程调用与提交原子一致。重试时先 HeadObject 判断对象是否已经完成。

## 5. 存储端口设计

```java
public interface ObjectStoragePort {
    StorageCapabilities capabilities();
    PresignedUpload issueSingleUpload(SingleUploadSpec spec);
    MultipartUpload initializeMultipart(MultipartUploadSpec spec);
    PresignedPartUpload issuePartUpload(PartUploadSpec spec);
    CompletedObject completeMultipart(CompleteMultipartSpec spec);
    void abortMultipart(AbortMultipartSpec spec);
    StoredObjectMetadata inspect(StorageLocation location);
    PresignedAccess issueAccess(AccessSpec spec);
    void delete(StorageLocation location);
}
```

约束：

- 端口使用自有 DTO和值对象，不泄漏 AWS、OSS、TOS SDK 类型。
- 提供方错误统一映射为 `StorageUnavailable`、`StorageAccessDenied`、`StorageObjectMissing`、`StorageConflict` 等应用可理解错误。
- Adapter 配置明确连接超时、请求超时和重试上限；完成 Multipart 和删除保持幂等。
- `StorageProviderRegistry` 按 `StorageProviderCode` 选择 Adapter，但 MVP 配置固定为 RustFS，不允许客户端选择。

## 6. RustFS Adapter

### 6.1 SDK 与连接

- 使用 AWS SDK for Java v2：`s3` 与 `s3-presigner`。
- S3 API Endpoint 指向 RustFS `9000` 端口，不使用 Console `9001`。
- 使用 Signature V4、配置 Region（默认 `us-east-1`）。
- 本地和默认部署采用 path-style access；生产如启用域名再显式配置 virtual-host style。
- Access Key/Secret Key 只从环境变量或密钥管理系统注入，不进入 Nacos 明文、日志和数据库。

### 6.2 Bucket 与对象 Key

MVP 建议一个私有 Bucket，例如 `luminova-private`。对象 Key 由服务端生成：

```text
{environment}/{purpose}/{yyyy}/{MM}/{fileId-prefix}/{fileId}
```

不在 Key 中使用用户输入文件名、手机号或业务标题，避免路径注入、隐私泄漏和重命名成本。原始文件名仅存数据库并在下载响应中使用。

### 6.3 Checksum

- 客户端创建会话时提交十六进制 SHA-256。
- 普通 PUT 签名绑定 `Content-Length`、`Content-Type` 和可支持的 checksum header，完成后通过 HeadObject 校验大小与存储元数据。
- Multipart 每片登记 ETag，并在提供方支持时绑定分片 checksum。
- ETag 不等同于 SHA-256，禁止把 ETag 当完整性哈希。
- 若 RustFS/SDK 组合不能可靠返回整体 SHA-256，MVP 采用“上传时写入声明 checksum 元数据 + 后台抽样/按策略流式复核”；高安全用途在复核完成前进入 `QUARANTINED`。

## 7. HTTP API 草案

统一前缀：`/api/v1/files`。

### 7.1 创建上传会话

`POST /api/v1/files/upload-sessions`

请求：

```json
{
  "purpose": "CANVAS_ASSET",
  "businessContext": "CANVAS",
  "businessObjectType": "CANVAS",
  "businessObjectId": "123456",
  "originalFileName": "scene.png",
  "mediaType": "image/png",
  "size": 1048576,
  "sha256": "...",
  "idempotencyKey": "..."
}
```

响应按 `uploadMode` 返回：

- `SINGLE_PUT`：`uploadUrl`、`requiredHeaders`、`expiresAt`。
- `MULTIPART`：`partSize`、`partCount`，客户端另行请求分片 URL。

### 7.2 获取分片上传地址

`POST /api/v1/files/upload-sessions/{sessionId}/parts/{partNumber}/url`

返回短时 URL 和必需请求头。服务端验证会话归属、状态、过期时间和分片范围。

### 7.3 登记已上传分片

`PUT /api/v1/files/upload-sessions/{sessionId}/parts/{partNumber}`

请求包含 `etag` 和可选 `checksumSha256`。重复登记同一值幂等，不同值按最新上传结果覆盖并记录审计。

### 7.4 查询上传会话

`GET /api/v1/files/upload-sessions/{sessionId}`

返回状态、过期时间和已登记分片，不返回真实 Object Key 与 storage uploadId。

### 7.5 完成或中止

- `POST /api/v1/files/upload-sessions/{sessionId}/complete`
- `POST /api/v1/files/upload-sessions/{sessionId}/abort`

完成成功返回稳定的 `fileId` 和文件元数据。两者均须幂等。

### 7.6 文件查询与访问

- `GET /api/v1/files/{fileId}`：稳定元数据。
- `POST /api/v1/files/{fileId}/access-url`：签发预览/下载 URL。
- `DELETE /api/v1/files/{fileId}`：业务软删除。

错误响应使用统一业务错误码：

- `FILE_POLICY_REJECTED`
- `FILE_UPLOAD_SESSION_EXPIRED`
- `FILE_UPLOAD_STATE_CONFLICT`
- `FILE_CHECKSUM_MISMATCH`
- `FILE_NOT_AVAILABLE`
- `FILE_ACCESS_DENIED`
- `STORAGE_TEMPORARILY_UNAVAILABLE`

## 8. 跨上下文契约与授权

文件上下文不能猜测画布、用户或其他业务对象权限。定义应用端口：

```java
public interface FileBusinessAuthorizationPort {
    void requireUploadPermission(UserId operatorId, BusinessOwnership ownership, UploadPurpose purpose);
    void requireReadPermission(UserId operatorId, BusinessOwnership ownership);
    void requireDeletePermission(UserId operatorId, BusinessOwnership ownership);
}
```

MVP 可由防腐层调用对应业务模块 RPC。为避免任意 `objectType/objectId` 绕过权限，`businessContext + purpose` 必须映射到已注册的授权策略；未知组合直接拒绝。跨模块只传稳定 ID和结果 DTO，不共享聚合、Entity 或 Mapper。

业务模块只保存 `fileId`。展示时先由业务模块完成自身资源授权，再调用文件服务获取元数据/签名地址；文件服务仍执行归属校验，形成纵深防御。

## 9. 数据模型

### 9.1 `cloud_file`

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | 云文件 ID |
| uploader_id | BIGINT | 上传用户主体 ID |
| business_context | VARCHAR(32) | 业务上下文 |
| business_object_type | VARCHAR(32) | 对象类型 |
| business_object_id | VARCHAR(64) | 稳定对象 ID |
| upload_purpose | VARCHAR(32) | 上传用途 |
| original_file_name | VARCHAR(255) | 清洗后的原始文件名 |
| media_type | VARCHAR(127) | 声明媒体类型 |
| file_size | BIGINT | 字节数 |
| sha256 | CHAR(64) | SHA-256 |
| provider_code | VARCHAR(32) | 存储提供方 |
| bucket_name | VARCHAR(63) | 内部 Bucket |
| object_key | VARCHAR(512) | 内部对象 Key |
| status | VARCHAR(24) | 文件状态 |
| created_at / available_at / deleted_at / purged_at | TIMESTAMPTZ | 生命周期时间 |
| version | BIGINT | 乐观锁 |

索引：业务归属索引、上传用户/创建时间索引、待清理状态/删除时间索引。`provider_code + bucket_name + object_key` 建唯一约束。

### 9.2 `file_upload_session`

包含会话 ID、fileId、上传模式、状态、声明字段快照、storage uploadId、过期时间、幂等键、错误码和版本。`uploader_id + idempotency_key` 建唯一约束。

### 9.3 `file_upload_part`

联合主键 `(session_id, part_number)`，记录 ETag、checksum、size 和登记时间。只允许应用通过会话聚合端口写入。

### 9.4 `file_cleanup_task`

记录清理类型、fileId/sessionId、对象定位快照、下次执行时间、尝试次数和最后错误码。任务领取使用状态条件更新或 `SKIP LOCKED` 等基础设施机制；业务层不依赖 SQL 细节。

所有单表读写使用 MyBatis-Plus。复杂任务领取如需 PostgreSQL 特性，应封装在 infrastructure 且先评估是否能通过 Wrapper/乐观锁实现。

### 9.5 `cloud_file_reference`

引用账本是判断文件是否仍被业务使用的事实来源，建议字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | 引用记录 ID |
| file_id | BIGINT | 云文件 ID |
| business_context | VARCHAR(32) | 引用方业务上下文 |
| object_type / object_id | VARCHAR | 引用方业务对象 |
| reference_type | VARCHAR(32) | COVER、ATTACHMENT、ASSET 等 |
| status | VARCHAR(16) | ACTIVE / RELEASED |
| bind_key | VARCHAR(128) | 建立引用的稳定幂等键 |
| source_event_id | VARCHAR(64) | 异步释放时的来源事件 ID |
| bound_at / released_at | TIMESTAMPTZ | 生命周期时间 |

`file_id + business_context + object_type + object_id + reference_type` 建业务唯一约束。绑定与释放使用条件更新，重复事件不得改变已完成结果。`cloud_file` 上可保留派生的 active reference count 用于查询优化，但不能取代引用账本和定期校验。

### 9.6 `outbox_event` 与 `consumed_event`

`outbox_event` 至少包含 `event_id`、`aggregate_type`、`aggregate_id`、`event_type`、`payload`、`status`、`occurred_at`、`published_at`、`attempts`、`next_attempt_at` 和 `last_error`。发布器使用条件领取或 `FOR UPDATE SKIP LOCKED` 批量投递。

`consumed_event` 至少包含 `consumer_name + event_id` 唯一键、处理状态和处理时间。消费者必须在“写入消费幂等记录 + 修改引用账本/文件状态”的同一本地事务中完成处理。

Outbox 的归属规则是“谁产生事实，谁写 Outbox”：

- 画布删除产生 `CanvasDeleted`，由画布服务在删除画布的同一本地事务写入自己的 Outbox。
- 附件替换产生 `FileReferenceReleased`，由对应业务服务写入自己的 Outbox。
- 文件变为可用、删除或物理清理产生的事件，由文件服务写入文件服务自己的 Outbox。

不建设中心化 Outbox 远程服务，否则业务事务与远程写入之间仍存在消息丢失窗口。可抽取 `luminova-outbox-core` 共享组件，统一事件信封、表结构迁移模板、发布器、退避策略、指标与 RocketMQ Adapter，但每个服务保有自己的事件表和本地事务。

## 10. 一致性、幂等与失败恢复

### 创建会话

1. 校验登录用户与业务权限。
2. 校验 UploadPurpose 策略。
3. 生成 fileId、sessionId 和对象定位。
4. 事务内创建 `PENDING` 文件与 `INITIATED` 会话。
5. 事务外签发 URL；签发失败不改变文件可用性，客户端可重试。

### 完成上传

1. 条件更新会话到 `COMPLETING`，并发请求只有一个成为执行者。
2. Multipart 调用 Complete；普通上传跳过此步。
3. HeadObject 验证位置、大小和校验信息。
4. 新事务将文件置为 `AVAILABLE`、会话置为 `COMPLETED`。
5. 若第 2 步成功但第 4 步失败，重试通过 HeadObject 识别已完成对象并继续确认。

### 删除

1. 事务内校验业务权限和引用状态，将文件置为 `DELETED` 并写清理任务。
2. Worker 在保留期后调用存储删除。
3. 删除成功或对象已不存在都视为成功，将文件置为 `PURGED`。
4. 临时错误指数退避；超过阈值进入告警队列。

### 引用建立与释放

1. 显式绑定属于需要即时反馈的同步命令：文件服务验证文件 `AVAILABLE`、业务权限和幂等键后写入 `cloud_file_reference`。
2. 业务对象删除或附件替换由业务服务在自身事务内更新业务数据并写 Outbox。
3. Outbox Publisher 将事件至少一次投递到 RocketMQ；发送失败按指数退避重试。
4. 文件服务消费者以 `consumer_name + event_id` 去重，在一个本地事务内释放引用并记录消费结果。
5. 最后一个有效引用释放后，文件进入 `UNREFERENCED` 并设置 `unreferenced_at`；宽限期内允许合法重绑。
6. 宽限期结束且无保留标记时，清理调度器将其推进为 `DELETED`，随后执行既有物理清理流程。

### 消息队列职责边界

数据库是文件元数据、引用关系和事件发布状态的事实来源；RustFS 保存二进制；RocketMQ 只是异步事件传输通道。以下操作保持同步：创建/完成上传、签发访问地址、查询状态、显式绑定。以下操作适合异步：业务删除后的引用释放、文件状态通知、物理清理触发、媒体处理和对账结果通知。MQ 暂时不可用不得阻塞已在本地事务提交的业务操作，Outbox Publisher 恢复后继续投递。

### 双向对账与孤岛治理

对账分两条链路：

1. **数据库 -> 对象存储：** 扫描 `PENDING/AVAILABLE/UNREFERENCED/DELETED` 文件，使用 HeadObject 检查对象是否存在和基础元数据是否一致。缺失对象进入异常状态并告警，禁止继续签发访问 URL。
2. **对象存储 -> 数据库：** 按受管 Prefix 分页列举对象，与 `provider + bucket + object_key` 唯一记录比对。没有数据库记录的对象进入隔离清单，超过安全保留期且再次确认后才删除。

对账任务按游标分片，保存 checkpoint，支持断点续跑；所有修复和清理动作记录审计事件。禁止一次扫描发现异常后立即删除，避免事务延迟、消息积压或暂时性查询错误造成误删。

## 11. 安全设计

- Bucket 默认私有，关闭匿名访问。
- RustFS 账号按 Bucket/Prefix 最小授权，生产禁止 root 凭证。
- 外部 Endpoint 必须使用 TLS；签名 URL 域名必须是客户端可访问的可信域名。
- 预签名 URL 有效期短，并绑定方法、对象、必要请求头和内容长度条件。
- 限制文件名长度、媒体类型、大小、分片数、并发数和请求频率。
- 不在日志、异常、审计、数据库中记录签名 URL、Access Key、Secret Key。
- 防止对象 Key 使用用户输入；防止通过返回头注入恶意文件名。
- 下载默认 `attachment`；明确安全的图片/媒体用途才允许 `inline`。
- 为未来病毒扫描保留 `QUARANTINED` 状态和 `FileAvailable` 事件。

## 12. 配置设计

```yaml
luminova:
  file-storage:
    active-provider: rustfs
    access-url-ttl: 5m
    upload-url-ttl: 15m
    multipart-threshold: 64MB
    multipart-part-size: 16MB
    session-ttl: 24h
    delete-retention: 7d
    max-active-sessions-per-user: 10
    providers:
      rustfs:
        endpoint: ${RUSTFS_ENDPOINT}
        public-endpoint: ${RUSTFS_PUBLIC_ENDPOINT}
        region: ${RUSTFS_REGION:us-east-1}
        bucket: ${RUSTFS_BUCKET:luminova-private}
        path-style-access: true
        access-key: ${RUSTFS_ACCESS_KEY}
        secret-key: ${RUSTFS_SECRET_KEY}
```

`endpoint` 供服务端访问，`public-endpoint` 用于生成客户端可达的签名 URL，二者必须共享相同签名语义。密钥只从环境或 Secret 管理器读取。

## 13. 可观测性

指标建议：

- `file_upload_session_started_total{purpose,mode,provider}`
- `file_upload_completed_total{purpose,mode,provider,result}`
- `file_upload_bytes_total{purpose,provider}`
- `file_storage_request_duration_seconds{operation,provider}`
- `file_upload_active_sessions`
- `file_cleanup_pending_tasks`
- `file_cleanup_retry_total{provider,error}`
- `file_reference_active_total{context,type}`
- `file_unreferenced_total{purpose}`
- `outbox_pending_events{service,event_type}`
- `outbox_publish_retry_total{service,event_type}`
- `file_event_consume_failure_total{consumer,event_type}`
- `file_reconciliation_anomaly_total{direction,type}`

日志关联字段：`requestId`、`operatorId`（脱敏/稳定 ID）、`fileId`、`sessionId`、`providerCode`、`operation`、`result`。禁止输出签名 URL 和凭证。

## 14. 扩展新平台的步骤

1. 新增 `StorageProviderCode` 与配置属性。
2. 实现 `ObjectStoragePort` Adapter，或复用通用 S3 Adapter。
3. 声明并验证 `StorageCapabilities`。
4. 通过统一 Adapter 契约测试：PUT/GET、Multipart、Head、删除、幂等和错误映射。
5. 配置路由后灰度启用；业务 API和数据库主模型不变。

不提前设计跨云智能调度。只有出现多平台路由需求时，再新增 `StoragePlacementPolicy` 领域策略。

## 15. 测试策略

- **领域测试：** 状态转换、过期、分片完整性、删除/清理不变量。
- **应用测试：** 使用 Fake Storage 与 Fake Authorization 验证编排、幂等、并发和远程成功/事务失败恢复。
- **Persistence Adapter：** PostgreSQL/Testcontainers 验证唯一约束、乐观锁、分页和任务领取。
- **RustFS Adapter：** Testcontainers/Compose RustFS 验证预签名 PUT/GET、Multipart、Abort、Head 和 Delete。
- **接口测试：** 参数校验、鉴权、错误码、未知用途、越权业务对象和过期会话。
- **契约测试：** 所有未来 Storage Adapter 必须通过同一套 `ObjectStorageContractTest`。
- **ArchUnit：** domain 不依赖框架；interfaces 不依赖 Mapper/Adapter；application 只依赖端口。

## 16. 分阶段实施建议

### Phase 1：领域骨架与普通上传

模块、领域模型、元数据表、RustFS Adapter、普通预签名 PUT、完成校验、私有访问 URL。

### Phase 2：Multipart 与失败恢复

分片签名、分片登记、完成/中止、断点续传、状态机恢复和幂等测试。

### Phase 3：治理与跨模块接入

引用账本、按服务本地 Outbox、RocketMQ 事件、幂等消费、软删除、清理 Worker、双向对账、指标告警、工作台封面/画布素材首个业务接入、契约文档。

## 17. 架构决策与风险

- **选择数据库作为事实来源：** 上传状态和文件元数据必须跨重启保留；Redis 后续只可用于限流或短时缓存。
- **选择预签名直传：** 降低应用带宽，但要求正确配置 CORS、外部 Endpoint、TLS 和短时凭证。
- **选择 S3 端口而非供应商接口：** RustFS/MinIO 可复用，但 OSS/TOS 的细节仍可能需要独立 Adapter，不能假设完全兼容。
- **不把 ETag 当 SHA-256：** Multipart ETag 通常不是内容哈希，完整性策略必须单独实现。
- **Outbox 不中心化：** 事件必须与产生事实的数据在同一数据库事务提交；共享的是组件和规范，不是远程事务写入服务。
- **MQ 不是事实来源：** RocketMQ 采用至少一次投递，允许重复且必须幂等；任何队列故障都可由数据库中的 Outbox 和消费记录恢复。
- **物理删除采用保守策略：** 引用账本、宽限期、保留标记和二次对账全部通过后才清理对象，以可恢复性优先于即时释放空间。
- **远程完成与数据库提交非原子：** 通过 `COMPLETING` 状态、Head 探测和幂等重试恢复，不使用长数据库事务包裹远程调用。

## 18. 实现前确认项

开始编码前需确认：

1. 模块名采用 `luminova-file`。
2. MVP 默认 Bucket 名及各环境外部访问域名。
3. 默认软删除保留期（当前建议 7 天）。
4. 第一批 UploadPurpose 及各自媒体类型/大小上限。
5. 工作台作为首个接入业务，采用 RPC 授权还是由网关后端聚合调用。

以上确认完成后再进入实现阶段。

## 19. 参考资料

- [RustFS S3 Compatibility Matrix](https://docs.rustfs.com/en/reference/s3-compatibility)：确认预签名 GET/PUT、核心 Multipart 与常用对象操作的兼容范围。
- [RustFS SDKs](https://docs.rustfs.com/en/developer/sdk)：官方建议优先使用 AWS S3 SDK，并说明 MinIO SDK 兼容性。
- [RustFS Python SDK Guide](https://docs.rustfs.com/en/developer/sdk/python)：说明 Signature V4、Region、path-style、预签名和 Multipart 的连接参数；Java Adapter采用同等 S3 语义。
- [RustFS Status Check](https://docs.rustfs.com/en/operations/status-check)：确认 `9000` 为 S3 API、`9001` 为 Console。

## 20. 引用一致性、Outbox 与孤岛文件治理

> 本章已同步到飞书技术文档，详细定义引用账本、按事实产生方归属的本地 Outbox、RocketMQ 至少一次投递、幂等消费、双向对账、隔离与延迟物理清理。

## 21. 设计模式与代码落点

设计模式用于稳定依赖方向和封装变化点，不作为增加抽象层级的目标。MVP 只实现已经存在明确变化或一致性风险的模式。

### 21.1 六边形架构（Ports and Adapters）

**解决问题：** 领域和应用用例不能依赖 RustFS、AWS SDK、MyBatis、RocketMQ 或 HTTP。

**代码落点：**

- 入站端口：`InitiateFileUploadUseCase`、`CompleteFileUploadUseCase`、`BindFileReferenceUseCase`。
- 出站端口：`ObjectStoragePort`、`CloudFileRepository`、`FileBusinessAuthorizationPort`、`DomainEventOutboxPort`。
- 入站 Adapter：REST Controller、RocketMQ Consumer、定时任务。
- 出站 Adapter：`RustFsObjectStorageAdapter`、MyBatis-Plus Repository、RocketMQ Publisher。

依赖只能从 interfaces/infrastructure 指向 application/domain，禁止领域层反向依赖实现类。

### 21.2 Adapter（适配器模式）

**解决问题：** 不同对象存储 SDK 的类型、异常、签名参数和能力不一致。

`RustFsObjectStorageAdapter` 将 AWS S3 SDK 转换为 `ObjectStoragePort` 的自有 DTO 和统一异常。未来增加 `MinioObjectStorageAdapter`、`AliyunOssObjectStorageAdapter` 或 `VolcTosObjectStorageAdapter`，应用用例保持不变。

跨业务权限调用同样使用防腐层 Adapter，把画布、工作台等上下文的 RPC DTO 映射为文件上下文的 `BusinessOwnership`，禁止外部模型进入领域层。

### 21.3 Strategy（策略模式）

**解决问题：** 上传用途、对象 Key、存储位置和清理保留期会随业务或平台变化。

建议策略接口：

```java
public interface UploadPolicy {
    void validate(UploadDeclaration declaration);
    UploadMode chooseMode(UploadDeclaration declaration, StorageCapabilities capabilities);
}

public interface ObjectKeyStrategy {
    String generate(CloudFileId fileId, UploadPurpose purpose, Instant now);
}
```

`UploadPurposePolicyRegistry` 按用途选择策略；MVP 不允许客户端选择策略。未来确有多平台路由需求时再引入 `StoragePlacementPolicy`，当前固定返回 RustFS。

### 21.4 Registry + Factory（注册表与工厂）

**解决问题：** 根据 `StorageProviderCode` 选择 Adapter，并在启动时校验唯一性和能力。

`StorageProviderRegistry` 接收 Spring 注入的 `List<ObjectStoragePort>`，构建不可变映射；`ObjectStoragePortFactory` 只负责按 providerCode 查找，不包含业务路由规则。未知、重复或未启用的 provider 在启动或调用时快速失败。

MVP 只有 RustFS 仍保留 Registry 边界，但不实现复杂抽象工厂层级。

### 21.5 State（状态模式的领域化实现）

**解决问题：** 上传会话和文件存在严格状态转换，并发请求不能任意改状态。

状态转换由聚合行为封装，例如 `session.startCompleting()`、`session.complete()`、`file.markAvailable()`、`file.markUnreferenced()`、`file.markDeleted()`。MVP 使用枚举 + 聚合方法 + 转换表，不为每个状态创建独立 Spring Bean；只有状态行为显著复杂后才升级为状态对象。

非法转换抛出稳定领域错误，Repository 使用版本号或条件更新保证并发安全。

### 21.6 Repository（仓储模式）

**解决问题：** 聚合持久化不能泄漏 MyBatis Entity、Mapper 和 SQL 细节。

领域/应用层定义 `CloudFileRepository`、`UploadSessionRepository`、`FileReferenceRepository`；基础设施层实现领域对象与 Persistence Object 的映射。任务领取、分页对账等专用查询使用独立 Query Repository，避免把复杂查询塞入聚合仓储。

### 21.7 Specification / Policy（规格与规则策略）

**解决问题：** “是否允许访问、是否可绑定、是否可清理”由多项规则共同决定，不能散落在 Controller 和 Worker。

建议封装：

- `FileBindableSpecification`：文件可用、用途匹配、归属合法。
- `FilePurgeEligibilitySpecification`：无有效引用、宽限期结束、无保留标记、状态为 DELETED。
- `UploadDeclarationPolicy`：大小、媒体类型、扩展名、Checksum 要求。

规格只做确定性判断；需要 RPC 或数据库数据时，由应用服务先加载上下文再传入规格，避免领域对象主动访问基础设施。

### 21.8 Application Service / Command（应用服务与命令对象）

**解决问题：** 用例编排、事务边界、权限校验和远程调用不应进入 Controller 或聚合。

每个写用例使用明确命令对象和具体应用服务，例如 `InitiateFileUploadCommand`、`CompleteFileUploadCommand`、`ReleaseFileReferenceCommand`。Controller 只负责协议转换和参数校验；应用服务负责权限、仓储、领域行为、Outbox 及事务编排。

### 21.9 Transactional Outbox（事务发件箱）

**解决问题：** 本地数据库提交成功但 MQ 发送失败时不能丢失事件。

业务数据与 `outbox_event` 在同一本地事务提交；Publisher 异步领取并投递 RocketMQ。该模式归事实产生方使用，不部署中心化远程 Outbox 服务。`luminova-outbox-core` 只复用事件信封、表结构模板、Publisher、重试和指标。

### 21.10 Idempotent Consumer（幂等消费者）

**解决问题：** RocketMQ 至少一次投递会产生重复消息。

Consumer 使用 `consumer_name + event_id` 唯一键。消费幂等记录与引用账本更新在同一本地事务提交；已成功事件直接确认，处理中断事件可安全重试。业务幂等不能只依赖 MQ 的 Message ID。

### 21.11 Observer / Domain Event（观察者与领域事件）

**解决问题：** 文件可用、解绑、删除、清理等后续动作不应与主用例强耦合。

聚合产生 `FileAvailable`、`FileUnreferenced`、`FileDeleted`、`FilePurged` 等领域事件；应用服务将其持久化到 Outbox，外部订阅方异步处理。领域事件表达已经发生的事实，命名使用过去式，不把“请删除对象”这类命令伪装成事件。

### 21.12 Decorator（装饰器模式）

**解决问题：** 指标、Tracing、审计和统一错误映射属于横切能力，不应污染核心 Adapter。

可使用 `ObservedObjectStoragePort` 包装实际 `ObjectStoragePort`，统一记录耗时、结果和 providerCode；禁止记录签名 URL 和凭证。重试仅用于明确可重试、幂等的存储操作，并设置上限；业务状态冲突不得被装饰器盲目重试。

### 21.13 Process Manager（流程管理器，仅用于长流程）

**解决问题：** Multipart 完成、远程对象成功但数据库确认失败等流程跨越多个事务和远程副作用。

`CompleteUploadProcessManager` 根据持久化状态推进 `COMPLETING -> HeadObject 验证 -> COMPLETED`，重启后可继续。MVP 不引入通用 Saga 框架；只有出现跨多个独立服务的补偿链时才评估 Saga。

### 21.14 模式组合示例

一次完成上传的调用链：

```text
REST Controller（Inbound Adapter）
  -> CompleteFileUploadApplicationService（Application Service）
  -> UploadSession / CloudFile（State + Aggregate）
  -> ObjectStoragePort（Port）
  -> RustFsObjectStorageAdapter（Adapter）
  -> Repository（持久化聚合）
  -> DomainEventOutboxPort（Transactional Outbox）
  -> RocketMQ Publisher
  -> Consumer（Idempotent Consumer）
```

### 21.15 MVP 明确不采用的模式

- 不使用全局 Singleton 保存 SDK Client；生命周期交给 Spring 容器和配置管理。
- 不为每个简单状态建立类层级，优先枚举、聚合行为和状态转换测试。
- 不预先建设通用工作流/Saga 平台。
- 不用责任链堆叠固定校验；只有校验步骤需要插件化或动态组合时再引入。
- 不使用事件溯源作为文件事实来源；当前数据库状态 + 审计事件已满足需求。
