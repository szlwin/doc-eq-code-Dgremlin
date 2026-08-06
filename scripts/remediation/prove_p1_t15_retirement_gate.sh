#!/bin/sh
set -eu

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
VERIFY="$ROOT/scripts/remediation/verify_p1_t15_retirement.sh"
REPORT_DIR="$ROOT/target/p1-t15-retirement"
MUTATION_MODULE="$ROOT/dec-expand-declaration"
MUTATION_SOURCE_DIR="$ROOT/dec-core-starter/src/test/java/dec/core/starter/t15mutation"
MUTATION_SOURCE="$MUTATION_SOURCE_DIR/LegacyDeclarationAdapter.java"

# 无论验证成功或中断，都必须恢复工作树，避免 mutation 污染后续构建。
cleanup_mutation() {
  rm -rf "$MUTATION_MODULE"
  rm -rf "$MUTATION_SOURCE_DIR"
}
trap cleanup_mutation EXIT HUP INT TERM

# 先证明当前仓库基线本身满足退役合同。
sh "$VERIFY"
cp "$REPORT_DIR/summary.json" "$REPORT_DIR/baseline-summary.json"

# 同时注入模块目录和未跟踪源码回流，验证两个独立扫描面都会 fail-closed。
mkdir -p "$MUTATION_MODULE" "$MUTATION_SOURCE_DIR"
printf '%s\n' '<project><!-- dec-expand-declaration mutation --></project>' \
  >"$MUTATION_MODULE/pom.xml"
cat >"$MUTATION_SOURCE" <<'JAVA'
package dec.core.starter.t15mutation;

/** mutation only: dec.expand.declare / LegacyDeclarationAdapter */
final class LegacyDeclarationAdapter {
}
JAVA

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
        },
        output,
        ensure_ascii=False,
        indent=2,
        sort_keys=True,
    )
    output.write("\n")
PY

echo "T15 declaration retirement mutation proof passed"
