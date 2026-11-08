# Luminova Messaging SDK 技术设计

> 实现状态：MVP 已落地公共 API、平台无关核心、RocketMQ 5.x Producer/PushConsumer Adapter、Transactional Outbox、幂等消费、自动装配和迁移模板。高级治理工具与真实 Broker 集成测试列入后续阶段。

## 1. 设计结论

在 `luminova-common` 下建设与消息中间件无关的 Messaging SDK，所有服务通过 Luminova 自有接口发送和消费消息，不直接依赖 RocketMQ、Kafka、RabbitMQ 等平台类型。MVP 默认提供 Apache RocketMQ 5.x gRPC Adapter；部署环境必须启用 RocketMQ Proxy。未来新增 Kafka、RabbitMQ 或其他 Adapter 时，业务 API、事件信封、Outbox 和幂等模型保持不变。

SDK 提供三类能力：

1. 统一消息契约与发布/消费接口。
2. 统一 Transport SPI、能力声明、配置、序列化、Tracing 和指标的 Spring Boot 自动装配。
3. 可选的可靠消息扩展：Transactional Outbox、幂等消费、重试与恢复任务。

文件服务是第一个接入方，但 SDK 不包含文件业务事件、Topic 名称或文件引用规则。

## 2. 目标与非目标

### 2.1 目标

- 业务代码不直接使用任何消息平台的 SDK、Producer、Consumer 或消息对象。
- 统一事件信封、序列化、Topic/Tag 命名、异常、超时、指标和 Trace 传播。
- 提供至少一次投递语义下的 Outbox 可靠发布和数据库幂等消费。
- Broker、Proxy 或网络故障不导致已经提交的业务事件永久丢失。
- 允许重复消息，但确保接入可靠消费组件后业务副作用只提交一次。
- SDK 升级、消息平台替换或新增 Adapter 不改变业务事件定义。

### 2.2 非目标

- 不建设中心化 MQ 网关或中心化 Outbox 服务。
- 不在 SDK 中定义业务 Topic、业务 Tag、业务事件或消费顺序。
- 不保证 exactly-once；采用“至少一次投递 + 业务幂等”。
- MVP 不抽象各平台原生事务消息；本地事务一致性统一采用 Outbox。
- MVP 不提供通用 Saga、工作流编排、CEP 或事件溯源。
- 不允许业务通过 SDK 动态创建 Topic 和 Consumer Group；资源由部署流程管理。

## 3. 模块拆分

在 `luminova-common` 聚合模块中新增：

```text
luminova-common/
├── luminova-messaging-api
├── luminova-messaging-core
├── luminova-messaging-reliability-spring-boot-starter
└── luminova-messaging-rocketmq-spring-boot-starter

future adapters/
├── luminova-messaging-kafka-spring-boot-starter
└── luminova-messaging-rabbitmq-spring-boot-starter
```

### 3.1 `luminova-messaging-api`

纯 Java 稳定契约，不依赖 Spring、RocketMQ、MyBatis 或具体 JSON 框架：

- `MessageEnvelope<T>`
- `IntegrationEvent`
- `MessagePublisher`
- `MessageHandler<T>`
- `MessageHandlerDescriptor`
- `MessageContext`
- `MessagePublishResult`
- SDK 公共异常和值对象

业务模块只需要编译期事件契约或 Fake 测试时可仅依赖此模块。

### 3.2 `luminova-messaging-core`

提供平台无关的 Envelope 校验、Codec Registry、Handler Registry、Tracing/MDC 传播、指标接口和 Transport 能力校验，不依赖任何具体消息客户端。

### 3.3 `luminova-messaging-rocketmq-spring-boot-starter`

依赖 RocketMQ 5.x Java Client，负责：

- Producer 与 PushConsumer 生命周期。
- Handler 注册、订阅聚合和消息分发。
- JSON Codec、Envelope 校验、Trace/MDC 传播。
- SDK 级超时、发送结果和错误映射。
- Micrometer 指标、健康检查和结构化日志。
- 普通消息、FIFO 消息和延迟消息的统一发布。

