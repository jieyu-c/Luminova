# PRD：MVP 用户系统（后端、数据库与 API）

## 1. 概述

Luminova 需要一套最小可用的用户系统，为后续画布、生成任务等业务提供统一身份。MVP 使用“手机号 + 密码”注册和登录，短信验证码用于证明手机号归属以及找回密码；权限仅区分普通用户 `USER` 与管理员 `ADMIN`。

本方案适配当前工程：Java 21、Spring Boot 3.5、PostgreSQL、Redis、Sa-Token、Nacos/Dubbo，以及已有的 `luminova-auth`、`luminova-user`、`luminova-sms` 和 `luminova-gateway` 模块。

设计原则：优先保证身份数据正确、密码不可逆存储、接口不可被低成本爆破；MVP 只实现手机号密码认证，但技术模型和认证流程允许未来增加短信免密、邮箱密码、微信及其他 OAuth/OIDC 登录，而不修改用户主体和会话模型。

### 1.1 已确认范围

- 公开注册。
- 手机号 + 密码登录。
- 短信验证码注册和找回密码。
- `USER` / `ADMIN` 两级角色。
- 本期只包含后端、数据库与 API，不包含前端页面。

### 1.2 前置假设

- 手机号在接口层统一转换为 E.164 格式，例如中国大陆号码存为 `+8613800138000`；首版可以只开放 `+86`，但表结构不绑定国家。
- Redis 是验证码、限流和 Sa-Token 会话的必要运行依赖；PostgreSQL 是用户数据的唯一事实来源。
- 客户端只能通过 Gateway 访问 HTTP API，Dubbo 接口不暴露到公网。
- 所有生产流量使用 HTTPS；服务间网络仅允许可信服务访问。

## 2. 目标

- 用户能通过手机号验证码完成注册，并使用手机号和密码登录。
- 用户能退出当前会话、查看自己的基本资料、修改昵称和修改密码。
- 忘记密码的用户能验证手机号后重置密码。
- 管理员能查询用户并禁用或恢复账号。
- 密码、验证码、登录接口具备 MVP 必需的安全保护。
- 认证状态可被其他业务服务可靠校验，禁用账号后已有会话及时失效。

## 3. 服务职责与调用关系

### 3.1 `luminova-gateway`

- 对外统一入口、路由、请求体大小限制和基础 IP 限流。
- 传递 `X-Request-Id`，不存在时生成；日志中不得记录密码、完整手机号、验证码或 Token。
- 不接受客户端伪造的用户身份请求头；业务服务以 Sa-Token 会话为准。

### 3.2 `luminova-auth`

- 暴露注册、登录、退出、验证码发送、重置密码等公开认证 API。
- 调用用户服务完成账号创建、凭证校验和密码更新。
- 登录成功后通过 Sa-Token 创建服务端会话；会话数据落 Redis。
- 负责认证接口的组合限流、统一模糊错误响应和安全审计事件。
- 通过认证策略注册表选择登录实现；Controller 和会话签发逻辑不感知具体登录方式。

### 3.3 `luminova-user`

- 唯一拥有并修改用户主体、登录身份、密码凭证和角色数据的服务。
- 注册时在一个本地数据库事务内创建账号和资料，避免跨服务分布式事务。
- 通过受信任的 Dubbo 接口向 auth 提供创建账号、验证密码、修改密码等能力。
- 暴露已登录用户资料 API 和管理员用户管理 API。

### 3.4 `luminova-sms`

- 封装短信供应商，不持有用户数据。
- auth 生成验证码并在 Redis 保存摘要后，调用 sms 发送短信。
- 供应商密钥只从环境变量或密钥管理系统读取，不进入 Git/Nacos 明文配置。

### 3.5 推荐调用流程

```text
客户端 -> Gateway -> Auth -> User（Dubbo）-> PostgreSQL
                         |-> SMS
                         |-> Redis（验证码、限流、会话）

客户端 -> Gateway -> User -> PostgreSQL
                    |-> Redis/Sa-Token（校验会话）
```

## 4. 用户故事与验收标准

### US-001：发送注册验证码

**描述：** 作为访客，我希望收到短信验证码，以证明手机号属于我。

**验收标准：**

- [ ] 合法手机号可请求 `REGISTER` 用途验证码。
- [ ] 同一手机号 60 秒内最多发送 1 次，1 小时最多 5 次，24 小时最多 10 次。
- [ ] 同一 IP 1 小时最多请求 20 次；超过限制返回 `429`。
- [ ] 验证码为 6 位随机数字，使用安全随机数生成，有效期 5 分钟。
- [ ] Redis 只保存验证码 HMAC 摘要，不保存明文；验证成功后立即删除。
- [ ] 对“手机号已注册”等情况返回相同的通用响应，避免枚举账号。
- [ ] 单元测试和模块测试通过。

### US-002：手机号注册

**描述：** 作为访客，我希望通过手机号、验证码和密码创建账号。

**验收标准：**

- [ ] 注册必须提交手机号、验证码、密码和 `requestId`。
- [ ] 验证码用途必须为 `REGISTER`，且最多允许连续验证失败 5 次。
- [ ] 手机号数据库唯一；并发注册最多成功一次。
- [ ] 密码通过 BCrypt（cost 12，可配置）哈希后保存，任何表和日志均不保存明文。
- [ ] 新用户默认角色为 `USER`、状态为 `ACTIVE`。
- [ ] 注册成功直接签发登录会话，并返回脱敏用户信息。
- [ ] 同一 `requestId` 的重复请求不会创建多个账号。
- [ ] 数据库集成测试覆盖成功、重复手机号、错误/过期验证码和并发注册。

### US-003：密码登录

**描述：** 作为已注册用户，我希望通过手机号和密码登录。

**验收标准：**

- [ ] 正确凭证且账号为 `ACTIVE` 时创建 Sa-Token 会话。
- [ ] 手机号不存在、密码错误或账号不可用均返回相同的 `手机号或密码错误`，HTTP `401`。
- [ ] 同一账号连续失败 5 次后锁定登录 15 分钟；成功登录后清除失败计数。
- [ ] 同一 IP 每分钟最多 20 次登录尝试，超过限制返回 `429`。
- [ ] 密码校验即使账号不存在也执行一次固定的假哈希校验，降低计时枚举风险。
- [ ] 登录成功更新 `last_login_at`，并写入不含敏感值的审计记录。
- [ ] 单元测试和模块测试通过。

