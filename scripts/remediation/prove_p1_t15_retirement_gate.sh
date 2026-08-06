#!/bin/sh
set -eu

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
VERIFY="$ROOT/scripts/remediation/verify_p1_t15_retirement.sh"
REPORT_DIR="$ROOT/target/p1-t15-retirement"
ROOT_POM="$ROOT/pom.xml"
XML_POM="$ROOT/dec-context-config-parse-xml/pom.xml"
MUTATION_MODULE="$ROOT/dec-expand-declaration"
MUTATION_RESOURCE_DIR="$ROOT/dec-core-starter/src/test/resources/t15mutationi002"
MUTATION_CLASS_DIR="$ROOT/dec-core-starter/target/test-classes/dec/core/starter/t15mutationi002"
MUTATION_ARCHIVE="$ROOT/target/t15-neutral-content.zip"
BACKUP_DIR=""
mutation_active=0

# 只恢复本脚本修改的 POM 和注入文件，不清理任何基线文件。
cleanup_mutation() {
  if [ "$mutation_active" -eq 1 ]; then
    cp "$BACKUP_DIR/root-pom.xml" "$ROOT_POM"
    cp "$BACKUP_DIR/xml-pom.xml" "$XML_POM"
    rm -rf "$MUTATION_MODULE"
    rm -rf "$MUTATION_RESOURCE_DIR"
    rm -rf "$MUTATION_CLASS_DIR"
    rm -f "$MUTATION_ARCHIVE"
    mutation_active=0
  fi
  if [ -n "$BACKUP_DIR" ] && [ -d "$BACKUP_DIR" ]; then
    rm -rf "$BACKUP_DIR"
    BACKUP_DIR=""
  fi
}

# 基线必须先通过；失败时门禁保持只读并立即阻断 mutation。
sh "$VERIFY"
cp "$REPORT_DIR/summary.json" "$REPORT_DIR/baseline-summary.json"
cp "$REPORT_DIR/dependency-tree-modules.json" "$REPORT_DIR/baseline-dependency-tree-modules.json"

BACKUP_DIR=$(mktemp -d "${TMPDIR:-/tmp}/p1-t15-i002.XXXXXX")
cp "$ROOT_POM" "$BACKUP_DIR/root-pom.xml"
cp "$XML_POM" "$BACKUP_DIR/xml-pom.xml"
mutation_active=1
trap cleanup_mutation EXIT HUP INT TERM

# 注入真实 Reactor 旧模块、根 profile/dependencyManagement 坐标和非 Demo 模块依赖。
mkdir -p "$MUTATION_MODULE"
cat >"$MUTATION_MODULE/pom.xml" <<'XML'
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>doc.eq.code</groupId>
  <artifactId>dec-expand-declaration</artifactId>
  <version>1.0</version>
</project>
XML
python3 - "$ROOT_POM" "$XML_POM" <<'PY'
from pathlib import Path
import re
import sys

root_pom = Path(sys.argv[1])
xml_pom = Path(sys.argv[2])
root_text = root_pom.read_text(encoding="utf-8")
root_text, module_count = re.subn(
    r"</modules>",
    "<module>dec-expand-declaration</module>\n</modules>",
    root_text,
    count=1,
)
profile = """
        <profile>
            <id>p1-t15-i002-retired-coordinate-mutation</id>
            <dependencyManagement>
                <dependencies>
                    <dependency>
                        <groupId>doc.eq.code</groupId>
                        <artifactId>dec-expand-declaration</artifactId>
                        <version>1.0</version>
                    </dependency>
                </dependencies>
            </dependencyManagement>
        </profile>
"""
root_text, profile_count = re.subn(
    r"</profiles>",
    profile + "</profiles>",
    root_text,
    count=1,
)
if module_count != 1 or profile_count != 1:
    raise SystemExit("unable to inject root module/profile mutation")
root_pom.write_text(root_text, encoding="utf-8")

xml_text = xml_pom.read_text(encoding="utf-8")
dependency = """
        <dependency>
            <groupId>doc.eq.code</groupId>
            <artifactId>dec-expand-declaration</artifactId>
            <version>1.0</version>
        </dependency>
"""
xml_text, dependency_count = re.subn(
    r"</dependencies>",
    dependency + "</dependencies>",
    xml_text,
    count=1,
)
if dependency_count != 1:
    raise SystemExit("unable to inject non-Demo module dependency mutation")
xml_pom.write_text(xml_text, encoding="utf-8")
PY

# 中性文件名中的反射字符串和 ServiceLoader 内容必须独立触发阻断。
mkdir -p "$MUTATION_RESOURCE_DIR/META-INF/services"
printf '%s\n' 'Class.forName("dec.expand.declare.business.BusinessDeclare")' \
  >"$MUTATION_RESOURCE_DIR/reflection-probe.txt"
printf '%s\n' 'dec.expand.declare.service.DefaultServiceDeclare' \
  >"$MUTATION_RESOURCE_DIR/META-INF/services/example.spi.Contract"