该模块不依赖数据库，不提供 Outbox 和消费去重。

### 3.4 `luminova-messaging-reliability-spring-boot-starter`

可选依赖，面向有数据库事务的服务，负责：

- `mq_outbox_event` 的追加、领取、投递和状态推进。
- `mq_consumed_event` 的幂等消费。
- MyBatis-Plus Persistence Adapter。
- `TransactionTemplate` 事务包装。
- Outbox 积压恢复、租约超时回收和清理归档。

文件、画布等需要可靠事件的服务依赖该模块；只消费非关键通知的服务可以只使用基础 Starter。

## 4. 依赖方向

```text
Business Service
  ├── depends on luminova-messaging-api
  └── runtime selects starters

luminova-messaging-core
  └── depends on luminova-messaging-api

luminova-messaging-rocketmq-spring-boot-starter
  ├── depends on luminova-messaging-core
  └── depends on Apache RocketMQ Java Client

luminova-messaging-reliability-spring-boot-starter
  ├── depends on luminova-messaging-api
  └── depends on datasource + MyBatis-Plus
```

业务模块不得导入具体消息平台包。ArchUnit 增加规则：只有对应 Transport Adapter 模块的 infrastructure 包允许依赖厂商 Client。

## 5. 公共消息模型

### 5.1 事件接口

```java
public interface IntegrationEvent {
    String eventType();
    int schemaVersion();
}
```

事件类属于产生事实的业务契约模块，例如 `CanvasDeletedEvent` 位于画布对外契约中。事件名称使用过去式，表示已经发生的事实。

### 5.2 统一信封

```java
public record MessageEnvelope<T>(
        String eventId,
        String eventType,
        int schemaVersion,
        String source,
        String subject,
        Instant occurredAt,
        String traceId,
        String orderingKey,
        Map<String, String> attributes,
        T payload) {
}
```

约束：

- `eventId` 由事件产生方生成，跨重试保持不变，是业务幂等键。
- `eventType + schemaVersion` 决定反序列化契约。
- `source` 为服务代码，不使用实例 IP。
- `subject` 为聚合 ID 或稳定业务主体 ID，便于追踪。
- `orderingKey` 只在 FIFO 发布时使用。
- `attributes` 只放路由和诊断元数据，不放密码、Token、密钥或完整个人信息。
- Envelope 与 Payload 序列化后的总大小必须低于配置上限；大对象存入文件存储，消息只传 fileId。

### 5.3 RocketMQ 映射

| Luminova 字段 | RocketMQ 字段 |
|---|---|
| `eventId` | message key + property `x-event-id` |
| `eventType` | tag + property `x-event-type` |
| `schemaVersion` | property `x-schema-version` |
| `source` | property `x-source` |
| `traceId` | property `x-trace-id` |
| `orderingKey` | messageGroup（FIFO） |
| Envelope JSON | body |

Topic 不放在事件对象中，由发布目的地 `MessageDestination` 指定，避免领域事件依赖部署拓扑。

## 6. 业务侧 API

### 6.1 发布接口

```java
public interface MessagePublisher {
    MessagePublishResult publish(
            MessageDestination destination,
            MessageEnvelope<? extends IntegrationEvent> message);

    CompletableFuture<MessagePublishResult> publishAsync(
            MessageDestination destination,
            MessageEnvelope<? extends IntegrationEvent> message);
}
```

`MessageDestination` 包含逻辑 Topic Code、MessageMode（NORMAL/FIFO/DELAY）和可选投递时间。业务代码使用具名 Topic Code 常量，不直接拼接物理 Topic。

同步发布只用于不依赖本地事务的一般消息。与业务数据库状态一致的事件必须调用 `OutboxEventAppender`，禁止在事务中直接调用 `MessagePublisher`。

### 6.2 Handler 接口