### US-004：会话与退出

**描述：** 作为已登录用户，我希望安全地维持会话并退出。

**验收标准：**

- [ ] Token 仅从 `Authorization: Bearer <token>` 读取，不从 URL、Cookie 或请求体读取。
- [ ] 会话 2 小时无操作过期，最长存活 7 天；超时值可配置。
- [ ] `POST /api/v1/auth/logout` 使当前 Token 立即失效并具备幂等性。
- [ ] MVP 不提供 Refresh Token；Token 过期后客户端重新登录。
- [ ] 账号被禁用或密码被重置时，该用户全部已有会话立即注销。
- [ ] 未登录访问受保护接口返回统一 `401`，权限不足返回 `403`。

### US-005：找回并重置密码

**描述：** 作为忘记密码的用户，我希望验证手机号后设置新密码。

**验收标准：**

- [ ] 可请求 `RESET_PASSWORD` 用途验证码，限流规则与注册验证码一致。
- [ ] 对已注册和未注册手机号均返回相同的通用发送结果。
- [ ] 只有正确且未使用的验证码可以重置密码。
- [ ] 新密码不得与当前密码相同。
- [ ] 重置成功后注销该用户所有会话，并写入审计记录。
- [ ] 重置接口不返回“手机号是否存在”等可枚举信息。

### US-006：查看和更新本人资料

**描述：** 作为已登录用户，我希望查看并修改自己的基础资料。

**验收标准：**

- [ ] 用户可查看 `id`、脱敏手机号、昵称、头像 URL、角色、状态和创建时间。
- [ ] 用户只能修改自己的昵称和头像 URL，不能修改角色、状态和手机号。
- [ ] 昵称去除首尾空格后长度为 1～30 个字符。
- [ ] 头像只允许合法的 `https` URL，最大长度 500；也可为空。
- [ ] 越权访问其他用户资料不可通过修改请求参数实现。

### US-007：登录后修改密码

**描述：** 作为已登录用户，我希望使用当前密码修改密码。

**验收标准：**

- [ ] 必须同时提交当前密码和新密码。
- [ ] 当前密码错误返回通用业务错误，不泄露哈希或内部状态。
- [ ] 新密码符合密码规则且不得与当前密码相同。
- [ ] 修改成功后注销全部会话；客户端需要用新密码重新登录。
- [ ] 写入安全审计事件，但不记录密码内容。

### US-008：管理员管理账号状态

**描述：** 作为管理员，我希望查询、禁用和恢复用户，以处理违规账号。

**验收标准：**

- [ ] 只有 `ADMIN` 可以访问管理员 API，普通用户返回 `403`。
- [ ] 支持按用户 ID、完整手机号精确查询和状态筛选，列表必须分页。
- [ ] 管理员可将用户状态在 `ACTIVE` 与 `DISABLED` 间切换。
- [ ] 禁用用户时立即注销其全部会话。
- [ ] 管理员不能禁用自己；MVP 不提供通过 HTTP 提升角色的接口。
- [ ] 每次状态变更记录操作者、目标用户、时间、请求 ID 和原因。

### US-009：初始化管理员

**描述：** 作为运维人员，我希望安全地创建首个管理员，而不开放提权 API。

**验收标准：**

- [ ] 首个管理员通过一次性运维脚本或部署初始化任务创建。
- [ ] 初始手机号和密码来自部署密钥，不写入仓库或普通配置文件。
- [ ] 初始化操作可重复执行但不会重复创建，也不会覆盖已有密码。
- [ ] 初始化完成后清除一次性密钥。

## 5. 功能需求

- **FR-1：** 系统必须规范化手机号后再做唯一性判断和查询。
- **FR-2：** 验证码必须按 `requestId + phone + purpose` 绑定，不能跨用途复用。
- **FR-3：** 注册必须原子地创建账号和资料；任一步失败不得产生半成品账号。
- **FR-4：** 密码规则为 8～64 个字符，至少包含字母和数字；允许符号和空格，不强制复杂特殊字符组合。
- **FR-5：** 系统不得截断超长密码；超出上限应直接拒绝。
- **FR-6：** 所有认证失败响应不得暴露账号是否存在、是否被禁用或具体密码错误原因。
- **FR-7：** 登录会话必须保存在 Redis，并支持按用户 ID 注销全部会话。
- **FR-8：** 每个受保护接口必须校验登录状态；管理员接口还必须校验 `ADMIN` 角色。
- **FR-9：** 用户角色和状态变更后，旧会话不得继续使用旧权限。
- **FR-10：** 所有写接口校验请求体，并限制请求体大小；非法参数返回稳定错误码。
- **FR-11：** 注册、重置密码和管理员状态修改应支持幂等控制或数据库约束兜底。
- **FR-12：** 安全事件必须携带 `requestId`，便于问题追踪。
- **FR-13：** 登录方式通过稳定的 `loginType` 标识，新增登录方式不得要求修改已有认证策略。
- **FR-14：** 所有登录方式认证成功后必须产出统一的 `AuthenticatedPrincipal`，再进入同一套账号状态检查、会话签发和审计流程。
- **FR-15：** 外部身份（如微信 `openid`）只能通过“提供方 + 外部主体 ID”唯一定位，不能以昵称、手机号等可变资料作为身份主键。
- **FR-16：** 一个用户可以绑定多个登录身份，但一个登录身份最多属于一个用户。

## 6. 数据模型

### 6.1 `user_account`

`user_account` 只代表业务中的“用户主体”，不包含任何具体登录方式的标识或凭证。未来增加微信或邮箱登录时，业务表中的 `user_id` 和 Sa-Token 登录 ID 均不变化。