# Java 源文件放在仓库外，只把带旧 FQCN 的 class 常量池注入 target/test-classes。
mkdir -p "$MUTATION_CLASS_DIR"
cat >"$BACKUP_DIR/GateProbe.java" <<'JAVA'
package dec.core.starter.t15mutationi002;

/** 仅用于验证 class 常量池残留会被退休门禁阻断。 */
final class GateProbe {
    private static final String LEGACY_TYPE =
            "dec.expand.declare.business.BusinessDeclare";

    private GateProbe() {
    }
}
JAVA
javac -source 8 -target 8 -d "$ROOT/dec-core-starter/target/test-classes" \
  "$BACKUP_DIR/GateProbe.java" >/dev/null 2>&1

# 中性 ZIP entry 名称不得掩盖资源内容中的旧运行时字符串。
mkdir -p "$ROOT/target"
python3 - "$MUTATION_ARCHIVE" <<'PY'
from pathlib import Path
import sys
import zipfile

archive = Path(sys.argv[1])
with zipfile.ZipFile(str(archive), "w", compression=zipfile.ZIP_DEFLATED) as output:
    output.writestr("neutral/payload.bin", b"dec.expand.declare.business.BusinessDeclare")
PY

# 一次 mutation 必须同时证明所有 Review 指定扫描面均 fail-closed。
set +e
sh "$VERIFY" >"$REPORT_DIR/mutation-run.log" 2>&1
mutation_status=$?
set -e
cp "$REPORT_DIR/summary.json" "$REPORT_DIR/mutation-summary.json"
cp "$REPORT_DIR/violations.txt" "$REPORT_DIR/mutation-violations.txt"
cp "$REPORT_DIR/dependency-tree.txt" "$REPORT_DIR/mutation-dependency-tree.txt"
cp "$REPORT_DIR/dependency-tree-modules.json" "$REPORT_DIR/mutation-dependency-tree-modules.json"

if [ "$mutation_status" -eq 0 ]; then
  echo "T15 I002 retirement mutation unexpectedly passed" >&2
  exit 1
fi

require_category() {
  category=$1
  if ! grep -q "^${category}[[:space:]]" "$REPORT_DIR/mutation-violations.txt"; then
    echo "T15 I002 mutation did not detect category: $category" >&2
    exit 1
  fi
}

for category in \
  MODULE \
  POM_COORDINATE \
  DEPENDENCY_TREE \
  SOURCE_REFERENCE \
  SERVICE_LOADER \
  CLASS_CONSTANT_POOL \
  ARTIFACT_RESOURCE_CONTENT
do
  require_category "$category"
done

# 额外确认 POM proof 同时覆盖根 profile/dependencyManagement 和非 Demo 模块依赖。
root_pom_hits=$(grep -c '^POM_COORDINATE[[:space:]]pom.xml:' \
  "$REPORT_DIR/mutation-violations.txt" || true)
if [ "$root_pom_hits" -lt 2 ]; then
  echo "T15 I002 mutation did not prove root module plus profile/dependencyManagement scanning" >&2
  exit 1
fi
if ! grep -q '^POM_COORDINATE[[:space:]]dec-context-config-parse-xml/pom.xml:' \
    "$REPORT_DIR/mutation-violations.txt"; then
  echo "T15 I002 mutation did not prove non-Demo module POM scanning" >&2
  exit 1
fi

# 恢复所有注入内容后重新验证，证明扫描结果不依赖脏工作树或旧缓存。
cleanup_mutation
sh "$VERIFY"
cp "$REPORT_DIR/summary.json" "$REPORT_DIR/restored-summary.json"
cp "$REPORT_DIR/dependency-tree-modules.json" "$REPORT_DIR/restored-dependency-tree-modules.json"

python3 - "$REPORT_DIR/mutation-proof-summary.json" <<'PY'
import json
import sys

with open(sys.argv[1], "w", encoding="utf-8") as output:
    json.dump(
        {
            "task": "TASK-P1-T15",
            "iteration": "I002",
            "gate": "DECLARATION_RUNTIME_RETIREMENT_MUTATION_PROOF",
            "result": "PASSED",
            "expectedBlocked": True,
            "detectedCategories": [
                "MODULE",
                "POM_COORDINATE",
                "DEPENDENCY_TREE",
                "SOURCE_REFERENCE",
                "SERVICE_LOADER",
                "CLASS_CONSTANT_POOL",
                "ARTIFACT_RESOURCE_CONTENT",
            ],
            "scenarios": [
                "NON_DEMO_MODULE_POM_DEPENDENCY",
                "DEPENDENCY_MANAGEMENT_PROFILE_COORDINATE",
                "FULL_REACTOR_DEPENDENCY_TREE",
                "NEUTRAL_REFLECTION_RESOURCE",
                "CLASS_CONSTANT_POOL",
                "SERVICE_LOADER_CONTENT",
                "NEUTRAL_ARCHIVE_ENTRY_CONTENT",
            ],
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

echo "T15 I002 declaration retirement mutation proof passed"
