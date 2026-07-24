# P0 已知问题

- 当前执行容器无法解析外网且没有 Maven，Wrapper 无法在本地下载 Maven 发行包；由 GitHub Actions 完成动态验证。
- `mix` 仅做文件完整性、XML well-formed 和引用存在性验证；目标语义留待 P1-P5。
- 旧 XML/YAML 快照当前锁定资源摘要，不声明两种格式语义等价；完整等价测试属于 P8。
- 迁入测试目录的旧数据库场景仍含固定连接名；默认构建通过 `mysql-it` 标签隔离，P6/P7 再消除运行时硬编码。
- 仓库内 legacy JAR 已通过 bootstrap 固定；未随仓库提供的 `artoria` 仍从远程仓库解析，供应链迁移与校验属于 P8。
