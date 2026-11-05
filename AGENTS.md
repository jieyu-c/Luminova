# Luminova 项目开发约束

本文件适用于整个仓库。所有后续需求分析、设计、实现、重构和代码评审都必须遵守，不需要用户重复说明。

## 必读规范

开始工作前必须阅读并遵守：

- [开发规范](docs/development-standards.md)
- [领域词汇表](GLOSSARY.md)

若需求引入或改变领域概念，必须在实现过程中同步更新 `GLOSSARY.md`。若代码与规范冲突，以本文件及开发规范为准；不得为追求进度静默绕过规范。

## 强制要求

1. 后端业务模块采用 DDD 分层：`interfaces -> application -> domain`，`infrastructure` 通过端口适配领域或应用层。
2. `domain` 不得依赖 Spring、MyBatis、Dubbo、Sa-Token、Redis、HTTP DTO 或其他基础设施技术。
3. Controller 只处理协议转换和输入校验；应用服务只编排用例与事务；业务不变量放入聚合、实体、值对象或领域模块；Adapter 不承载业务规则。
4. 跨模块只共享稳定契约，不共享数据库 Entity、领域聚合或框架对象。
5. 新增或修改的关键类、公开方法、业务规则、事务、并发和安全敏感代码必须添加准确的中文注释。
6. 注释重点解释职责、边界、不变量和“为什么”，不得逐行翻译代码，不得包含密码、Token、完整手机号或密钥。
7. 禁止使用含义模糊的 `UserService`、`AuthService`、`CommonUtil`、`Manager` 等命名；类名必须体现具体用例或领域职责。
8. 新功能必须同时提供相称的领域测试、应用测试、Adapter 集成测试及必要的 ArchUnit 架构测试。
9. 修改现有代码时采用渐进治理：本次涉及的代码必须符合规范，不得无授权重写无关模块。

交付前必须按 `docs/development-standards.md` 的检查清单自检，并在结果中说明验证情况。