| 字段 | 类型 | 约束/说明 |
| --- | --- | --- |
| `id` | `bigint` | 主键；雪花 ID 或统一 ID 生成器 |
| `nickname` | `varchar(30)` | 非空；默认生成“用户 + 手机尾号” |
| `avatar_url` | `varchar(500)` | 可空 |
| `role` | `varchar(16)` | `USER` / `ADMIN`；非空，默认 `USER` |
| `status` | `varchar(16)` | `ACTIVE` / `DISABLED`；非空 |
| `last_login_at` | `timestamptz` | 可空 |
| `created_at` | `timestamptz` | 非空 |
| `updated_at` | `timestamptz` | 非空 |
| `version` | `integer` | 乐观锁版本，非空 |

索引与约束：

- `CHECK (role IN ('USER', 'ADMIN'))`。
- `CHECK (status IN ('ACTIVE', 'DISABLED'))`。
- 管理端列表索引 `(status, created_at DESC, id DESC)`。

### 6.2 `user_identity`

表示“用户可以用什么身份被定位”，与用户主体是多对一关系。手机号、邮箱、微信等身份统一建模，但凭证不放在本表。

| 字段 | 类型 | 约束/说明 |
| --- | --- | --- |
| `id` | `bigint` | 主键 |
| `user_id` | `bigint` | 关联 `user_account.id`，非空 |
| `identity_type` | `varchar(32)` | MVP 为 `PHONE`；未来可为 `EMAIL`、`WECHAT`、`OIDC` |
| `provider` | `varchar(64)` | MVP 为 `LOCAL`；外部登录填写提供方，如 `WECHAT_MP` |
| `identifier` | `varchar(255)` | 规范化手机号、邮箱或外部主体 ID；非空 |
| `identifier_masked` | `varchar(255)` | 用于安全展示的脱敏值 |
| `verified_at` | `timestamptz` | 身份完成验证的时间 |
| `created_at` | `timestamptz` | 非空 |
| `updated_at` | `timestamptz` | 非空 |

约束：

- `UNIQUE (identity_type, provider, identifier)`，防止同一登录身份绑定多个用户。
- `INDEX (user_id)`，支持查询用户已绑定的登录方式。
- `identifier` 属于敏感信息，Entity 的 `toString()`、普通日志和管理列表不得直接输出。

### 6.3 `password_credential`

密码凭证独立存放，只有 `luminova-user` 内部凭证仓储可以访问。一个手机号或邮箱身份可各自拥有本地密码；MVP 只为 `PHONE + LOCAL` 创建凭证。

| 字段 | 类型 | 约束/说明 |
| --- | --- | --- |
| `identity_id` | `bigint` | 主键兼外键，关联 `user_identity.id` |
| `password_hash` | `varchar(100)` | BCrypt 哈希，非空 |
| `password_changed_at` | `timestamptz` | 非空 |
| `created_at` | `timestamptz` | 非空 |
| `updated_at` | `timestamptz` | 非空 |

第三方登录 Token 不应复用本表；未来确需保存 OAuth Token 时应建立独立加密凭证表，并使用 KMS 管理的密钥加密。

### 6.4 `security_audit_log`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 主键 |
| `event_type` | `varchar(32)` | `LOGIN_SUCCESS`、`LOGIN_FAILURE`、`PASSWORD_RESET`、`PASSWORD_CHANGE`、`USER_DISABLED`、`USER_ENABLED` |
| `actor_user_id` | `bigint` | 操作者，可空 |
| `target_user_id` | `bigint` | 目标用户，可空 |
| `identity_type` | `varchar(32)` | 登录方式对应的身份类型 |
| `identifier_masked` | `varchar(255)` | 只保存脱敏标识，如 `+86138****8000` |
| `result` | `varchar(16)` | `SUCCESS` / `FAILURE` |
| `reason_code` | `varchar(32)` | 机器可读原因，不保存密码或验证码 |
| `ip` | `inet` | 客户端 IP；仅信任 Gateway 清洗后的来源信息 |
| `user_agent` | `varchar(300)` | 截断保存，可空 |
| `request_id` | `varchar(64)` | 链路标识 |
| `created_at` | `timestamptz` | 非空 |

保留建议：180 天后删除或归档。登录失败日志可以异步写入，但数据库异常不得阻断正常登录；管理员变更日志必须与变更可靠落库。

### 6.5 Redis 键设计

- `auth:otp:{purpose}:{requestId}`：验证码 HMAC、手机号、失败次数；TTL 5 分钟。
- `auth:otp:send:phone:{phoneHmac}:{window}`：手机号发送限流。
- `auth:otp:send:ip:{ip}:{window}`：IP 发送限流。
- `auth:login:fail:{loginType}:{identifierHmac}`：身份登录失败计数；TTL 15 分钟。
- `auth:login:ip:{ip}`：IP 登录限流。
- `auth:idempotency:register:{requestId}`：注册幂等结果；TTL 24 小时。
- Sa-Token 自有会话键：使用独立前缀并配置 Redis 持久化策略。

手机号用于 Redis key 时应先规范化再做 HMAC，避免在 Redis 运维界面直接暴露完整手机号。

## 7. HTTP API 契约

统一响应沿用项目 `BaseResponse`；建议错误结构至少包含 `code`、`message`、`requestId`。时间使用 ISO-8601 UTC。以下 API 均以 `/api/v1` 为前缀。

### 7.1 认证 API（`luminova-auth`）

| 方法与路径 | 鉴权 | 请求核心字段 | 成功结果 |
| --- | --- | --- | --- |
| `POST /auth/verification-codes` | 无 | `phone`, `purpose` | `requestId`, `expiresInSeconds`, `retryAfterSeconds` |
| `POST /auth/register` | 无 | `requestId`, `phone`, `code`, `password` | `token`, `expiresInSeconds`, `user` |
| `POST /auth/login/password` | 无 | `phone`, `password` | `token`, `expiresInSeconds`, `user` |
| `POST /auth/logout` | 可选登录态 | 无 | 空；重复调用仍成功 |
| `POST /auth/password/reset` | 无 | `requestId`, `phone`, `code`, `newPassword` | 空 |

`purpose` 只允许 `REGISTER`、`RESET_PASSWORD`。验证码发送接口无论手机号是否存在都返回 `202 Accepted`；开发和测试环境可使用可观察的假短信适配器，但生产响应绝不返回验证码。

未来登录方式使用独立且强类型的入口，例如 `/auth/login/sms`、`/auth/login/email-password`、`/auth/login/wechat/callback`。这些入口只负责将各自不同的请求转换为统一认证命令，后续流程复用；不设计一个包含任意 `Map<String, Object>` 的万能请求 DTO。