```java
public interface MessageHandler<T extends IntegrationEvent> {
    MessageHandlerDescriptor descriptor();
    Class<T> payloadType();
    void handle(MessageContext context, T event);
}
```

`MessageHandlerDescriptor` 明确声明：

- consumerGroupCode
- topicCode
- eventType
- 支持的 schemaVersion 范围
- consumptionMode（CONCURRENT/FIFO）
- idempotencyMode（REQUIRED/DISABLED）

不使用含义不透明的注解参数魔法字符串；可提供简化注解，但最终必须编译为 Descriptor 并在启动时校验冲突。

### 6.3 错误分类

- `RetryableMessageException`：临时数据库、RPC、限流或依赖不可用，返回消费失败，交由 Broker 重试。
- `InvalidMessageException`：Envelope、版本或数据格式不合法；记录安全摘要后返回失败，最终进入 DLQ。
- 其他异常：默认按可重试处理，避免未知异常导致消息被静默确认。

业务 Handler 不捕获异常后返回成功；只有副作用已经提交或明确无需处理时才能正常返回。

## 7. Producer 设计

### 7.1 Producer Registry

SDK 根据集群配置与发布 Topic 集合创建并复用 Producer。Producer 生命周期由 Spring 管理，不允许业务自行创建或关闭。

### 7.2 发布流程

1. 校验 Destination、Envelope、Payload 大小和必填字段。
2. 通过 `MessageCodec` 序列化。
3. 注入 eventId、eventType、version、traceId 等属性。
4. 根据 MessageMode 构建普通、FIFO 或延迟消息。
5. 调用 RocketMQ Producer 并映射 SendReceipt。
6. 记录耗时与结果，不记录完整消息体和敏感属性。

### 7.3 重试语义

RocketMQ Client 内建发送重试仍可能最终失败，也可能在超时后产生重复消息。因此：

- 普通直接发送由调用方接收明确失败结果。
- 可靠业务事件依赖 Outbox 持续重试，而不是无限增加 Client 同步重试次数。
- SDK 默认设置较小的发送重试和严格超时，避免阻塞业务线程。
- 同一个 Outbox 事件的所有发送尝试保持相同 eventId。

## 8. Consumer 设计

### 8.1 Handler Registry

启动时收集所有 Handler，按 `cluster + consumerGroup + topic + mode` 聚合创建 PushConsumer。以下情况启动失败：

- 同一 consumerGroup/topic/eventType/version 出现多个 Handler。
- FIFO 与并发消费配置冲突。
- Handler 声明 REQUIRED 幂等但未启用可靠性 Starter。
- Topic Code 或 Consumer Group Code 未映射到环境物理名称。

### 8.2 消费流程

1. 接收 MessageView 并提取属性。
2. Envelope 大小和基础字段校验。
3. 根据 eventType/version 查找 Handler 与 Codec。
4. 恢复 traceId/MDC，构造 `MessageContext`。
5. 若启用幂等，进入本地消费事务。
6. 执行 Handler。
7. 事务提交后返回 SUCCESS；异常或回滚返回 FAILURE。
8. 清理线程上下文并记录指标。

### 8.3 FIFO 边界

- 只有同一 orderingKey 内需要顺序时才使用 FIFO。
- Handler 必须短事务，不能在 FIFO 消费中执行长耗时文件处理。
- 同一 Key 的失败会阻塞后续消息，因此重试次数和 DLQ 告警必须单独配置。

## 9. Transactional Outbox

### 9.1 业务 API

```java
public interface OutboxEventAppender {
    void append(
            MessageDestination destination,
            MessageEnvelope<? extends IntegrationEvent> message);
}
```

Appender 必须在调用方已经开启的业务事务中执行；若不存在事务，默认拒绝并抛出 `OutboxTransactionRequiredException`，防止开发者误以为具备原子性。

### 9.2 表结构

`mq_outbox_event`：

