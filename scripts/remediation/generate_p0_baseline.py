#!/usr/bin/env python3
import argparse
import re
import shutil
import subprocess
from pathlib import Path


def run(*args):
    return subprocess.run(args, text=True, capture_output=True, errors="replace")


def git(*args):
    result = run("git", *args)
    if result.returncode:
        raise SystemExit(result.stderr.strip() or "git command failed")
    return result.stdout


def command_version(command, *args):
    if shutil.which(command) is None:
        return "not installed"
    result = run(command, *args)
    output = (result.stdout + result.stderr).strip().splitlines()
    return output[0] if output else f"exit={result.returncode}"


parser = argparse.ArgumentParser()
parser.add_argument("--revision", default="HEAD")
args = parser.parse_args()
revision = args.revision
commit = git("rev-parse", revision).strip()
files = [path for path in git("ls-tree", "-r", "--name-only", revision).splitlines() if path]


def count(suffix):
    return sum(path.endswith(suffix) for path in files)


def grep(pattern):
    result = run(
        "git", "grep", "-n", "-E", pattern, revision, "--",
        "*.java", "*.xml", "*.yaml", "*.yml", "pom.xml"
    )
    return [line for line in result.stdout.splitlines() if line]


root_pom = git("show", f"{revision}:pom.xml")
modules = []
for line in root_pom.splitlines():
    match = re.search(r"<module>([^<]+)</module>", line)
    if match and not line.strip().startswith("<!--"):
        modules.append(match.group(1))

main_methods = grep(r"public[[:space:]]+static[[:space:]]+void[[:space:]]+main")
test_failure_ignore = grep(r"testFailureIgnore")
hardcoded_connections = grep(r"jdbc:mysql|mysqldb|con1|con2")

print("# P0 整改前可重复基线")
print(f"\n- Revision: `{commit}`")
print(f"- Git: `{command_version('git', '--version')}`")
print(f"- Java runtime used for audit: `{command_version('java', '-version')}`")
print(f"- Maven executable used for audit: `{command_version('mvn', '-version')}`")
print(f"- Maven Wrapper at baseline: `{'present' if 'mvnw' in files else 'absent'}`")
print(f"- Modules in Reactor: {len(modules)} (`" + "`, `".join(modules) + "`)")
print(f"- `dec-demo` in Reactor: `{'yes' if 'dec-demo' in modules else 'no'}`")
print(f"- Tracked files: {len(files)}")
print(
    f"- Java: {count('.java')}; XML: {count('.xml')}; "
    f"YAML: {count('.yaml') + count('.yml')}; Markdown: {count('.md')}"
)
print("\n## 已知构建阻断")
print("- 基线没有 Maven Wrapper，当前审计容器也没有系统 Maven。")
print(f"- `{len(test_failure_ignore)}` 处 `testFailureIgnore` 允许测试失败不阻断。")
print("- `dec-demo` 未进入默认 Reactor。" if "dec-demo" not in modules else "- `dec-demo` 已进入默认 Reactor。")
print("- 数据库示例含固定 URL、账号、密码或连接名，无法作为无外部数据库的核心门禁。")

for title, rows in [
    ("main 方法", main_methods),
    ("testFailureIgnore", test_failure_ignore),
    ("固定数据库/连接", hardcoded_connections),
]:
    print(f"\n## {title} ({len(rows)})")
    print("```text")
    print("\n".join(rows) or "(none)")
    print("```")
