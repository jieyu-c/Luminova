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
10. 后端 Java 代码块的 `{` 放在语句行尾，`}` 单独占一行。禁止 `{` 独占一行，也禁止语句和 `}` 写在同一行。该条不适用于前端。

## 代码排版

后端 Java 的编写、修改和评审必须遵守本节。调整逻辑不能作为打破排版的理由。前端代码不套用本节，继续使用 `luminova-frontend` 里已有的写法。

### 左大括号在行尾，右大括号单独成行

本节只约束后端 Java。

以下结构的 `{` 和 `}` 是代码块：类、接口、枚举、记录、方法、构造器、紧凑构造器、lambda / 箭头函数，以及 `if`、`else`、`for`、`while`、`do`、`switch`、`try`、`catch`、`finally`。

1. `{` 留在语句、方法或关键字的同一行末尾，不单独换行。
2. `}` 单独占一行，并与该语句的起始列对齐。
3. 块内即使只有一条语句，也必须换行。禁止 `method() { return x; }`、`if (x) { return; }` 和 `if (x) return;`。
4. `else`、`else if`、`catch`、`finally` 跟在上一块的 `}` 后面，不另起一行；它们自己的 `{` 仍留在该行末尾。
5. `{` 后、`}` 前不插入空行。除右大括号外不要额外拆行，代码保持紧凑。

```java
public void disable(UserId operatorId, DisableReason reason) {
    if (id.equals(operatorId)) {
        throw new CannotDisableSelfException();
    } else {
        status = AccountStatus.DISABLED;
    }
}
```

下面两种都不合格。第一种把 `{` 单独换了行；第二种让 `}` 和语句挤在同一行：

```java
public void disable(UserId operatorId, DisableReason reason)
{
    if (id.equals(operatorId))
    {
        throw new CannotDisableSelfException();
    }
}

public UserId id() { return id; }
```

### 非代码块保持紧凑

数组、注解参数和字符串中的花括号不是代码块。它们保持紧凑，不要拆成单独的大括号行。

### 前端排版

`luminova-frontend` 的 TypeScript、TSX 和 JavaScript 不使用上一节的 Java 大括号规则。保持现有前端写法：两空格缩进，`{` 留在语句行尾；只有一条语句的 `if` 可以不加大括号，例如 `if (!open) return;`。不要把前端文件改成后端的块结构。

## 交付自检

交付前必须按 `docs/development-standards.md` 的检查清单自检，并在结果中说明验证情况。