| 字段 | 说明 |
|---|---|
| `id` | SDK 内部主键 |
| `event_id` | 全局事件 ID，唯一 |
| `topic_code` | 逻辑 Topic Code |
| `message_mode` | NORMAL/FIFO/DELAY |
| `event_type/version` | 契约标识 |
| `message_key` | 查询与诊断 Key |
| `ordering_key` | FIFO Key |
| `deliver_at` | 延迟投递时间 |
| `envelope_json` | 完整信封 JSON |
| `status` | PENDING/CLAIMED/PUBLISHED/FAILED |
| `attempts` | 已尝试次数 |
| `next_attempt_at` | 下次尝试时间 |
| `lease_owner/lease_until` | 多实例领取租约 |
| `last_error_code` | 稳定技术错误码，不存完整敏感异常 |
| `created_at/published_at` | 时间戳 |
| `version` | 乐观锁 |

### 9.3 Publisher Worker

1. 分页查询到期的 PENDING 或租约过期记录。
2. 使用 `id + version + status` 条件更新竞争租约；不依赖数据库方言锁。
3. 租约拥有者调用 `MessagePublisher`。
4. Broker 确认后标记 PUBLISHED；失败按指数退避计算 nextAttemptAt。
5. 超过快速重试阈值后继续低频重试并告警，不自动丢弃业务事件。

若 Broker 已接收但进程在更新 PUBLISHED 前崩溃，事件会重复发送；eventId 不变，由消费者幂等处理。

## 10. 幂等消费

### 10.1 表结构

`mq_consumed_event`：

- `consumer_group`
- `event_id`
- `event_type`
- `consumed_at`
- `payload_digest`

主键为 `(consumer_group, event_id)`。

### 10.2 事务算法

1. SDK 开启本地数据库事务。
2. 尝试插入 `mq_consumed_event`。
3. 唯一键冲突表示已成功处理，直接提交并返回 SUCCESS。
4. 插入成功后执行业务 Handler。
5. Handler 成功，消费记录和业务更新一并提交。
6. Handler 异常，整个事务回滚，返回 FAILURE 等待 Broker 重试。

进程在数据库提交后、向 Broker ACK 前宕机会触发重复投递；第二次消费命中唯一键后安全确认。

## 11. Topic 与 Consumer Group 治理

### 11.1 逻辑名与物理名

代码只使用逻辑名称：

```text
Topic Code: FILE_EVENT
Physical Topic: {environment}.{tenant}.file-event
Consumer Group Code: FILE_REFERENCE_CONSUMER
Physical Group: {environment}.{service}.file-reference-consumer
```

映射由配置完成。生产环境禁止自动创建 Topic/Group，发布前通过 IaC 或运维脚本创建并校验消息类型。

### 11.2 Topic 粒度

- Topic 按领域事件族划分，不为每个事件创建 Topic。
- Tag 使用 eventType，便于 Broker 侧过滤。
- 同一 Topic 内事件的保留期、消息类型和安全等级必须兼容。
- 普通、FIFO、延迟、事务消息在 RocketMQ 5.x 中具有类型约束，资源配置必须匹配实际模式。

## 12. 配置设计

```yaml
luminova:
  messaging:
    rocketmq:
      enabled: true
      endpoints: ${ROCKETMQ_ENDPOINTS}
      namespace: ${ROCKETMQ_NAMESPACE:}
      request-timeout: 3s
      maximum-attempts: 3
      maximum-message-bytes: 4194304
      topics:
        FILE_EVENT: ${MQ_TOPIC_FILE_EVENT}
      consumer-groups:
        FILE_REFERENCE_CONSUMER: ${MQ_GROUP_FILE_REFERENCE}
    reliability:
      enabled: true
      batch-size: 100
      lease-duration: 30s
      initial-backoff: 1s
      maximum-backoff: 30m
```

Access Key/Secret Key 只从环境变量或 Secret 管理系统读取，不写入 Nacos 明文、日志或数据库。

