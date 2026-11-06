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
5. 注释不能只写在类上。类、公开方法，以及含有业务判断的私有方法，都要有准确的中文注释，说明职责、边界和不变量。细则见「注释」。
6. 方法体内只在复杂逻辑处注释，解释为什么这样写。普通方法调用和容易看懂的分支不要逐行注释，也不得写入密码、Token、完整手机号或密钥。
7. 禁止使用含义模糊的 `UserService`、`AuthService`、`CommonUtil`、`Manager` 等命名；类名必须体现具体用例或领域职责。
8. 新功能必须同时提供相称的领域测试、应用测试、Adapter 集成测试及必要的 ArchUnit 架构测试。
9. 修改现有代码时采用渐进治理：本次涉及的代码必须符合规范，不得无授权重写无关模块。
10. 后端 Java 代码块的 `{` 放在语句行尾，`}` 单独占一行。禁止 `{` 独占一行，也禁止语句和 `}` 写在同一行。该条不适用于前端。
11. 数据库读写使用 MyBatis-Plus。`domain`、`application` 和 `interfaces` 不得依赖 Mapper、Entity 或 Wrapper。仓储实现类名表达业务端口，不以 MyBatis 做前缀。
12. 领域对象、应用视图、跨上下文契约和持久化 Entity 之间的结构转换使用 MapStruct。转换器类名以 `Converter` 结尾。`Mapper` 只表示数据库 Mapper。细则见「类型转换」。
13. 尽量不要使用魔法值。业务错误码、状态、角色、身份类型、审计事件、提供方代号，以及会在多处比较或分支的字面量，必须定义成枚举或具名常量。细则见「魔法值」。

## 魔法值

新增和修改代码时，尽量不要把有业务含义的字符串或数字直接写在调用处。字面量只放在枚举或常量的定义处，调用处引用这个类型。

1. 取值封闭、需要比较或分支时用枚举。例如 `new BizException(BizErrorCode.INVALID_CREDENTIALS)`，不要写 `"INVALID_CREDENTIALS"`。
2. 对外契约保持稳定。响应、审计和持久化使用枚举的 `code()` 或 `name()`，不要在调用处再抄一遍相同字符串。多个枚举常量可以共享同一个对外码，例如登录锁定和验证码频率对外都是 `RATE_LIMITED`，文案由各自常量提供。
3. 同一个取值只定义一次。跨上下文共用的码放在契约模块；只属于一个聚合的取值放在该领域类型上，例如 `AccountStatus.ACTIVE`、`IdentityKey.LOCAL_PROVIDER`。
4. 比较和分支使用枚举或常量。不要写 `equals("ACTIVE")` 或 `switch` 字符串错误码。
5. 修改既有代码时，本次涉及的魔法值一起替换。不要为此重写无关模块。

以下字面量可以保留：

- 只出现一次、不参与业务分支的展示文案或日志说明。
- 测试里故意构造的非法样本，例如超长字符串、无效 IP。样本如果表示已有枚举值，仍使用该枚举。
- 协议或框架已经提供常量时，使用框架常量，不要再自定义一套同义字符串。

## 注释

注释用来解释代码本身看不出来的职责、边界和原因。类注释不能代替方法注释。

1. 类注释说明这个类型负责什么、不负责什么，以及它维护的不变量。
2. 公开方法，以及含有业务判断、事务、并发或安全取舍的私有方法，都要在方法上注释。说明行为、前置条件、结果和可能的业务失败。
3. 方法体内只在复杂逻辑处添加注释，例如安全上故意合并失败原因、乐观锁冲突、提交后才执行的副作用、只在首次写入时设置过期时间。注释解释为什么，不翻译语句。
4. 普通赋值、查询、转换和一眼能看懂的方法调用不添加注释。不要为了凑数量在每行上面写说明。

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

## 数据库访问

后端数据库操作必须使用 MyBatis-Plus。禁止在新增或修改的持久化代码中使用 `JdbcTemplate`、手写注解 SQL 或 XML SQL 完成单表读写。

1. 每张表一个 Mapper，继承 `BaseMapper`。插入、按主键查询、按主键更新使用 `insert`、`selectById`、`updateById`。
2. 条件查询使用 `LambdaQueryWrapper` 或 `Wrappers.lambdaQuery()`，用实体方法引用表达列，不写字符串列名。
3. 分页使用 MyBatis-Plus 的 `Page`，由已配置的分页插件生成 limit。不要手写 `LIMIT` / `OFFSET`。
4. 乐观锁使用实体字段上的 `@Version`，不要在 SQL 中手写 `version = version + 1`。
5. 主键由领域或调用方分配时，实体使用 `@TableId(type = IdType.INPUT)`，并保持全局 `id-type: input`。列名依靠驼峰转下划线，不在每个字段上重复写列名。
6. Mapper、持久化 Entity 和 Wrapper 只允许出现在 infrastructure。领域和用例通过仓储端口读写，Controller 不得注入 Mapper。仓储实现类按端口命名，例如 `UserAccountRepositoryAdapter`，禁止 `MybatisUserAccountRepository` 这类把框架名写进业务类型的名字。
7. 单表 Wrapper 表达不了的技术细节，例如 PostgreSQL `inet` 类型转换，放在 infrastructure 的 TypeHandler 中，不把 SQL 方言写进应用层。
8. 敏感表禁止让 MyBatis-Plus 或 Lombok 生成包含密码、完整标识或 Token 的 `toString()`。

## 类型转换

领域对象、应用视图、跨上下文契约和持久化 Entity 之间的结构转换使用 MapStruct。禁止在用例、仓储或防腐层里逐字段拷贝这类结构。

1. 转换器接口放在 `application` 或 `infrastructure`。`domain` 不依赖 MapStruct，也不出现转换注解。
2. 转换必须走聚合或值对象的构造器。不得为了转换给聚合增加 setter，不得在转换器里补业务默认值或改写不变量。
3. 类名以 `Converter` 结尾，并表达被转换的业务概念，例如 `UserAccountPersistenceConverter`、`UserProfileViewConverter`。禁止以 `Mapper` 结尾，也禁止以 MapStruct 做前缀。`Mapper` 只留给 MyBatis-Plus 的数据库 Mapper，例如 `UserAccountMapper`。
4. MapStruct 的 `@Mapper` 是框架注解，保留在转换器接口上；生成类型跟随 Converter 接口命名。手写组装只用于无法表达成两个类型之间的转换，例如从当前请求取出客户端 IP。
5. 未映射的目标属性必须编译失败。选择哪个身份对外展示、账号是否可以登录等业务判断留在用例或聚合，不放进转换器。

## 交付自检

交付前必须按 `docs/development-standards.md` 的检查清单自检，并在结果中说明验证情况。
