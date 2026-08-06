#!/bin/sh
set -eu

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
REPORT_DIR="$ROOT/target/p1-t15-retirement"
DEPENDENCY_REPORT="$REPORT_DIR/dependency-tree.txt"
VIOLATIONS="$REPORT_DIR/violations.txt"
SUMMARY="$REPORT_DIR/summary.json"

mkdir -p "$REPORT_DIR"
: >"$VIOLATIONS"

# 统一记录残留事实，保证 CI 和独立 Review 可以机器解析失败原因。
record_violation() {
  category=$1
  detail=$2
  printf '%s\t%s\n' "$category" "$detail" >>"$VIOLATIONS"
}

# 旧模块目录和根 Reactor 声明必须整体消失。
if [ -e "$ROOT/dec-expand-declaration" ]; then
  record_violation "MODULE" "dec-expand-declaration directory exists"
fi

if grep -n "dec-expand-declaration" "$ROOT/pom.xml" >/dev/null 2>&1; then
  grep -n "dec-expand-declaration" "$ROOT/pom.xml" \
    | while IFS= read -r line; do
        record_violation "ROOT_POM" "$line"
      done
fi

# Starter 只能通过 Compiler 公共 API 工作，不得保留全局 Config 或旧 Parser 依赖。
for legacy_file in \
  "$ROOT/dec-core-starter/src/main/java/dec/core/starter/common/ConfigUtil.java" \
  "$ROOT/dec-core-starter/src/main/java/dec/core/starter/common/DataSourceManager.java"
do
  if [ -e "$legacy_file" ]; then
    record_violation "STARTER_GLOBAL" "${legacy_file#$ROOT/}"
  fi
done

for legacy_dependency in \
  dec-context-config-parse-xml \
  dec-context-config-parse-yaml
do
  if grep -n "$legacy_dependency" "$ROOT/dec-core-starter/pom.xml" >/dev/null 2>&1; then
    grep -n "$legacy_dependency" "$ROOT/dec-core-starter/pom.xml" \
      | while IFS= read -r line; do
          record_violation "STARTER_DEPENDENCY" "$line"
        done
  fi
done

# 同时扫描已跟踪和未跟踪源码，使 mutation proof 注入的回流文件也必须被阻断。
set +e
git -C "$ROOT" grep --untracked -n -I -E \
  'dec\.expand\.declare|LegacyDeclarationAdapter|doc\.eq\.code:dec-expand-declaration' \
  -- \
  ':(glob)**/src/main/**' \
  ':(glob)**/src/test/**' \
  ':(glob)**/META-INF/services/**' \
  ':(exclude)dec-expand-declaration/**' \
  >"$REPORT_DIR/source-scan.txt"
source_status=$?
set -e
if [ "$source_status" -eq 0 ]; then
  while IFS= read -r line; do
    record_violation "SOURCE" "$line"
  done <"$REPORT_DIR/source-scan.txt"
elif [ "$source_status" -ne 1 ]; then
  echo "source residual scan failed unexpectedly" >&2
  exit 2
fi

# Maven 依赖树必须证明没有任何模块继续解析旧 Artifact。
"$ROOT/mvnw" --batch-mode --no-transfer-progress \
  -DskipTests \
  -DoutputType=text \
  -DoutputFile="$DEPENDENCY_REPORT" \
  dependency:tree >/dev/null

if grep -n "dec-expand-declaration" "$DEPENDENCY_REPORT" >/dev/null 2>&1; then
  grep -n "dec-expand-declaration" "$DEPENDENCY_REPORT" \
    | while IFS= read -r line; do
        record_violation "DEPENDENCY_TREE" "$line"
      done
fi

# 发布 Artifact 中不得包含旧 package、旧 artifactId 或 Adapter 名称。
: >"$REPORT_DIR/artifact-scan.txt"
find "$ROOT" -type f \( -name '*.jar' -o -name '*.war' -o -name '*.zip' \) \
  -path '*/target/*' -print \
  | LC_ALL=C sort \
  | while IFS= read -r artifact; do
      if unzip -Z1 "$artifact" 2>/dev/null \
          | grep -E '(^|/)dec/expand/declare/|LegacyDeclarationAdapter|dec-expand-declaration' \
          >"$REPORT_DIR/current-artifact-match.txt"; then
        while IFS= read -r entry; do
          record_violation \
            "ARTIFACT" \
            "${artifact#$ROOT/}:$entry"
        done <"$REPORT_DIR/current-artifact-match.txt"
      fi
    done

violation_count=$(wc -l <"$VIOLATIONS" | tr -d ' ')
result=PASSED
if [ "$violation_count" -ne 0 ]; then
  result=FAILED
fi

python3 - "$SUMMARY" "$result" "$violation_count" <<'PY'
import json
import sys

summary_path, result, count = sys.argv[1:]
with open(summary_path, "w", encoding="utf-8") as output:
    json.dump(
        {
            "task": "TASK-P1-T15",
            "gate": "DECLARATION_RUNTIME_RETIREMENT",
            "result": result,
            "violationCount": int(count),
            "scopes": [
                "REACTOR_MODULES",
                "DEPENDENCY_MANAGEMENT",
                "DEPENDENCY_TREE",
                "SOURCE_REFERENCES",
                "SERVICE_LOADER",
                "REFLECTION_STRINGS",
                "PUBLISHED_ARTIFACTS",
                "STARTER_GLOBAL_CONFIG",
            ],
        },
        output,
        ensure_ascii=False,
        indent=2,
        sort_keys=True,
    )
    output.write("\n")
PY

if [ "$result" != "PASSED" ]; then
  echo "T15 retirement gate found $violation_count residual item(s)" >&2
  cat "$VIOLATIONS" >&2
  exit 1
fi

echo "T15 declaration runtime retirement gate passed"
