#!/bin/sh
set -eu

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
VERIFY="$ROOT/scripts/remediation/verify_p1_t15_retirement.sh"
REPORT_DIR="$ROOT/target/p1-t15-retirement"
MUTATION_MODULE="$ROOT/dec-expand-declaration"
MUTATION_SOURCE_DIR="$ROOT/dec-core-starter/src/test/java/dec/core/starter/t15mutation"
MUTATION_SOURCE="$MUTATION_SOURCE_DIR/LegacyDeclarationAdapter.java"
mutation_active=0

# 只清理由本脚本成功注入的内容，绝不删除基线中真实存在的残留。
cleanup_mutation() {
  if [ "$mutation_active" -eq 1 ]; then
    rm -rf "$MUTATION_MODULE"
    rm -rf "$MUTATION_SOURCE_DIR"
    mutation_active=0
  fi
}

# 先证明当前仓库基线本身满足退役合同；失败时只报告，不修改工作树。
sh "$VERIFY"
cp "$REPORT_DIR/summary.json" "$REPORT_DIR/baseline-summary.json"

# 基线通过后才允许注入 mutation，并从此刻开始安装异常清理门禁。
mutation_active=1
trap cleanup_mutation EXIT HUP INT TERM
mkdir -p "$MUTATION_MODULE" "$MUTATION_SOURCE_DIR"
printf '%s\n' '<project><!-- dec-expand-declaration mutation --></project>' \
  >"$MUTATION_MODULE/pom.xml"
cat >"$MUTATION_SOURCE" <<'JAVA'
package dec.core.starter.t15mutation;

/** mutation only: dec.expand.declare / LegacyDeclarationAdapter */
final class LegacyDeclarationAdapter {
}
JAVA

# 同时注入模块目录和未跟踪源码回流，验证两个独立扫描面都会 fail-closed。
set +e
sh "$VERIFY" >"$REPORT_DIR/mutation-run.log" 2>&1
mutation_status=$?
set -e
cp "$REPORT_DIR/summary.json" "$REPORT_DIR/mutation-summary.json"
cp "$REPORT_DIR/violations.txt" "$REPORT_DIR/mutation-violations.txt"

if [ "$mutation_status" -eq 0 ]; then
  echo "T15 retirement mutation unexpectedly passed" >&2
  exit 1
fi
if ! grep -q '^MODULE[[:space:]]' "$REPORT_DIR/mutation-violations.txt"; then
  echo "T15 retirement mutation did not detect module residue" >&2
  exit 1
fi
if ! grep -q '^SOURCE[[:space:]]' "$REPORT_DIR/mutation-violations.txt"; then
  echo "T15 retirement mutation did not detect source residue" >&2
  exit 1
fi

# 删除注入内容后再次验证，证明门禁没有依赖脏工作树或一次性缓存。
cleanup_mutation
sh "$VERIFY"
cp "$REPORT_DIR/summary.json" "$REPORT_DIR/restored-summary.json"

python3 - "$REPORT_DIR/mutation-proof-summary.json" <<'PY'
import json
import sys

with open(sys.argv[1], "w", encoding="utf-8") as output:
    json.dump(
        {
            "task": "TASK-P1-T15",
            "gate": "DECLARATION_RUNTIME_RETIREMENT_MUTATION_PROOF",
            "result": "PASSED",
            "expectedBlocked": True,
            "detectedCategories": ["MODULE", "SOURCE"],
            "restoredBaselinePassed": True,
            "baselineFailureIsReadOnly": True,
        },
        output,
        ensure_ascii=False,
        indent=2,
        sort_keys=True,
    )
    output.write("\n")
PY

echo "T15 declaration retirement mutation proof passed"
