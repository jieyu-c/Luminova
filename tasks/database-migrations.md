# 数据库迁移

## 用户服务

`luminova-user` 使用 Flyway 管理 PostgreSQL 用户库。服务启动时会先校验并执行
`luminova-business/luminova-user/src/main/resources/db/migration/` 中尚未执行的迁移，
成功后才继续启动。当前 `V1__create_user_identity_tables.sql` 创建用户主体、登录身份、
密码凭证和安全审计表及其索引。

生产部署需为 `luminova-user` 配置可连接目标 PostgreSQL 数据库的 JDBC 地址和账号：

- `POSTGRES_URL`（或 `POSTGRES_HOST`、`POSTGRES_PORT`、`POSTGRES_DB`）
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`

执行迁移的数据库账号需有目标 schema 的建表、建索引和修改权限。成功执行的迁移会记录
在 `flyway_schema_history` 表中。以后每次结构变更都新增一个递增版本，例如
`V2__add_user_locale.sql`；已执行的迁移文件不可编辑或复用版本号。

## 全新数据库

数据库创建完成后直接部署/启动 `luminova-user`。Flyway 会在应用提供服务前执行全部待执行迁移。
PostgreSQL 的 `vector` 扩展由 `docker/postgres/init/01-vector.sql` 在 Docker 数据卷首次
初始化时创建；如果使用外部 PostgreSQL 且需要该扩展，应由数据库管理员预先安装。用户表迁移
由 Flyway 管理，不要再通过手工 SQL 或容器初始化脚本重复创建。

## 已有数据库接入

不要直接对已有库启用一个从空库开始的迁移历史，也不要把 `baseline-on-migrate` 打开作为
通用部署设置。先备份数据库，并将现存表、列类型、默认值、约束和索引与当前 `V1` 逐项核对。

若已有结构与 `V1` 完全一致，由有权限的运维人员使用与应用相同版本的 Flyway 工具对目标库
执行一次性 `baseline`，版本设为 `1`，描述为 `create user identity tables`。此操作只建立
迁移历史基线，不会执行 `V1`。之后正常部署由应用自动执行更高版本的迁移。

若结构不一致，先编写并评审一个针对现状的前置迁移/修复方案，在备份副本演练并完成数据核对，
再建立与目标结构相符的基线。不要仅为让服务启动而基线化不匹配的数据库；Flyway 的校验
无法替代对既有表结构的核对。

## 迁移约束

- 迁移必须可审计、可重复部署；版本化 SQL 不删除数据、不依赖运行时应用代码。
- 对大表采用兼容性扩展、应用部署、数据回填、清理旧结构的分阶段迁移。
- 执行不可事务化或耗时的 PostgreSQL DDL 前，应评估锁表影响和恢复步骤。
- 应用正常运行账号尽量只拥有业务读写权限；迁移可在发布流程中使用受控的迁移账号执行。
- Docker 的 `docker/postgres/init` 仅适用于 PostgreSQL 数据卷首次初始化，不作为持续 schema 迁移机制。