### 7.2 用户 API（`luminova-user`）

| 方法与路径 | 鉴权 | 请求核心字段 | 成功结果 |
| --- | --- | --- | --- |
| `GET /users/me` | 登录 | 无 | 当前用户资料 |
| `PATCH /users/me` | 登录 | `nickname`, `avatarUrl`（至少一项） | 更新后的资料 |
| `POST /users/me/password/change` | 登录 | `currentPassword`, `newPassword` | 空，并注销全部会话 |

### 7.3 管理员 API（`luminova-user`）

| 方法与路径 | 鉴权 | 请求核心字段 | 成功结果 |
| --- | --- | --- | --- |
| `GET /admin/users` | `ADMIN` | `page`, `size`, `status`, `userId`, `phone` | 分页用户列表 |
| `PATCH /admin/users/{userId}/status` | `ADMIN` | `status`, `reason` | 更新后的用户摘要 |

分页 `size` 默认 20，最大 100。管理员列表只返回脱敏手机号，除非未来有明确、审计过的客服业务需要展示完整手机号。

### 7.4 建议错误码

| HTTP | 业务码 | 场景 |
| --- | --- | --- |
| `400` | `INVALID_ARGUMENT` | 参数格式或密码规则不合法 |
| `400` | `VERIFICATION_CODE_INVALID` | 验证码错误、过期或已使用，统一文案 |
| `401` | `INVALID_CREDENTIALS` | 登录失败，统一文案 |
| `401` | `UNAUTHORIZED` | 未登录或 Token 无效 |
| `403` | `FORBIDDEN` | 已登录但角色不足 |
| `409` | `PHONE_ALREADY_REGISTERED` | 注册最终提交发生唯一键冲突 |
| `409` | `PASSWORD_REUSED` | 新旧密码相同 |
| `429` | `RATE_LIMITED` | 触发验证码或登录限流，并返回 `Retry-After` |

注意：验证码“发送”阶段不得返回 `PHONE_ALREADY_REGISTERED`；注册提交阶段可以返回冲突，因为调用者已经持有该手机号收到的有效验证码。

## 8. 可扩展登录技术设计

### 8.1 核心抽象

登录流程分为两个阶段：认证器证明“请求者控制某个登录身份”，统一认证管线再检查用户状态并创建会话。所有业务服务始终只识别稳定的 `userId`，不识别手机号、微信 `openid` 或其他登录标识。

```java
public enum LoginType {
    PHONE_PASSWORD,
    // 未来新增：PHONE_SMS, EMAIL_PASSWORD, WECHAT, OIDC
}

public interface LoginCommand {
    LoginType loginType();
}

public record PhonePasswordLoginCommand(
        String normalizedPhone,
        char[] password,
        String requestId,
        String clientIp,
        String userAgent
) implements LoginCommand {
    @Override public LoginType loginType() {
        return LoginType.PHONE_PASSWORD;
    }
}

public record AuthenticatedPrincipal(
        long userId,
        String role,
        String status,
        long identityId
) {}

public interface LoginAuthenticator<C extends LoginCommand> {
    LoginType supports();
    AuthenticatedPrincipal authenticate(C command);
}
```

安全注意：示例使用 `char[]` 表达密码不应成为普通字符串字段或进入 `toString()`；受 Java/JSON 绑定限制，Controller 接收后仍需立即避免复制、禁止日志输出，并在使用完成后清零可清零的缓冲区。

### 8.2 使用的设计模式

#### 策略模式（Strategy）

- 每种登录方式实现一个 `LoginAuthenticator`。
- MVP 实现 `PhonePasswordAuthenticator`：规范化手机号，调用 user 的凭证验证接口，返回统一主体。
- 未来的 `PhoneSmsAuthenticator` 消费短信验证码；`WechatAuthenticator` 调用微信适配器交换外部身份；它们不负责创建 Sa-Token 会话。
- 登录方式自己的限流、外部协议校验和凭证校验放在对应策略内，共享安全规则放在统一认证管线。

#### 注册表模式（Registry）

Spring 自动注入全部策略，启动时构造只读注册表；认证流程以 `LoginType` 查找策略，不写 `switch` 或多层 `if/else`。

```java
@Component
public final class LoginAuthenticatorRegistry {
    private final Map<LoginType, LoginAuthenticator<?>> authenticators;

    public LoginAuthenticatorRegistry(List<LoginAuthenticator<?>> candidates) {
        this.authenticators = candidates.stream().collect(toUnmodifiableMap(
                LoginAuthenticator::supports,
                identity(),
                (left, right) -> { throw new IllegalStateException("重复登录策略"); }
        ));
    }

    public LoginAuthenticator<?> required(LoginType type) {
        var result = authenticators.get(type);
        if (result == null) throw new UnsupportedLoginTypeException(type);
        return result;
    }
}
```

注册表必须在启动阶段检查重复 `LoginType`，防止不确定地覆盖策略。泛型调用的受控类型转换应集中封装在注册表内部，并通过 `commandType()` 或等价机制做运行时校验，不能散落在业务代码中。

#### 适配器模式（Adapter）

- `SmsSender` 是领域端口，供应商实现如 `AliyunSmsAdapter`、`TencentSmsAdapter`；切换供应商不影响验证码业务。
- 未来定义 `ExternalIdentityProvider`，分别实现微信、Apple 或标准 OIDC 适配器，将不同响应转换为统一 `ExternalIdentity(provider, subject)`。
- 外部提供方的 DTO、错误码和 SDK 类型只能存在于 adapter/infrastructure 包，不得泄漏进领域层。

#### 门面/模板流程（Application Pipeline）

`LoginApplicationService` 是统一登录门面，流程固定为：

1. 校验请求与全局限流。
2. 从注册表取得策略并执行认证。
3. 统一检查账号状态。
4. 统一签发 Sa-Token 会话，登录 ID 永远使用 `userId`。
5. 更新最近登录信息并记录审计。
6. 返回相同结构的 `LoginResult`。

策略只回答“这个身份是否合法且对应哪个用户”，不能直接签发 Token。这样新增登录方式不会绕过账号禁用、审计或会话规则。

