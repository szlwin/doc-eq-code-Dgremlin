#!/bin/sh
set -eu

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
REPORT_DIR=${P1_T15_REPORT_DIR:-"$ROOT/target/p1-t15-retirement"}
SCANNER="$ROOT/scripts/remediation/p1_t15_retirement_scan.py"
RAW_DEPENDENCY_REPORT="$REPORT_DIR/dependency-tree.raw.txt"
DEPENDENCY_LOG="$REPORT_DIR/dependency-tree-command.log"
DEPENDENCY_DIR="$REPORT_DIR/dependency-trees"
DEPENDENCY_STATUS_TSV="$REPORT_DIR/dependency-tree-status.tsv"
SUMMARY="$REPORT_DIR/summary.json"
VIOLATIONS="$REPORT_DIR/violations.txt"

mkdir -p "$REPORT_DIR"
rm -rf "$DEPENDENCY_DIR"
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
  "$DEPENDENCY_STATUS_TSV" \
  "$SUMMARY" \
  "$VIOLATIONS"
mkdir -p "$DEPENDENCY_DIR"

# 先冻结 Reactor 和全部 POM 清单，后续依赖报告必须逐模块闭合。
python3 "$SCANNER" prepare --root "$ROOT" --report-dir "$REPORT_DIR"
: >"$RAW_DEPENDENCY_REPORT"
: >"$DEPENDENCY_LOG"
printf '%s\n' 'path	coordinate	status	report' >"$DEPENDENCY_STATUS_TSV"

# Maven Reactor 的 outputFile 会被后续模块覆盖，因此每个目标模块独立生成依赖树。
module_index=0
TAB=$(printf '\t')
tail -n +2 "$REPORT_DIR/reactor-modules.tsv" \
  | while IFS="$TAB" read -r module_path group_id artifact_id packaging; do
      module_index=$((module_index + 1))
      safe_path=$(printf '%s' "$module_path" | sed 's#[^A-Za-z0-9_.-]#_#g')
      if [ "$module_path" = "." ]; then
        safe_path=root
      fi
      report_name=$(printf 'dependency-trees/%03d-%s.txt' "$module_index" "$safe_path")
      log_name=$(printf 'dependency-trees/%03d-%s.log' "$module_index" "$safe_path")
      report_file="$REPORT_DIR/$report_name"
      log_file="$REPORT_DIR/$log_name"
      coordinate="$group_id:$artifact_id"

      set +e
      if [ "$module_path" = "." ]; then
        "$ROOT/mvnw" --batch-mode --no-transfer-progress \
          -N \
          -DskipTests \
          -DoutputType=text \
          -DoutputFile="$report_file" \
          dependency:tree >"$log_file" 2>&1
      else
        "$ROOT/mvnw" --batch-mode --no-transfer-progress \
          -pl "$module_path" \
          -am \
          -DskipTests \
          -DoutputType=text \
          -DoutputFile="$report_file" \
          dependency:tree >"$log_file" 2>&1
      fi
      module_status=$?
      set -e

      # 命令成功仍必须证明该独立文件确实属于目标模块，避免再次接受覆盖产物。
      if [ "$module_status" -eq 0 ]; then
        if [ ! -f "$report_file" ]; then
          module_status=3
        elif ! grep -F -q "$coordinate:" "$report_file"; then
          module_status=4
        fi
      fi

      printf '===== MODULE %s (%s) =====\n' "$module_path" "$coordinate" \
        >>"$RAW_DEPENDENCY_REPORT"
      if [ -f "$report_file" ]; then
        cat "$report_file" >>"$RAW_DEPENDENCY_REPORT"
      fi
      printf '\n' >>"$RAW_DEPENDENCY_REPORT"

      printf '===== MODULE %s (%s) / STATUS %s =====\n' \
        "$module_path" "$coordinate" "$module_status" >>"$DEPENDENCY_LOG"
      if [ -f "$log_file" ]; then
        cat "$log_file" >>"$DEPENDENCY_LOG"
      fi
      printf '\n' >>"$DEPENDENCY_LOG"

      printf '%s\t%s\t%s\t%s\n' \
        "$module_path" "$coordinate" "$module_status" "$report_name" \
        >>"$DEPENDENCY_STATUS_TSV"
    done

# 任一模块命令失败、报告缺失或目标坐标缺失，都把完整依赖树生成判为失败。
dependency_status=0
if ! awk -F '\t' 'NR > 1 && $3 != 0 { failed = 1 } END { exit failed ? 1 : 0 }' \
    "$DEPENDENCY_STATUS_TSV"; then
  dependency_status=1
fi
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
