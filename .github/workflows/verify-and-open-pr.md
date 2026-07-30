# Verify and Open PR：完整使用文档

## 1. 目标

本 Workflow 用于实现以下自动化流程：

```text
从 dev_all 创建功能分支
→ 编写代码
→ commit 并 push
→ 触发 GitHub Actions
→ 核心编译和测试
→ MySQL 集成测试
→ 全部通过
→ 自动创建或复用指向 dev_all 的 Pull Request
```

任意测试失败时，创建 PR 的 Job 不会运行。

## 2. 文件位置

将 Workflow 保存到：

```text
.github/workflows/verify-and-open-pr.yml
```

建议保留现有文件：

```text
.github/workflows/p0-build.yml
```

两个 Workflow 的职责不同：

- `verify-and-open-pr.yml`：PR 创建前验证，验证通过后创建 PR。
- `p0-build.yml`：PR 创建后的最终合并门禁。

## 3. 执行顺序

Workflow 中三个 Job 严格串行执行：

```text
core-verify
    ↓
mysql-it
    ↓
create-pr
```

### core-verify

执行：

```bash
./mvnw --version
scripts/remediation/bootstrap_legacy_dependencies.sh
./mvnw --batch-mode --no-transfer-progress clean verify
scripts/remediation/prove_test_failure_gate.sh
```

并上传：

```text
**/target/surefire-reports/**
**/target/failsafe-reports/**
**/target/site/jacoco/**
```

### mysql-it

启动 MySQL 8.0 服务，创建：

```text
database: demo-test2
user: root
password: mysqldb
port: 3306
```

导入：

```text
dec-demo/src/test/resources/mysql/schema.sql
dec-demo/src/test/resources/mysql/data.sql（存在时）
```

然后执行：

```bash
./mvnw --batch-mode --no-transfer-progress -Pmysql-it verify
```

### create-pr

只有 `mysql-it` 成功后才运行。

它会：

1. 检查源分支和目标分支不能相同。
2. 检查是否已经存在相同源分支到目标分支的开放 PR。
3. 已存在时复用，不重复创建。
4. 不存在时创建新 PR。
5. 在 PR 正文记录测试项目和 Workflow Run 地址。

## 4. 权限模型

Workflow 顶层权限为：

```yaml
permissions:
  contents: read
```

只有 `create-pr` Job 额外获得：

```yaml
permissions:
  contents: read
  pull-requests: write
```

这样核心构建和 MySQL 测试代码没有 PR 写权限，降低错误脚本或恶意脚本滥用令牌的风险。

## 5. 仓库设置

打开：

```text
Repository
→ Settings
→ Actions
→ General
→ Workflow permissions
```

启用：

```text
Allow GitHub Actions to create and approve pull requests
```

否则即使 YAML 设置了 `pull-requests: write`，使用仓库 `GITHUB_TOKEN` 创建 PR 仍可能被拒绝。

## 6. 首次安装注意事项

`workflow_dispatch` 手工触发要求 Workflow 文件存在于仓库默认分支。

当前仓库连接器返回的默认分支是：

```text
master
```

因此有两种选择：

### 方案 A：将 dev_all 改为默认分支

当 `dev_all` 是实际开发主线时，这是更一致的方案。

路径：

```text
Repository
→ Settings
→ General
→ Default branch
→ dev_all
```

### 方案 B：让 Workflow 文件也存在于 master

不修改默认分支时，需要先将：

```text
.github/workflows/verify-and-open-pr.yml
```

合入 `master`，之后才能在 Actions 页面使用手工触发。

自动 `push` 触发不依赖手工按钮，但功能分支自身必须包含该 Workflow 文件。

## 7. 推荐的功能分支名称

自动触发支持：

```text
feature/**
fix/**
refactor/**
chore/**
```

例如：

```text
feature/xml-execution
fix/mysql-transaction
refactor/context-registry
chore/update-test-fixtures
```

## 8. 开发操作

从 `dev_all` 创建分支：

```bash
git switch dev_all
git pull origin dev_all
git switch -c feature/xml-execution
```

完成编码后先本地验证：

