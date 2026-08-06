#!/bin/sh
set -eu

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
PASS_FILE="$ROOT/dec-core-compiler/src/main/java/dec/core/compiler/pass/CandidateContextPublicationPass.java"
BOUND_FILE="$ROOT/dec-core-compiler/src/main/java/dec/core/compiler/compiled/DigestBoundCompiledInput.java"
REPORT_DIR="$ROOT/dec-core-compiler/target/t14-mutation-proof"
SUREFIRE_DIR="$ROOT/dec-core-compiler/target/surefire-reports"
TMP_DIR=$(mktemp -d)

cp "$PASS_FILE" "$TMP_DIR/CandidateContextPublicationPass.java"
cp "$BOUND_FILE" "$TMP_DIR/DigestBoundCompiledInput.java"
mkdir -p "$REPORT_DIR"

# 无论 mutation 验证在哪一步退出，都必须恢复正确生产源码。
restore_sources() {
  cp "$TMP_DIR/CandidateContextPublicationPass.java" "$PASS_FILE"
  cp "$TMP_DIR/DigestBoundCompiledInput.java" "$BOUND_FILE"
}

cleanup() {
  restore_sources 2>/dev/null || true
  rm -rf "$TMP_DIR"
}
trap cleanup EXIT HUP INT TERM

# 校验目标测试已实际执行，失败是行为断言而不是编译或环境错误。
validate_behavior_failure() {
  status=$1
  xml_file=$2
  log_file=$3
  summary_file=$4
  mutation_name=$5

  if [ "$status" -eq 0 ]; then
    echo "ERROR: $mutation_name mutation unexpectedly passed" >&2
    exit 1
  fi
  if [ ! -f "$xml_file" ]; then
    echo "ERROR: $mutation_name did not produce Surefire XML" >&2
    exit 1
  fi
  if grep -q "COMPILATION ERROR" "$log_file"; then
    echo "ERROR: $mutation_name failed during compilation" >&2
    exit 1
  fi

  python3 - "$xml_file" "$summary_file" "$mutation_name" "$status" <<'PY'
import json
import sys
import xml.etree.ElementTree as ET

xml_path, summary_path, mutation_name, status_text = sys.argv[1:]
root = ET.parse(xml_path).getroot()
tests = int(root.attrib.get("tests", "0"))
failures = int(root.attrib.get("failures", "0"))
errors = int(root.attrib.get("errors", "0"))
skipped = int(root.attrib.get("skipped", "0"))
failure = root.find(".//failure")
failure_type = "" if failure is None else failure.attrib.get("type", "")

if tests != 1 or failures != 1 or errors != 0 or skipped != 0:
    raise SystemExit(
        "%s expected tests=1/failures=1/errors=0/skipped=0, got %s/%s/%s/%s"
        % (mutation_name, tests, failures, errors, skipped)
    )
if failure is None or (
        "Assertion" not in failure_type
        and "opentest4j" not in failure_type.lower()):
    raise SystemExit(
        "%s did not fail with an assertion: %s"
        % (mutation_name, failure_type)
    )

with open(summary_path, "w", encoding="utf-8") as output:
    json.dump(
        {
            "mutation": mutation_name,
            "mavenStatus": int(status_text),
            "tests": tests,
            "failures": failures,
            "errors": errors,
            "skipped": skipped,
            "failureType": failure_type,
            "classification": "BEHAVIOR_ASSERTION_FAILURE",
        },
        output,
        ensure_ascii=False,
        indent=2,
        sort_keys=True,
    )
    output.write("\n")
PY
}

# Mutation A：临时短路 request schema/options 门禁。
python3 - "$PASS_FILE" <<'PY'
from pathlib import Path
import sys

path = Path(sys.argv[1])
text = path.read_text(encoding="utf-8")
old = """        if (!input.get().matchesRequest(
                options.schemaVersion(),
                options.optionsDigest())) {"""
new = """        // I003 mutation proof：临时短路 request provenance 门禁。
        if (false && !input.get().matchesRequest(
                options.schemaVersion(),
                options.optionsDigest())) {"""
if text.count(old) != 1:
    raise SystemExit("request binding mutation anchor mismatch")
path.write_text(text.replace(old, new), encoding="utf-8")
PY

set +e
"$ROOT/mvnw" --batch-mode --no-transfer-progress \
  -pl dec-core-compiler -am \
  -Dtest=CandidateContextT14Test#requestMismatchFailsWithExactDiagnostic \
  -Dsurefire.failIfNoSpecifiedTests=false \
  test >"$REPORT_DIR/request-binding.log" 2>&1
