# P0 已知问题

- 部分受限执行容器无法解析外网且没有 Maven/`gh`；当前正式验收改由指定本地环境完成，GitHub Actions 仅作非阻断辅助回归。
- 本地 MySQL 正式验证依赖专用测试 schema 和 fixture；数据库、表结构或连接配置必须由执行环境明确准备，不得回退生产/开发业务库。
- GitHub 临时 MySQL Service 与本机数据库不共享 schema/data；远程 `mysql-it` 的环境可重复性作为后续 CI 环境治理项，不阻断 P0 本地正式退出。
- `mix` 仅做文件完整性、XML well-formed 和引用存在性验证；目标语义留待 P1-P5。
- 旧 XML/YAML 快照当前锁定资源摘要，不声明两种格式语义等价；完整等价测试属于 P8。
- 迁入测试目录的旧数据库场景仍含固定连接名；默认构建通过 `mysql-it` 标签隔离，P6/P7 再消除运行时硬编码。
- 仓库内 legacy JAR 已通过 bootstrap 固定；未随仓库提供的 `artoria` 仍从远程仓库解析，供应链迁移与校验属于 P8。
