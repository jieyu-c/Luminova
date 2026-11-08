# Luminova Messaging SDK 需求与架构设计

## 1. 背景

Luminova 的文件、画布、通知等服务需要通过异步事件协作。如果业务服务直接依赖 RocketMQ Client，会把厂商消息类型、重试语义、Topic 配置和升级成本扩散到所有模块，也会导致每个服务重复实现 Outbox、消费幂等和可观测性。

因此建设统一的 Luminova Messaging SDK。SDK 以稳定业务接口隔离具体消息中间件，首版支持 RocketMQ 5.x，未来可以增加 Kafka、RabbitMQ 等 Adapter。

## 2. 产品目标

- 所有业务服务通过统一接口发布和消费消息。
- 业务模块不依赖任何厂商 Producer、Consumer 或消息对象。
- 统一事件信封、事件版本、逻辑 Topic、异常、Trace 和指标。
- 通过 Transactional Outbox 保证已提交的业务事件最终可恢复发布。
- 通过本地事务幂等保证重复投递不会重复提交数据库业务结果。
- 新增或更换消息平台不改变业务事件和业务 Handler。

## 3. 非目标

- 不建设中心化 MQ 网关或中心化 Outbox 服务。
- 不承诺 exactly-once，采用至少一次投递和业务幂等。
- SDK 不定义文件、画布等业务事件。
- MVP 不提供 Saga、工作流编排、事件溯源和动态创建 Topic。
- MVP 不支持单个服务按单条消息动态切换 MQ Provider。

## 4. 用户与使用场景

### 4.1 消息生产方

业务服务在业务事实发生后创建集成事件。一般通知可以直接发布；必须与本地数据库状态一致的事件在业务事务中追加 Outbox。

### 4.2 消息消费方

消费者分散在各业务服务中。SDK 创建底层 Consumer 并分发到业务 Handler；要求幂等的 Handler 由可靠性模块在同一本地事务内写入消费记录并执行业务更新。

### 4.3 运维与开发人员

通过配置完成逻辑 Topic、消费组和物理资源映射，通过指标观察发布失败、Outbox 积压、重复消费和 Handler 失败。

## 5. 功能需求

### 5.1 统一发布

- 提供同步和异步发布接口。
- 使用逻辑目的地，不在事件中保存物理 Topic。
- 支持普通消息，并为 FIFO、延迟消息保留显式模式。
- 平台不支持高级能力时明确失败，禁止静默降级。

### 5.2 统一消费

- Handler 显式声明消费组、逻辑 Topic、事件类型、版本范围、消费模式和幂等要求。
- SDK 启动时检查重复 Handler、缺失映射和不支持能力。
- 只有 Handler 完成并且本地事务提交后才能确认消费成功。
- Handler 异常必须返回消费失败并交由 Broker 重试。

### 5.3 可靠发布

- 业务数据和 Outbox 记录在同一本地事务提交。
- 不存在活动事务时拒绝追加 Outbox。
- Worker 使用租约和乐观锁支持多实例竞争。
- 发布失败按指数退避持续重试，不自动丢弃业务事件。
- Broker 已确认但状态更新前宕机时允许重复发送，事件 ID 保持不变。

### 5.4 幂等消费

- 以 `consumerGroup + eventId` 作为唯一幂等键。
- 消费记录和业务 Handler 在同一本地事务提交或回滚。
- 唯一键冲突表示已经成功处理，可以安全确认。
- 其他数据库异常不能伪装为重复消费成功。

### 5.5 多平台扩展

- 公共层定义 `MessageTransport` SPI 和能力声明。
- RocketMQ 使用官方 5.x gRPC Java Client。
- RabbitMQ Adapter 未来使用 Spring AMQP/官方 AMQP Client。
- Kafka Adapter 未来使用 Kafka Client。
- Outbox 只保存逻辑目的地和统一信封，不保存厂商分区、Delivery Tag 等业务事实。

## 6. 稳定性承诺

- 不保证消息只投递一次。
- 保证与业务事务一起落库的事件不会因 MQ 临时故障永久丢失。
- 保证相同事件在同一消费组内最多提交一次数据库业务结果。
- 外部支付、短信、邮件等副作用仍由业务层使用稳定幂等键或补偿任务保障。
- 失败事件必须可监控、可定位并可人工恢复。

## 7. 模块架构

```text
luminova-messaging-api
        ↑
luminova-messaging-core
        ↑
        ├── luminova-messaging-rocketmq-spring-boot-starter
        └── luminova-messaging-reliability-spring-boot-starter
```

业务服务只编译依赖公共 API；运行时选择具体 Adapter 和可选可靠性模块。

## 8. 设计模式

- 端口与适配器：公共 API 与厂商 Client 隔离。
- 策略模式：Provider、重试、编码和消费执行策略可替换。
- 注册表模式：集中校验和索引 Handler。
- 责任链/管道：统一执行校验、版本、Trace、指标、幂等和 Handler。
- 工厂模式：统一创建信封和厂商消息对象。
- 仓储模式：隔离 MyBatis-Plus Outbox 与消费记录持久化。
- Transactional Outbox：解决数据库提交成功但 MQ 发送失败。
- Idempotent Consumer：解决至少一次投递带来的重复消息。
- 租约模式：实现多实例 Worker 竞争与宕机恢复。
- 防腐层：将厂商异常和回执转换为 Luminova 模型。

## 9. MVP 验收标准

- 公共 API 不依赖 Spring、MyBatis、Jackson 或 RocketMQ。
- 业务模块无需导入 RocketMQ 包即可发布和消费消息。
- 普通消息可通过 RocketMQ 5.x Producer/PushConsumer Adapter 传输。
- FIFO 和延迟模式具有能力声明与 RocketMQ 映射。
- Outbox 无事务调用会失败。
- 多实例通过租约和乐观锁领取 Outbox。
- 重复事件不会重复执行数据库业务副作用。
- 逻辑 Topic 和消费组缺失配置时应用启动失败。
- SDK 模块测试和全仓编译通过。

## 10. 后续阶段

- 接入真实 RocketMQ 5.x + Proxy 的容器集成测试。
- 增加 Micrometer 指标、健康检查和结构化日志。
- 增加 DLQ 查询、告警与人工重放工具。
- 增加 Kafka、RabbitMQ Adapter 契约测试。
- 增加事件 Upcaster 和版本迁移工具。