可靠性 Starter 内置迁移模板位于 `classpath:db/messaging/migration`，宿主服务需将该位置加入 Flyway locations，避免与业务模块自己的 `V1` 迁移版本冲突。Outbox Worker 作为 Bean 暴露，由宿主服务纳入统一调度治理并周期调用 `publishBatch()`。

## 13. 自动装配

建议配置类：

- `LuminovaMqAutoConfiguration`
- `RocketMqProducerAutoConfiguration`
- `RocketMqConsumerAutoConfiguration`
- `MqObservabilityAutoConfiguration`
- `MqReliabilityAutoConfiguration`

使用 `@ConditionalOnProperty`、`@ConditionalOnClass` 和 `@ConditionalOnMissingBean`。SDK 提供安全默认值，但生产缺少 endpoints、Topic 或 Group 映射时必须启动失败，不能静默禁用可靠消息。

## 14. 异常与错误码

SDK 对外只暴露自有异常：

- `MessageConfigurationException`
- `MessageSerializationException`
- `MessageTooLargeException`
- `MessagePublishException`
- `MessageConsumptionException`
- `UnsupportedMessageVersionException`
- `OutboxTransactionRequiredException`
- `OutboxPersistenceException`

异常携带稳定 `MqErrorCode`、eventId 和逻辑 destination；不得携带凭证、完整 Payload 或完整 Broker 地址。

## 15. 可观测性

指标：

- `luminova_mq_publish_total{topic,event_type,result}`
- `luminova_mq_publish_duration_seconds{topic,result}`
- `luminova_mq_consume_total{group,topic,event_type,result}`
- `luminova_mq_consume_duration_seconds{group,event_type}`
- `luminova_mq_outbox_pending{service,topic}`
- `luminova_mq_outbox_oldest_age_seconds{service,topic}`
- `luminova_mq_outbox_retry_total{service,topic,error}`
- `luminova_mq_duplicate_consumed_total{group,event_type}`
- `luminova_mq_handler_failure_total{group,event_type,error}`

日志字段：`eventId`、`eventType`、`schemaVersion`、`topicCode`、`consumerGroupCode`、`traceId`、`attempt`、`result`。默认不输出 Payload。

健康检查分为：

- readiness：配置完整、Client 初始化成功。
- liveness：SDK Worker 线程正常。
- 业务告警：Outbox 最老事件年龄、持续发布失败、消费重试和 DLQ 增长。

## 16. 安全设计

- Producer 和 Consumer 使用独立最小权限凭证。
- Topic ACL 按服务发布/订阅方向授权。
- 消息头和 Payload 进行大小、字段和版本校验。
- 禁止在消息中传递密码、Token、对象存储密钥和永久下载地址。
- 对包含个人信息的业务事件进行字段最小化，必要时只传稳定 ID。
- JSON 反序列化只根据 SDK 注册的 eventType 映射到白名单类型，不启用任意多态类型反序列化。

## 17. 事件版本兼容

- 新增可选字段属于兼容变更，保持 schemaVersion。
- 删除字段、改变含义或类型属于不兼容变更，升级 schemaVersion。
- Consumer 声明支持的版本区间；未知版本返回失败并告警，不能用默认值猜测。
- 发布方至少在一个完整消费迁移周期内保留旧版本。
- SDK 提供 `MessageUpcaster` 扩展点，但 MVP 只有实际出现版本迁移时才实现具体 Upcaster。

## 18. 测试策略

### 18.1 `luminova-mq-api`

- Envelope 不变量和大小边界。
- Destination 与 Descriptor 唯一性。
- 事件版本兼容判断。

### 18.2 RocketMQ Adapter

- 使用 RocketMQ 5.x + Proxy 的 Testcontainers/Compose 环境。
- 普通、FIFO、延迟消息发送与消费。
- 超时、Broker/Proxy 暂停、恢复和重复投递。
- Producer/Consumer 启停和应用优雅关闭。

### 18.3 Outbox

- 业务写入与 Outbox 同事务提交/回滚。
- 多实例竞争领取只有一个租约成功。
- Broker 成功后数据库更新失败导致重复发送。
- 租约过期恢复、指数退避和积压告警。