```bash
scripts/remediation/bootstrap_legacy_dependencies.sh

./mvnw \
  --batch-mode \
  --no-transfer-progress \
  clean verify
```

提交并推送：

```bash
git status
git add .
git commit -m "feat: implement XML execution"
git push -u origin feature/xml-execution
```

## 9. 自动触发

当分支名称符合以下模式时：

```text
feature/**
fix/**
refactor/**
chore/**
```

每次 `git push` 都会自动触发：

```text
Verify and Open PR
```

测试全部通过后：

- 没有 PR：创建指向 `dev_all` 的 PR。
- 已有 PR：复用现有 PR，不重复创建。
- 测试失败：不创建 PR。
- 同一分支再次 push：取消该分支尚未完成的旧运行，只保留最新运行。

## 10. 手工触发

GitHub 页面：

```text
Repository
→ Actions
→ Verify and Open PR
→ Run workflow
```

选择源分支，例如：

```text
feature/xml-execution
```

输入：

```text
base_branch: dev_all
pr_title: feat: implement XML execution
```

`pr_title` 可留空，系统会生成：

```text
Merge feature/xml-execution into dev_all
```

## 11. 使用 GitHub CLI 触发

在已安装并认证 `gh` 的环境中：

```bash
gh workflow run verify-and-open-pr.yml \
  --repo szlwin/doc-eq-code-Dgremlin \
  --ref feature/xml-execution \
  -f base_branch=dev_all \
  -f pr_title="feat: implement XML execution"
```

查看运行：

```bash
gh run list \
  --repo szlwin/doc-eq-code-Dgremlin \
  --workflow verify-and-open-pr.yml
```

跟踪运行：

```bash
gh run watch \
  --repo szlwin/doc-eq-code-Dgremlin
```

查看失败日志：

```bash
gh run view \
  --repo szlwin/doc-eq-code-Dgremlin \
  --log-failed
```

## 12. PR 创建后的门禁

现有 `p0-build.yml` 监听目标为 `dev_all` 的 Pull Request，因此 PR 创建后会进入最终构建门禁。

需要注意：PR 如果由 Workflow 自带的 `GITHUB_TOKEN` 创建，后续 PR Workflow 可能进入等待批准状态。此时在 PR 或 Actions 页面执行：

```text
Approve and run workflows
```

如果要求创建 PR 后的 Workflow 完全自动执行，需要改用 GitHub App installation token 或专用 PAT。默认方案不建议直接引入长期 PAT。

## 13. 测试失败后的处理

### core-verify 失败

检查：

```text
Verify Maven Wrapper
Bootstrap repository-bundled legacy dependencies
Compile and run core tests
Prove failing tests block the build
```

下载 Artifact：

```text
core-test-reports-<run-id>
```

### mysql-it 失败

检查：

```text
Initialize MySQL schema
Initialize MySQL test data
Verify MySQL schema
Run MySQL integration tests
```

下载 Artifact：

```text
mysql-test-reports-<run-id>
```

修复后重新 push，或者重新手工运行 Workflow。

## 14. 成功判定

创建 PR 前必须全部满足：

```text
[PASS] Maven Wrapper 可运行
[PASS] 四个仓库内置旧依赖可安装
[PASS] clean verify 成功
[PASS] 单元测试成功
[PASS] 人为失败测试能够阻断 Maven
[PASS] MySQL 8.0 启动并健康
[PASS] schema.sql 导入成功
[PASS] MySQL schema 检查成功
[PASS] -Pmysql-it verify 成功
[PASS] PR 写权限可用
```

## 15. 当前环境的 gh 状态

在当前 ChatGPT Shell 执行环境中实际检查：

```bash
command -v gh
gh --version
gh auth status
```

结果为：

```text
gh not installed
```

结论：

- 当前 Shell 不能直接执行 `gh` 命令。
- 当前 GitHub 连接器可读取并操作已连接的 GitHub 仓库，它与 Shell 中的 `gh` 是两套独立能力。
- 上述 Workflow 运行在 GitHub 的 `ubuntu-latest` Runner 上，并在创建 PR 前执行 `gh --version`；若 Runner 中没有 `gh`，该步骤会立即失败而不是静默跳过。