request_status=$?
set -e
request_xml="$SUREFIRE_DIR/TEST-dec.core.compiler.pass.CandidateContextT14Test.xml"
validate_behavior_failure \
  "$request_status" \
  "$request_xml" \
  "$REPORT_DIR/request-binding.log" \
  "$REPORT_DIR/request-binding.json" \
  "REQUEST_BINDING_BYPASS"
cp "$request_xml" "$REPORT_DIR/request-binding.xml"
restore_sources

# Mutation B：临时跳过 raw/published Source identity 闭包门禁。
python3 - "$BOUND_FILE" <<'PY'
from pathlib import Path
import sys

path = Path(sys.argv[1])
text = path.read_text(encoding="utf-8")
old = "        requireSameSourceClosure(checkedSources, checkedManifest);"
new = """        // I003 mutation proof：临时跳过 Source identity 闭包门禁。
        if (false) {
            requireSameSourceClosure(checkedSources, checkedManifest);
        }"""
if text.count(old) != 1:
    raise SystemExit("source closure mutation anchor mismatch")
path.write_text(text.replace(old, new), encoding="utf-8")
PY

set +e
"$ROOT/mvnw" --batch-mode --no-transfer-progress \
  -pl dec-core-compiler -am \
  -Dtest=CandidateContextT14IndependentReviewTest#sourceManifestClosureMismatchFailsClosed \
  -Dsurefire.failIfNoSpecifiedTests=false \
  test >"$REPORT_DIR/source-closure.log" 2>&1
closure_status=$?
set -e
closure_xml="$SUREFIRE_DIR/TEST-dec.core.compiler.pass.CandidateContextT14IndependentReviewTest.xml"
validate_behavior_failure \
  "$closure_status" \
  "$closure_xml" \
  "$REPORT_DIR/source-closure.log" \
  "$REPORT_DIR/source-closure.json" \
  "SOURCE_CLOSURE_BYPASS"
cp "$closure_xml" "$REPORT_DIR/source-closure.xml"
restore_sources

# 恢复正确实现后重跑两个目标测试，确保 mutation 不污染最终 GREEN。
"$ROOT/mvnw" --batch-mode --no-transfer-progress \
  -pl dec-core-compiler -am \
  -Dtest=CandidateContextT14Test#requestMismatchFailsWithExactDiagnostic,CandidateContextT14IndependentReviewTest#sourceManifestClosureMismatchFailsClosed \
  -Dsurefire.failIfNoSpecifiedTests=false \
  test >"$REPORT_DIR/restored-green.log" 2>&1

python3 - \
  "$SUREFIRE_DIR/TEST-dec.core.compiler.pass.CandidateContextT14Test.xml" \
  "$SUREFIRE_DIR/TEST-dec.core.compiler.pass.CandidateContextT14IndependentReviewTest.xml" \
  "$REPORT_DIR/restored-green.json" <<'PY'
import json
import sys
import xml.etree.ElementTree as ET

records = []
for xml_path in sys.argv[1:3]:
    root = ET.parse(xml_path).getroot()
    record = {
        "file": xml_path,
        "tests": int(root.attrib.get("tests", "0")),
        "failures": int(root.attrib.get("failures", "0")),
        "errors": int(root.attrib.get("errors", "0")),
        "skipped": int(root.attrib.get("skipped", "0")),
    }
    if record["tests"] != 1 or any(
            record[key] != 0 for key in ("failures", "errors", "skipped")):
        raise SystemExit("restored GREEN validation failed: %r" % record)
    records.append(record)

with open(sys.argv[3], "w", encoding="utf-8") as output:
    json.dump(
        {
            "classification": "RESTORED_GREEN",
            "records": records,
        },
        output,
        ensure_ascii=False,
        indent=2,
        sort_keys=True,
    )
    output.write("\n")
PY

python3 - "$REPORT_DIR" <<'PY'
import json
import pathlib
import sys

report_dir = pathlib.Path(sys.argv[1])
with (report_dir / "request-binding.json").open(encoding="utf-8") as source:
    request = json.load(source)
with (report_dir / "source-closure.json").open(encoding="utf-8") as source:
    closure = json.load(source)
with (report_dir / "restored-green.json").open(encoding="utf-8") as source:
    restored = json.load(source)

with (report_dir / "summary.json").open("w", encoding="utf-8") as output:
    json.dump(
        {
            "task": "TASK-P1-T14",
            "iteration": "I003",
            "mode": "TDD_REPAIR_ORACLE_HARDENING",
            "mutations": [request, closure],
            "restored": restored,
            "result": "PASSED",
        },
        output,
        ensure_ascii=False,
        indent=2,
        sort_keys=True,
    )
    output.write("\n")
PY

echo "T14 provenance mutation gate proved and restored to GREEN"