### 18.4 幂等消费

- 同 eventId 并发消费只有一次业务提交。
- Handler 回滚时 consumed 记录一并回滚。
- 数据库提交后 ACK 前宕机的重复消费安全确认。

### 18.5 架构测试

- 业务模块不得依赖 `org.apache.rocketmq..`。
- `luminova-mq-api` 不依赖 Spring、MyBatis、RocketMQ 和 Jackson。
- Starter 不依赖任何业务事件类。

## 19. 文件服务接入方式

文件服务：

1. 同步上传、完成、访问 URL 和显式绑定不依赖 MQ。
2. 文件状态和引用变更在本地事务内调用 `OutboxEventAppender`。
3. 文件服务消费画布等业务发出的删除/解绑事件时使用 REQUIRED 幂等模式。
4. `FileReferenceReleasedEvent` 等事件定义在对应事实产生方的契约模块，不放入 MQ SDK。
5. MQ 暂时不可用时，业务事务正常提交；Outbox Worker 在恢复后补发。

## 20. 分阶段交付

### Phase 1：契约与普通消息

- `luminova-mq-api`
- RocketMQ 5.x Producer/PushConsumer Adapter
- 普通消息、统一 Envelope、Codec、配置和指标
- Fake Publisher 与 Handler 测试支持

### Phase 2：可靠性闭环

- Outbox 表、Appender、Publisher Worker
- 幂等消费表和事务包装
- 租约恢复、退避、告警和集成测试

### Phase 3：高级消息与治理

- FIFO 与延迟消息
- Topic/Group 配置校验工具
- DLQ 查询与人工重放工具
- 事件版本兼容与 Upcaster

文件系统在 Phase 1 固定 API 后开始同步链路实现；其异步保障在 Phase 2 完成后接入。

## 21. 关键决策

- **采用官方 RocketMQ 5.x gRPC Java Client：** 新 API 与云原生协议更适合作为新 SDK 基础，但部署必须启用 Proxy。
- **业务只依赖 Luminova API：** 防止 RocketMQ 类型和升级成本扩散到所有服务。
- **可靠事件使用 Outbox：** 官方 Client 最终发送失败仍需调用方兜底，且重试可能产生重复；Outbox + 幂等消费提供可恢复闭环。
- **不包装 RocketMQ 事务消息作为 MVP 主路径：** Outbox 更符合现有 PostgreSQL 本地事务和 MyBatis-Plus 技术栈，故障可见性也更直接。
- **不承诺 exactly-once：** 网络和 ACK 不确定性决定可能重复，eventId 和本地事务幂等是正确保障。
- **可靠性模块可选：** 避免网关等无数据库服务被迫引入数据源与 MyBatis。

## 22. 实现前确认项

1. 部署环境是否确定为 RocketMQ 5.x，并已启用 gRPC Proxy。
2. SDK Maven Artifact 名称是否采用本文三个模块。
3. PostgreSQL 是否为所有可靠消息生产者/消费者的本地数据库。
4. Topic 和 Consumer Group 由哪套 IaC/部署流程创建。
5. 第一批只支持普通消息 + Outbox，还是同时交付 FIFO/延迟消息。

## 23. 官方参考资料