### 8.3 DDD 限界上下文与依赖方向

MVP 包含两个限界上下文。限界上下文是业务语言和模型的一致性范围，不简单等同于一个 Controller 或一张表。

#### 账号上下文（Account Context，部署于 `luminova-user`）

拥有用户主体、登录身份、密码凭证、角色和账号状态，是这些数据的唯一写入方。它回答“这个身份属于谁”“密码是否有效”“该账号能否使用”，但不处理 HTTP 登录流程、不生成验证码、不限流，也不签发 Token。

#### 认证上下文（Authentication Context，部署于 `luminova-auth`）

拥有登录流程、认证策略、验证挑战、登录限流和会话生命周期。它回答“本次登录是否可以建立会话”，但不直接访问用户表、不保存密码哈希，也不修改用户资料。

#### 上下文协作

- 认证上下文通过 `AccountAuthenticationFacade` 的 Dubbo 接口调用账号上下文。
- Dubbo DTO 是两个上下文之间的防腐层，只传稳定 ID、状态和必要结果，不共享 Entity、Aggregate 或数据库 DTO。
- 账号上下文不可依赖认证上下文；认证上下文可以依赖共享契约，但不能依赖 `luminova-user` 的实现包。
- 业务上下文只保存 `userId`；需要昵称、头像时通过用户查询接口读取，不能复制登录身份或凭证。

依赖方向固定为：

```text
interfaces -> application -> domain
                    |          ^
                    v          |
              port(interface)  |
                    ^          |
                    |          |
             infrastructure ---+
```

- `domain` 不依赖 Spring、MyBatis-Plus、Sa-Token、Redis、Dubbo 或 HTTP 类型。
- `application` 编排用例和事务，不承载可放入聚合的业务规则。
- `interfaces` 只做协议转换、输入校验和响应映射。
- `infrastructure` 实现仓储、RPC、缓存、短信及框架适配器，可以依赖外部技术。
- 依赖倒置接口放在需要它的一侧：领域所需的抽象放 domain，应用编排所需的外部端口放 application port。

### 8.4 聚合与一致性边界

#### `UserAccount` 聚合根

聚合包含 `UserAccount`、`LoginIdentity` 和本地 `PasswordCredential`，在同一数据库事务内维护：

- 新用户创建时必须同时创建已验证的手机号身份和密码凭证。
- 同一登录身份只能属于一个用户主体，最终由数据库唯一约束兜底。
- 禁用账号后不能通过任何身份登录。
- 角色只能通过受控领域行为改变；MVP 不公开提权接口。
- 不能删除或解绑用户最后一个可用登录身份。

聚合不负责：发送短信、生成验证码、调用 Sa-Token、写 HTTP 响应或控制 Redis TTL。

#### `VerificationChallenge` 聚合根

表示一次验证码挑战，维护用途、过期时间、失败次数、是否已消费等不变量。其仓储实现可以使用 Redis，但领域模型不出现 Redis key 或 Lua 脚本。

聚合行为至少包括：`verify(code, now)` 和 `consume(now)`；基础设施适配器必须保证校验与消费原子执行。它不负责短信供应商调用和用户创建。

#### 会话模块

会话由 Sa-Token 管理，不强行包装成贫血领域实体。应用层只依赖小接口 `SessionIssuer`：

- `issue(userId, role)`：签发属于用户主体的会话。
- `revokeCurrent()`：注销当前会话。
- `revokeAll(userId)`：账号禁用或密码变化后注销全部会话。

Sa-Token 细节全部隐藏在 `SaTokenSessionAdapter` 中。这是一个深模块：调用方只学习三个操作，不感知 Token key、Redis 数据结构和框架静态方法。

### 8.5 关键类逻辑边界

#### 认证上下文

| 类/接口 | 层 | 负责 | 明确不负责 |
| --- | --- | --- | --- |
| `PasswordLoginController` | interfaces | 校验 HTTP DTO、规范化链路信息、构造 `PhonePasswordLoginCommand`、映射响应 | 查询数据库、验证密码、调用 Sa-Token |
| `LoginApplicationService` | application | 执行统一登录管线：限流、选择策略、检查结果、签发会话、审计 | 理解具体密码/OAuth 协议、直接操作 Redis |
| `LoginAuthenticatorRegistry` | application | 注册并按 `LoginType` 返回唯一策略，启动时拒绝重复策略 | 执行认证、创建策略实例、静默选择默认策略 |
| `PhonePasswordAuthenticator` | application | 处理手机号密码登录策略，调用账号上下文认证接口并生成统一主体 | 读取密码哈希、签发 Token、返回 HTTP 响应 |
| `LoginRateLimiter` | application port | 表达登录限流所需的单一能力 | 暴露 Redis key、窗口算法实现细节 |
| `VerificationChallengeService` | application | 创建挑战、调用短信端口、协调原子验证和消费 | 创建用户、签发会话 |
| `VerificationChallenge` | domain | 维护挑战用途、有效期、失败次数与消费规则 | 知道 Redis/SMS/Controller |
| `VerificationChallengeRepository` | domain | 保存、查询和原子消费挑战 | 发送短信、记录登录会话 |
| `SmsSender` | application port | 发送指定模板和参数的短信 | 生成验证码、决定发送频率 |
| `SessionIssuer` | application port | 签发和撤销用户主体会话 | 校验密码、查询用户资料 |
| `SecurityAuditRecorder` | application port | 记录脱敏、结构化安全事件 | 影响认证成功或失败结果 |
| `SaTokenSessionAdapter` | infrastructure | 将 `SessionIssuer` 操作映射到 Sa-Token | 承载业务状态判断 |
| `RedisLoginRateLimiter` | infrastructure | 实现限流原子计数、TTL 和 `Retry-After` | 决定哪些业务场景需要限流 |
| `AliyunSmsAdapter` 等 | infrastructure | 转换供应商请求、处理超时和错误映射 | 保存验证码、创建用户 |

#### 账号上下文

