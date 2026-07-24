# 项目环境与自动化命令

## Shell 初始化

执行项目命令前需要先加载用户环境变量：

```bash
source ~/.bash_profile
```

为了避免每次手动输入，项目自动化脚本会在内部自动执行该命令。

## YAML Demo 回归测试

统一使用固定脚本执行 YAML 解析相关 demo 测试：

```bash
./scripts/test-yaml-demo.sh
```

默认会依次执行：

- `dec.demo.model.RuleTests`
- `dec.demo.system.OrderTest`
- `dec.demo.declaration.TestOrderBusiness`
- `dec.demo.directory.DirectoryTest`

也可以只执行单个场景：

```bash
./scripts/test-yaml-demo.sh rule
./scripts/test-yaml-demo.sh order
./scripts/test-yaml-demo.sh declaration
./scripts/test-yaml-demo.sh directory
```

## 沙箱授权说明

Codex 执行命令时可能会因为安全沙箱要求人工确认，当前项目主要触发点是：

- Maven 安装模块时写入本地仓库。
- Demo 测试连接本机 MySQL：`127.0.0.1:3306`。

为减少自动化任务被频繁打断，后续优先执行固定脚本：

```bash
./scripts/test-yaml-demo.sh
```

对该脚本授权一次后，后续自动化回归可以复用稳定命令入口，避免因为 Maven 参数或 mainClass 变化重复确认。