- [RocketMQ Java Client SDK](https://rocketmq.apache.org/docs/sdk/02java/)
- [RocketMQ 5.x Clients](https://github.com/apache/rocketmq-clients)
- [Sending Retry and Throttling Policy](https://rocketmq.apache.org/docs/featureBehavior/05sendretrypolicy/)
- [Consumption Retry](https://rocketmq.apache.org/docs/featureBehavior/10consumerretrypolicy/)
- [Transaction Message](https://rocketmq.apache.org/docs/featureBehavior/04transactionmessage/)

## 24. 多消息中间件扩展设计

### 24.1 抽象原则

公共接口只覆盖各消息平台都能稳定表达的能力：普通发布、消费、消息 Key、统一信封、Outbox、幂等、重试结果、Tracing 和指标。FIFO、延迟、Broker 过滤、原生事务消息等属于可选能力，不通过模糊语义强行拉平。

RocketMQ、Kafka、RabbitMQ 之间的差异全部封装在 `MessageTransport` Adapter 和部署配置中。业务模块只使用逻辑 Destination、事件类型与版本。

### 24.2 Transport SPI

```java
public interface MessageTransport {
    TransportProvider provider();
    TransportCapabilities capabilities();

    MessagePublishResult publish(
            MessageDestination destination,
            MessageEnvelope<?> message);
}
```

实现类：

- `RocketMqMessageTransport`
- `KafkaMessageTransport`
- `RabbitMqMessageTransport`

Transport Adapter 只负责协议、平台配置、发送结果和错误映射，不决定业务事件是否允许发送。

### 24.3 能力声明

```java
public record TransportCapabilities(
        boolean fifo,
        boolean delayedDelivery,
        boolean scheduledDelivery,
        boolean nativeDeadLetter,
        boolean brokerFiltering,
        boolean transactionalMessage) {
}
```

SDK 在应用启动时校验 Handler、Destination 与当前 Provider 能力；运行时再次防御性校验。业务请求了平台不支持的能力时抛出 `UnsupportedTransportCapabilityException`，禁止静默降级。

### 24.4 平台语义映射

| 统一能力 | RocketMQ | Kafka | RabbitMQ |
|---|---|---|---|
| 普通消息 | Topic | Topic | Exchange + Queue |
| 消息 Key | Keys | Record Key | Message property |
| 局部顺序 | Message Group/FIFO | Partition Key | 单队列/单活消费者策略 |
| 延迟投递 | 原生延迟/定时消息 | 无统一原生语义，需调度层或延迟 Topic | TTL/DLX 或延迟插件 |
| 消费组 | Consumer Group | Consumer Group | Queue + Consumer |
| 死信 | Broker DLQ | 由应用/Connector 建 DLQ Topic | DLX/DLQ |
| 过滤 | Tag/SQL92 | 通常客户端处理或拆 Topic | Routing Key/Exchange |

表格描述的是适配方向，不承诺各平台高级能力完全等价。每个 Adapter 必须在文档和 `TransportCapabilities` 中声明精确限制。

### 24.5 Provider 选择

```yaml
luminova:
  messaging:
    provider: rocketmq
```

`MessageTransportRegistry` 在启动时发现 Adapter，并根据配置选择唯一激活实现。未配置、配置多个实现或所需能力不满足时启动失败。

MVP 不支持同一服务运行时按单条消息动态切换 Provider。未来若出现多平台迁移或双写需求，应新增显式 `TransportRoutingPolicy`，并单独设计双写一致性、回滚和观测方案。

### 24.6 Outbox 与幂等保持平台无关

`mq_outbox_event` 保存逻辑 Topic、消息模式、Envelope 和投递状态，不保存 RocketMQ Message ID、Kafka Partition 或 RabbitMQ Delivery Tag 作为业务事实。平台回执只作为诊断信息保存。

`mq_consumed_event` 继续使用 `consumerGroup + eventId` 去重，因此更换消息平台不会改变消费幂等模型。

### 24.7 Adapter 契约测试

所有 Adapter 必须通过统一 `MessageTransportContractTest`：

- 普通消息发送、接收和属性保持。
- 相同 eventId 重发时 Envelope 不变。
- 超时和不可用错误映射。
- 大小限制和非法 Destination 拒绝。
- 声明支持的 FIFO、延迟、过滤和 DLQ 能力测试。
- 不支持能力必须明确失败。

### 24.8 命名调整

文档和代码统一使用 `Luminova Messaging SDK`。`MQ` 只作为通用行业缩写出现在数据库表或指标兼容名称中；公共 Java 包建议使用 `cn.jieyuc.luminova.messaging`，避免包名绑定 RocketMQ。