| 类/接口 | 层 | 负责 | 明确不负责 |
| --- | --- | --- | --- |
| `UserAccount` | domain aggregate | 维护账号状态、角色、资料和身份集合不变量；提供命名明确的领域行为 | 持久化自己、哈希密码、签发会话 |
| `LoginIdentity` | domain entity | 表达身份类型、提供方、规范化标识及验证状态 | 校验密码、调用第三方登录平台 |
| `PasswordCredential` | domain entity/value | 持有密码哈希和变更时间，禁止泄漏哈希 | 接收 HTTP DTO、选择 BCrypt 参数 |
| `AccountStatus`、`Role`、`IdentityKey` | domain value object | 类型化表达合法值并在构造时维护格式不变量 | 访问仓储或框架配置 |
| `PasswordPolicy` | domain module | 判断原始密码是否满足长度、组成和复用规则 | 执行哈希、读取配置中心 |
| `PasswordHasher` | domain seam | 提供 `hash` 与 `matches`，隐藏具体算法 | 存储凭证、处理登录失败次数 |
| `UserAccountRepository` | domain | 按用户 ID 或身份键加载聚合、保存聚合 | 暴露 MyBatis Wrapper、分页 DTO 或 Entity |
| `RegisterUserApplicationService` | application | 开启事务、验证注册命令、创建聚合并保存、返回稳定结果 | 手写 SQL、发送 HTTP 响应、签发 Token |
| `AccountCredentialApplicationService` | application | 通过身份键加载聚合并验证/修改凭证；对外只返回认证快照 | 返回密码哈希、创建登录会话 |
| `UserProfileApplicationService` | application | 查询和更新本人允许修改的资料 | 修改角色、账号状态或登录身份 |
| `AdminUserApplicationService` | application | 管理员查询及启用/禁用账号，记录原因并触发会话撤销事件/端口 | 允许操作者禁用自己、修改密码 |
| `AccountAuthenticationFacade` | interfaces/rpc | 将 Dubbo DTO 转成应用命令并返回防腐层 DTO | 直接调用 Mapper、返回领域对象 |
| `MybatisUserAccountRepository` | infrastructure | 把聚合与三张持久化表相互转换并实现并发约束错误映射 | 承载注册、角色或禁用规则 |
| `BCryptPasswordHasher` | infrastructure | 使用配置的 BCrypt cost 实现哈希与恒时语义匹配 | 决定密码业务规则 |

类命名不得使用含义模糊的 `UserService`、`AuthService`、`CommonUtil` 或 `Manager`。应用服务按用例命名，领域对象按业务语言命名，适配器按技术与所实现接口命名。

### 8.6 应用用例边界

每个写用例只有一个应用服务入口和一个事务边界：

- **注册**：auth 验证并消费短信挑战 → 调用 `RegisterUserApplicationService` → user 在本地事务中创建主体、身份和凭证 → auth 在成功后签发会话。
- **密码登录**：auth 限流 → `PhonePasswordAuthenticator` 调用 `AccountCredentialApplicationService.authenticatePhonePassword` → auth 统一签发会话。
- **重置密码**：auth 消费重置挑战 → user 在事务中变更凭证 → 提交成功后撤销全部会话。
- **禁用账号**：user 在事务中变更账号状态并写可靠审计/事件记录 → 提交成功后撤销全部会话。

不得让 Controller 开启事务；不得让一个应用服务通过 HTTP/RPC 调用另一个应用服务来复用几行逻辑。可复用业务规则下沉到聚合或领域模块，可复用外部能力放到端口之后。

### 8.7 建议包结构

```text
luminova-auth
└── cn.jieyuc.auth
    ├── interfaces.controller
    │   ├── PasswordLoginController
    │   └── dto
    ├── application.login
    │   ├── LoginApplicationService
    │   ├── LoginCommand
    │   ├── LoginAuthenticator
    │   ├── LoginAuthenticatorRegistry
    │   └── strategy.PhonePasswordAuthenticator
    ├── application.session
    │   └── SessionIssuer
    └── infrastructure
        ├── rpc.UserCredentialClient
        ├── sms.*SmsAdapter
        └── ratelimit.RedisLoginRateLimiter

luminova-user
└── cn.jieyuc.user
    ├── domain.account
    ├── domain.identity
    ├── domain.credential
    ├── application
    ├── interfaces.controller
    └── infrastructure.persistence
```

Controller 不直接操作 Sa-Token、Redis、Mapper 或 Dubbo；策略不依赖 HTTP DTO；Repository 不返回对外响应 DTO。

### 8.8 扩展一种登录方式的标准步骤

以未来增加短信免密登录为例：

1. 在 `LoginType` 添加 `PHONE_SMS`。
2. 新增强类型 `PhoneSmsLoginRequest` 和 `PhoneSmsLoginCommand`。
3. 新增 `PhoneSmsAuthenticator`，复用验证码端口并查询 `PHONE + LOCAL` 身份。
4. 新增 `/auth/login/sms` Controller 入口。
5. 增加该策略的限流、验证码消费和契约测试。

不需要修改 `LoginApplicationService`、会话签发器、用户主体、业务表外键或已有密码策略。若扩展步骤需要修改这些核心组件，说明抽象边界已经泄漏，应在评审中阻止。

### 8.9 身份绑定与账号合并原则

- MVP 不开放绑定/解绑 API，但数据模型允许一位用户拥有多个身份。
- 未来用户在已登录状态下绑定新身份时，必须再次验证当前会话并完成新身份的所有权证明。
- 第三方登录返回相同手机号或邮箱时，不能自动合并账号；必须由用户登录已有账号后显式绑定，防止账号接管。
- 至少保留一个可登录身份；解绑最后一个身份必须被拒绝。
- 账号合并风险高，不属于普通登录策略扩展，应作为独立功能设计和审计。

### 8.10 扩展性与架构测试

- 每个认证策略使用同一份契约测试：成功产出主体、失败不签发会话、禁用账号不能登录、敏感信息不进入异常。
- 注册表测试覆盖未知类型和重复类型启动失败。
- `LoginApplicationService` 使用假的认证策略验证：任何策略成功后都执行相同的状态检查、审计和会话签发。
- 外部提供方适配器使用 WireMock 或供应商 sandbox 测试超时、错误响应、重放和签名校验。
- 架构测试（可使用 ArchUnit）约束 controller 不访问 persistence，domain 不依赖 infrastructure，认证策略不直接依赖 Sa-Token。
- ArchUnit 必须验证 `domain` 不依赖 Spring、MyBatis、Dubbo、Sa-Token、Redis 与 `interfaces` 包。
- ArchUnit 必须验证 Controller 只能调用 application，不能依赖 Repository 或 infrastructure。
- 仓储契约测试必须由内存 Fake 与 PostgreSQL Adapter 共同通过，测试面只跨越 `UserAccountRepository` 接口。

