#!/bin/sh
set -eu

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
REPORT_DIR=${P1_T15_REPORT_DIR:-"$ROOT/target/p1-t15-retirement"}
SCANNER="$ROOT/scripts/remediation/p1_t15_retirement_scan.py"
RAW_DEPENDENCY_REPORT="$REPORT_DIR/dependency-tree.raw.txt"
DEPENDENCY_LOG="$REPORT_DIR/dependency-tree-command.log"
SUMMARY="$REPORT_DIR/summary.json"
VIOLATIONS="$REPORT_DIR/violations.txt"

mkdir -p "$REPORT_DIR"
rm -f \
  "$REPORT_DIR/reactor-modules.tsv" \
  "$REPORT_DIR/poms-scanned.txt" \
  "$REPORT_DIR/prepare-violations.txt" \
  "$REPORT_DIR/pom-scan.txt" \
  "$REPORT_DIR/source-scan.txt" \
  "$REPORT_DIR/compiled-scan.txt" \
  "$REPORT_DIR/artifact-scan.txt" \
  "$REPORT_DIR/dependency-tree.txt" \
  "$REPORT_DIR/dependency-tree-modules.json" \
  "$RAW_DEPENDENCY_REPORT" \
  "$DEPENDENCY_LOG" \
  "$REPORT_DIR/dependency-tree.status" \
  "$SUMMARY" \
  "$VIOLATIONS"

# 先冻结 Reactor 和全部 POM 清单，后续依赖报告必须逐模块闭合。
python3 "$SCANNER" prepare --root "$ROOT" --report-dir "$REPORT_DIR"
: >"$RAW_DEPENDENCY_REPORT"

# appendOutput 防止 Reactor 后续模块覆盖前序依赖树；扫描器还会逐模块核对坐标标记。
set +e
"$ROOT/mvnw" --batch-mode --no-transfer-progress \
  -DskipTests \
  -DoutputType=text \
  -DoutputFile="$RAW_DEPENDENCY_REPORT" \
  -DappendOutput=true \
  dependency:tree >"$DEPENDENCY_LOG" 2>&1
dependency_status=$?
set -e
printf '%s\n' "$dependency_status" >"$REPORT_DIR/dependency-tree.status"

# POM、源码、ServiceLoader、class 常量池和归档内容均由同一机器扫描器 fail-closed。
set +e
python3 "$SCANNER" scan --root "$ROOT" --report-dir "$REPORT_DIR"
scan_status=$?
set -e

if [ ! -f "$SUMMARY" ] || [ ! -f "$VIOLATIONS" ]; then
  echo "T15 retirement scanner did not produce mandatory evidence" >&2
  exit 2
fi

if [ "$scan_status" -ne 0 ]; then
  violation_count=$(wc -l <"$VIOLATIONS" | tr -d ' ')
  echo "T15 retirement gate found $violation_count residual or incomplete item(s)" >&2
  cat "$VIOLATIONS" >&2
  exit 1
fi

echo "T15 declaration runtime retirement gate passed"