### 8.11 中文注释与代码可读性规范

实现代码统一使用中文 Javadoc 和必要的中文行内注释。注释的目标是让新成员不追踪完整调用链也能理解类的职责、业务规则和风险点；不是逐行翻译 Java 语法。

#### 必须添加注释的位置

- 每个聚合根、实体、值对象、领域模块、应用服务、端口、仓储接口和基础设施适配器必须有类级中文 Javadoc。
- 所有 `public` 方法和跨模块接口方法必须有中文 Javadoc，说明用途、前置条件、结果、主要异常和安全注意事项。
- 聚合行为必须注明它维护的不变量，例如“禁用账号不能登录”“最后一个登录身份不能解绑”。
- 事务方法必须注明事务范围，以及哪些副作用必须在提交后执行。
- Redis Lua、分布式锁、幂等、并发冲突、恒时密码校验等不直观实现必须解释“为什么这样做”和失败时的行为。
- 涉及手机号、密码、验证码、Token 的字段和 DTO 必须注明敏感级别、是否允许记录日志、如何脱敏。
- 第三方适配器必须注明外部错误如何映射、超时/重试规则以及是否可以安全重试。
- 使用框架特性绕过常规依赖方向时，必须说明原因和替代方案，避免后续误删。

#### 不应添加的低价值注释

- 不给简单 getter/setter、显而易见的构造器赋值或普通空值判断添加“获取某字段”“设置某字段”式注释。
- 不保留已失效、与代码矛盾或被注释掉的旧实现。
- 不在注释中写密码、真实手机号、Token、供应商密钥或生产地址。
- 不用注释掩盖含糊命名；若必须解释“这个变量实际是什么”，优先重命名。

#### 类级注释模板

```java
/**
 * 手机号密码认证策略。
 *
 * <p>负责验证本次手机号密码凭证，并将账号上下文返回的结果转换为统一认证主体。
 * 本类不创建登录会话、不直接读取密码哈希，也不处理 HTTP 响应；会话签发和审计由
 * {@link LoginApplicationService} 在统一认证管线中完成。</p>
 *
 * <p>安全约束：异常和日志中不得包含原始密码或完整手机号。</p>
 */
final class PhonePasswordAuthenticator
        implements LoginAuthenticator<PhonePasswordLoginCommand> {
    // ...
}
```

#### 领域方法注释模板

```java
/**
 * 禁用当前用户主体。
 *
 * <p>该行为只修改聚合内的账号状态。全部会话撤销属于聚合外副作用，必须由应用层在
 * 数据库事务成功提交后调用 SessionIssuer 完成，避免事务回滚但会话已经被撤销。</p>
 *
 * @param operatorId 执行禁用操作的管理员用户 ID，不能与当前用户 ID 相同
 * @param reason     禁用原因，去除首尾空格后不能为空
 * @throws CannotDisableSelfException 管理员尝试禁用自己时抛出
 */
public void disable(UserId operatorId, DisableReason reason) {
    // 领域校验与状态变更
}
```

#### 代码评审要求

- 评审者必须同时检查实现与注释；注释不准确视为缺陷，而不是文档小问题。
- 新增类若无法用 3～6 行类级注释明确“负责/不负责”，说明类的逻辑边界可能仍不清晰，应先拆分或重命名。
- 复杂代码优先通过深模块、清晰命名和值对象降低理解成本，再用注释解释隐藏约束和设计原因。
- PR 中修改业务规则时，必须同步修改对应 Javadoc、`GLOSSARY.md` 和 PRD/ADR（如适用）。

## 9. 内部 Dubbo 契约

共享 API 只放接口与不含框架实现的 DTO，禁止把数据库 Entity 或 `password_hash` 返回给 auth。

- `registerUser(phone, rawPassword)`：user 在一个事务内创建用户主体、`PHONE + LOCAL` 身份和密码凭证。
- `verifyPhonePassword(phone, rawPassword)`：返回统一 `AuthenticatedPrincipal` 所需快照；不返回哈希。
- `resetPassword(phone, rawNewPassword)`：user 内部校验复用并更新哈希。
- `changePassword(userId, rawCurrentPassword, rawNewPassword)`：校验旧密码后更新。
- `getAuthSnapshot(userId)`：返回状态与角色，供会话校验或缓存更新。
- `findIdentity(identityType, provider, identifier)`：未来非密码策略定位身份时使用；不得返回任何凭证。

敏感参数不得写入 Dubbo access log、异常消息和链路追踪标签。服务间调用需设置超时，用户服务不可用时返回 `503 SERVICE_UNAVAILABLE`，不得将其伪装成密码错误。

## 10. 安全基线

### 10.1 密码

- 使用 `spring-security-crypto` 的 BCrypt，cost 默认 12并可配置；上线前通过目标机器压测，单次校验建议约 100～300 ms。
- 禁止 MD5、SHA-1、可逆加密或自制加盐方案。
- 密码字段在日志、异常、审计、监控和 DTO `toString()` 中全部排除。
- 生产环境可以配置一个服务端 pepper，但 pepper 必须来自密钥管理系统；MVP 若尚无可靠密钥设施，可暂缓，不能硬编码。

### 10.2 验证码与防刷

- 使用 `SecureRandom`；Redis 保存 `HMAC-SHA-256(serverSecret, phone + purpose + requestId + code)`。
- HMAC secret 来自部署密钥并支持轮换，不写入仓库。
- 限流同时覆盖手机号、IP、用途和时间窗口；返回 `Retry-After`。
- 验证成功采用原子“校验并删除”（Lua 脚本或等价事务），避免并发重复使用。
- 短信供应商调用失败时不创建可用验证码，或立即删除已创建记录。

### 10.3 Token 与授权

- Token 至少包含 128 bit 随机性，由 Sa-Token 生成并保存服务端会话。
- 只接受 Header Bearer Token；生产日志和 APM 必须对 `Authorization` 头脱敏。
- 角色校验在服务端完成，不信任客户端提交的角色或用户 ID。
- 账号状态/角色可以放在短 TTL 会话缓存中，但禁用和变更权限必须主动使全部会话失效。

### 10.4 网络与配置

- 生产环境强制 HTTPS，建议 Gateway 添加 HSTS。
- CORS 只允许实际前端域名、必要方法和 Header，不允许任意 Origin。
- `X-Forwarded-For` 只信任已知 Gateway；外部传入的同名头先清洗。
- 数据库、Redis、Nacos、短信密钥和 HMAC secret 不得使用仓库默认值部署到生产。
- Actuator 只开放必要健康检查，且不直接暴露公网。

### 10.5 数据与隐私

- 日志展示手机号时只保留国家码、前三位和后四位。
- API 不返回 `password_hash`，管理列表默认也不返回完整手机号。
- 数据库账户遵循最小权限；auth 不直接读写用户表，user 不持有短信供应商密钥。
- 数据库备份加密，生产数据不得直接复制到开发环境。

## 11. 非功能需求

- **NFR-1：可用性。** Redis 或 PostgreSQL 不可用时认证请求快速失败并返回 `503`，不得绕过认证。
- **NFR-2：性能。** 除密码哈希和短信供应商耗时外，认证接口服务端 P95 小于 500 ms；短信发送耗时单独监控。
- **NFR-3：一致性。** 注册、密码修改和状态变更必须使用数据库事务；唯一约束是并发安全的最终防线。
- **NFR-4：可观测性。** 记录成功率、失败率、限流次数、短信发送失败率和登录延迟，不记录敏感值。
- **NFR-5：时间。** 所有服务使用 UTC 存储时间，展示层负责时区转换。
- **NFR-6：兼容性。** API 版本固定在 `/api/v1`，新增字段保持向后兼容。

## 12. 非目标（MVP 不做）

- 本期不实现微信、Apple、GitHub 等第三方登录，但技术结构支持后续以策略和适配器接入。
- 本期不实现邮箱登录、验证码免密登录和双因素认证。
- 多角色、多权限点、角色管理页面或动态 RBAC。
- Refresh Token、设备列表、远程下线指定设备。
- 用户自行注销账号、换绑手机号、实名认证。
- 用户名体系、好友/关注关系、组织与租户。
- 风险画像、图形验证码、设备指纹和复杂风控平台；遭遇真实攻击后再按监控数据升级。

## 13. 实施顺序

1. **领域语言与边界**：以 `GLOSSARY.md` 为准建立两个限界上下文，先完成包依赖 ArchUnit 测试骨架和关键类职责清单。
2. **数据与领域模型**：建立用户主体、身份、密码凭证和审计表，完成约束、聚合、值对象、仓储 seam、密码编码器和手机号规范化模块。
3. **认证抽象**：实现登录命令、认证策略、策略注册表、统一登录门面及其契约测试。
4. **内部用户能力**：注册、手机号密码验证、修改/重置密码、身份查询、账号状态和角色查询的 Dubbo 接口。
5. **验证码与短信**：Redis 验证码状态机、原子消费、短信适配器及多维限流。
6. **认证 API**：注册、密码登录、退出、重置密码和 Sa-Token Redis 会话。
7. **用户 API**：本人资料、更新资料、修改密码。
8. **管理员 API**：分页查询、禁用/恢复、管理员初始化。
9. **安全收口**：Gateway 限流、CORS/HTTPS、日志脱敏、统一错误、审计与告警。
10. **可读性检查**：补齐并校对中文 Javadoc，确保职责、事务、安全和并发注释与实现一致。
11. **验证**：单元测试、PostgreSQL/Redis 集成测试、并发注册测试、架构测试以及基础安全测试。

每一步完成后都应通过 `mvn test`；认证关键路径建议使用 Testcontainers 验证真实 PostgreSQL 和 Redis 行为。

## 14. 完成定义

- [ ] 所有 9 个用户故事验收标准通过。
- [ ] 数据库迁移可在空数据库重复部署，且约束、索引正确。
- [ ] 注册、登录、退出、重置密码、查看资料和管理员禁用流程有端到端 API 测试。
- [ ] 并发注册不会产生重复账号；验证码不能被重复消费。
- [ ] 密码、验证码、完整手机号和 Token 未出现在日志、响应或审计表中。
- [ ] 普通用户无法访问管理员接口；禁用账号和密码变更会注销全部会话。
- [ ] 限流规则有自动化测试，响应包含稳定错误码和 `Retry-After`。
- [ ] Maven 测试全部通过，核心认证逻辑具备单元和集成测试。
- [ ] 登录策略注册表、统一认证管线和分层依赖约束测试通过。
- [ ] 账号与认证上下文的依赖方向通过 ArchUnit 自动检查。
- [ ] 关键类均按“负责/不负责”边界实现，Controller、应用服务、聚合和 Adapter 不越层承担逻辑。
- [ ] 所有关键类、公开方法、业务不变量、事务及安全敏感实现均有准确的中文注释。
- [ ] 代码评审不存在简单翻译语法的噪声注释、失效注释或包含敏感数据的注释。

## 15. 成功指标

- 注册成功率（排除验证码错误和重复手机号）≥ 99%。
- 登录接口在正常流量下错误率 < 0.5%，P95 满足性能目标。
- 生产环境无明文密码、验证码或 Token 日志事件。
- 已禁用账号在 30 秒内无法继续调用受保护 API；目标实现为立即失效。
- 验证码重复消费与并发重复注册数量为 0。

## 16. 待确认事项

- 首发是否只开放中国大陆 `+86` 手机号；当前设计兼容其他 E.164 号码。
- 选用哪家短信供应商，以及其模板审核、签名和发送频控限制。
- Sa-Token 当前版本是否升级；项目根配置为 `1.37.0`，实施前应核对所需 API 和安全更新。
- 生产部署是否已有可靠的密钥管理方式，用于短信密钥、验证码 HMAC secret 和可选 password pepper。
- 审计日志具体保留周期需结合隐私政策和实际合规要求确认，本文暂定 180 天。
