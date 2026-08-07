#!/usr/bin/env python3
"""TASK-P1-T15 I002：Declaration Runtime 退役门禁扫描器。"""

import argparse
import io
import json
import re
import sys
import zipfile
from pathlib import Path
import xml.etree.ElementTree as ET

TOKENS = (
    ("LEGACY_ARTIFACT", b"dec-expand-declaration"),
    ("LEGACY_PACKAGE_DOTTED", b"dec.expand.declare"),
    ("LEGACY_PACKAGE_SLASHED", b"dec/expand/declare"),
    ("LEGACY_ADAPTER", b"LegacyDeclarationAdapter"),
    ("LEGACY_COORDINATE", b"doc.eq.code:dec-expand-declaration"),
)
NS = {"m": "http://maven.apache.org/POM/4.0.0"}
ARCHIVES = {".jar", ".war", ".zip"}


def rel(root, path):
    """生成稳定的仓库相对路径。"""
    return path.relative_to(root).as_posix()


def matches(data):
    """返回内容中出现的旧运行时标识。"""
    return [name for name, token in TOKENS if token in data]


def line_of(data, token):
    """返回二进制内容中标识首次出现的行号。"""
    offset = data.find(token)
    return data[:max(offset, 0)].count(b"\n") + 1


def poms(root):
    """枚举全部非 target、非 project_doc 的 POM。"""
    found = []
    for path in root.rglob("pom.xml"):
        parts = path.relative_to(root).parts
        if not any(part in {".git", "target", "project_doc"} for part in parts):
            found.append(path)
    return sorted(found, key=lambda path: rel(root, path))


def value(project, name, parent=False):
    prefix = "m:parent/" if parent else ""
    node = project.find(prefix + "m:" + name, NS)
    return node.text.strip() if node is not None and node.text else ""


def reactor(root):
    """递归解析 Reactor，并保留每个模块的坐标。"""
    rows, failures, visited = [], [], set()

    def visit(directory, inherited_group=""):
        pom = directory / "pom.xml"
        resolved = pom.resolve()
        if resolved in visited:
            return
        visited.add(resolved)
        if not pom.is_file():
            failures.append("REACTOR_POM_MISSING\t" + rel(root, pom))
            return
        try:
            project = ET.parse(str(pom)).getroot()
        except (ET.ParseError, OSError) as error:
            failures.append("POM_PARSE_FAILURE\t%s:%s" % (rel(root, pom), error))
            return
        group = value(project, "groupId") or value(project, "groupId", True) or inherited_group
        artifact = value(project, "artifactId")
        packaging = value(project, "packaging") or "jar"
        path = "." if directory == root else rel(root, directory)
        if not group or not artifact:
            failures.append("POM_COORDINATE_MISSING\t" + rel(root, pom))
        rows.append((path, group, artifact, packaging))
        for module in project.findall("m:modules/m:module", NS):
            if module.text and module.text.strip():
                visit((directory / module.text.strip()).resolve(), group)

    visit(root.resolve())
    return rows, failures


def prepare(root, report):
    """冻结 Reactor 与 POM 清单，供 Maven 依赖树和最终扫描复用。"""
    report.mkdir(parents=True, exist_ok=True)
    rows, failures = reactor(root)
    with (report / "reactor-modules.tsv").open("w", encoding="utf-8") as output:
        output.write("path\tgroupId\tartifactId\tpackaging\n")
        for row in rows:
            output.write("\t".join(row) + "\n")
    (report / "poms-scanned.txt").write_text(
        "".join(rel(root, path) + "\n" for path in poms(root)), encoding="utf-8"
    )
    (report / "prepare-violations.txt").write_text(
        "".join("SCAN_FAILURE\t" + item + "\n" for item in failures), encoding="utf-8"
    )
    return 0


class Result:
    """集中记录违规和扫描计数。"""

    def __init__(self):
        self.bad = []
        self.metrics = {
            "pomFilesScanned": 0,
            "sourceFilesScanned": 0,
            "serviceFilesScanned": 0,
            "classFilesScanned": 0,
            "compiledResourceFilesScanned": 0,
            "classOutputDirectoriesScanned": 0,
            "artifactsScanned": 0,
            "artifactEntriesScanned": 0,
            "unreadableArtifacts": 0,
            "reactorModulesExpected": 0,
            "dependencyModulesCovered": 0,
        }

    def add(self, category, detail):
        self.bad.append((category, detail))


def scan_poms(root, report, result):
    """扫描所有 POM，覆盖 dependencyManagement、profile 和 plugin dependency。"""
    hits = []
    all_poms = poms(root)
    result.metrics["pomFilesScanned"] = len(all_poms)
    if not all_poms:
        result.add("SCAN_INCOMPLETE", "no project pom.xml files found")
    for path in all_poms:
        name = rel(root, path)
        try:
            data = path.read_bytes()
        except OSError as error:
            result.add("SCAN_FAILURE", "%s:%s" % (name, error))
            continue
        for token_name, token in TOKENS:
            offset = 0
            while True:
                offset = data.find(token, offset)
                if offset < 0:
                    break
                detail = "%s:%d:%s" % (name, data[:offset].count(b"\n") + 1, token_name)
                result.add("POM_COORDINATE", detail)
                hits.append(detail)
                offset += len(token)
    starter = root / "dec-core-starter" / "pom.xml"
    if starter.is_file():
        data = starter.read_bytes()
        for old in (b"dec-context-config-parse-xml", b"dec-context-config-parse-yaml"):
            if old in data:
                result.add("STARTER_DEPENDENCY", old.decode("ascii"))
    (report / "pom-scan.txt").write_text("\n".join(hits) + ("\n" if hits else ""), encoding="utf-8")


def source_scope(parts):
    joined = "/".join(parts)
    return "/src/main/" in "/" + joined + "/" or "/src/test/" in "/" + joined + "/" or "/META-INF/services/" in "/" + joined + "/"


def scan_sources(root, report, result):
    """扫描源码、资源、反射字符串和 ServiceLoader 内容。"""
    hits = []
    for path in sorted(root.rglob("*"), key=lambda item: str(item)):
        if not path.is_file():
            continue
        parts = path.relative_to(root).parts
        if any(part in {".git", "target", "project_doc"} for part in parts) or not source_scope(parts):
            continue
        name = rel(root, path)
        result.metrics["sourceFilesScanned"] += 1
        service = "/META-INF/services/" in "/" + "/".join(parts) + "/"
        if service:
            result.metrics["serviceFilesScanned"] += 1
        try:
            data = path.read_bytes()
        except OSError as error:
            result.add("SCAN_FAILURE", "%s:%s" % (name, error))
            continue
        for token_name, token in TOKENS:
            if token in data:
                category = "SERVICE_LOADER" if service else "SOURCE_REFERENCE"
                detail = "%s:%d:%s" % (name, line_of(data, token), token_name)
                result.add(category, detail)
                hits.append(category + "\t" + detail)
    (report / "source-scan.txt").write_text("\n".join(hits) + ("\n" if hits else ""), encoding="utf-8")


def scan_dependencies(report, result):
    """验证 Maven 追加输出包含全部 Reactor 模块，并生成显式模块标记。"""
    rows = []
    manifest = report / "reactor-modules.tsv"
    if manifest.is_file():
        for line in manifest.read_text(encoding="utf-8").splitlines()[1:]:
            fields = line.split("\t")
            if len(fields) == 4:
                rows.append(fields)
    result.metrics["reactorModulesExpected"] = len(rows)
    try:
        status = int((report / "dependency-tree.status").read_text().strip())
    except (OSError, ValueError):
        status = 127
    if status != 0:
        result.add("DEPENDENCY_TREE_GENERATION", "maven dependency:tree status %d" % status)
    try:
        lines = (report / "dependency-tree.raw.txt").read_text(encoding="utf-8", errors="replace").splitlines()
    except OSError as error:
        lines = []
        result.add("DEPENDENCY_TREE_GENERATION", str(error))
    expected = {group + ":" + artifact: row for row, group, artifact, _ in rows}
    covered, annotated = {}, []
    for number, line in enumerate(lines, 1):
        clean = re.sub(r"^\[INFO\]\s*", "", line.strip())
        for coordinate, row in expected.items():
            if clean.startswith(coordinate + ":"):
                annotated.append("===== MODULE %s (%s) =====" % (row, coordinate))
                covered.setdefault(coordinate, number)
                break
        annotated.append(line)
        if "dec-expand-declaration" in line:
            result.add("DEPENDENCY_TREE", "dependency-tree.raw.txt:%d:%s" % (number, line.strip()))
    (report / "dependency-tree.txt").write_text("\n".join(annotated) + ("\n" if annotated else ""), encoding="utf-8")
    result.metrics["dependencyModulesCovered"] = len(covered)
    modules = []
    for coordinate, row in expected.items():
        ok = coordinate in covered
        modules.append({"path": row, "coordinate": coordinate, "covered": ok, "firstRawLine": covered.get(coordinate)})
        if not ok:
            result.add("DEPENDENCY_TREE_INCOMPLETE", row + ":" + coordinate)
    (report / "dependency-tree-modules.json").write_text(
        json.dumps({"expectedModuleCount": len(rows), "coveredModuleCount": len(covered), "modules": modules}, ensure_ascii=False, indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
    )
    if not rows:
        result.add("SCAN_INCOMPLETE", "reactor module manifest is empty")


def class_roots(root):
    """枚举所有模块的 target/classes 与 target/test-classes。"""
    found = []
    for target in root.rglob("target"):
        if not target.is_dir() or "project_doc" in target.relative_to(root).parts:
            continue
        for child in (target / "classes", target / "test-classes"):
            if child.is_dir():
                found.append(child)
    return sorted(set(found), key=lambda path: rel(root, path))


def scan_compiled(root, report, result):
    """扫描 class 常量池和编译目录中的资源内容。"""
    hits = []
    roots = class_roots(root)
    result.metrics["classOutputDirectoriesScanned"] = len(roots)
    for base in roots:
        for path in sorted(base.rglob("*")):
            if not path.is_file():
                continue
            name = rel(root, path)
            is_class = path.suffix == ".class"
            key = "classFilesScanned" if is_class else "compiledResourceFilesScanned"
            result.metrics[key] += 1
            try:
                data = path.read_bytes()
            except OSError as error:
                result.add("SCAN_FAILURE", "%s:%s" % (name, error))
                continue
            for token_name in matches(data):
                if is_class:
                    category = "CLASS_CONSTANT_POOL"
                elif "/META-INF/services/" in "/" + name + "/":
                    category = "SERVICE_LOADER_COMPILED"
                else:
                    category = "COMPILED_RESOURCE"
                detail = name + ":" + token_name
                result.add(category, detail)
                hits.append(category + "\t" + detail)
    if result.metrics["classFilesScanned"] == 0:
        result.add("SCAN_INCOMPLETE", "no class files under target/classes or target/test-classes")
    (report / "compiled-scan.txt").write_text("\n".join(hits) + ("\n" if hits else ""), encoding="utf-8")


def scan_zip(name, archive, result, hits, depth=0):
    """递归扫描归档 entry 名称和解压后内容；无法读取时 fail-closed。"""
    if depth > 3:
        result.add("ARTIFACT_UNREADABLE", name + ":nested depth exceeded")
        result.metrics["unreadableArtifacts"] += 1
        return
    for info in archive.infolist():
        result.metrics["artifactEntriesScanned"] += 1
        entry = info.filename
        for token_name in matches(entry.encode("utf-8", errors="replace")):
            detail = "%s:%s:%s" % (name, entry, token_name)
            result.add("ARTIFACT_ENTRY", detail)
            hits.append("ARTIFACT_ENTRY\t" + detail)
        if info.is_dir():
            continue
        try:
            data = archive.read(info)
        except Exception as error:
            detail = "%s:%s:%s" % (name, entry, error)
            result.add("ARTIFACT_UNREADABLE", detail)
            hits.append("ARTIFACT_UNREADABLE\t" + detail)
            result.metrics["unreadableArtifacts"] += 1
            continue
        for token_name in matches(data):
            if entry.endswith(".class"):
                category = "ARTIFACT_CLASS_CONTENT"
            elif "/META-INF/services/" in "/" + entry:
                category = "SERVICE_LOADER_ARTIFACT"
            else:
                category = "ARTIFACT_RESOURCE_CONTENT"
            detail = "%s:%s:%s" % (name, entry, token_name)
            result.add(category, detail)
            hits.append(category + "\t" + detail)
        if Path(entry).suffix.lower() in ARCHIVES:
            try:
                with zipfile.ZipFile(io.BytesIO(data)) as nested:
                    scan_zip(name + "!" + entry, nested, result, hits, depth + 1)
            except Exception as error:
                detail = "%s!%s:%s" % (name, entry, error)
                result.add("ARTIFACT_UNREADABLE", detail)
                hits.append("ARTIFACT_UNREADABLE\t" + detail)
                result.metrics["unreadableArtifacts"] += 1


def scan_artifacts(root, report, result):
    """扫描 target 下 jar、war、zip 的路径、entry、class 和资源内容。"""
    hits, artifacts = [], []
    for path in root.rglob("*"):
        if not path.is_file() or path.suffix.lower() not in ARCHIVES:
            continue
        parts = path.relative_to(root).parts
        if "target" not in parts or "project_doc" in parts:
            continue
        try:
            path.resolve().relative_to(report.resolve())
            continue
        except ValueError:
            artifacts.append(path)
    artifacts.sort(key=lambda path: rel(root, path))
    result.metrics["artifactsScanned"] = len(artifacts)
    for path in artifacts:
        name = rel(root, path)
        for token_name in matches(name.encode("utf-8")):
            result.add("ARTIFACT_PATH", name + ":" + token_name)
        try:
            with zipfile.ZipFile(path) as archive:
                scan_zip(name, archive, result, hits)
        except Exception as error:
            detail = name + ":" + str(error)
            result.add("ARTIFACT_UNREADABLE", detail)
            hits.append("ARTIFACT_UNREADABLE\t" + detail)
            result.metrics["unreadableArtifacts"] += 1
    if not artifacts:
        result.add("SCAN_INCOMPLETE", "no jar, war or zip artifacts under target")
    (report / "artifact-scan.txt").write_text("\n".join(hits) + ("\n" if hits else ""), encoding="utf-8")


def finish(report, result):
    """按真实扫描计数生成 summary，禁止声明未执行的覆盖面。"""
    (report / "violations.txt").write_text(
        "".join(category + "\t" + detail + "\n" for category, detail in result.bad), encoding="utf-8"
    )
    categories = {category for category, _ in result.bad}
    metrics = result.metrics
    clean_scan = not categories.intersection({"SCAN_FAILURE", "SCAN_INCOMPLETE"})
    scopes = {
        "ALL_PROJECT_POMS": metrics["pomFilesScanned"] > 0 and clean_scan,
        "ALL_REACTOR_DEPENDENCY_TREES": metrics["reactorModulesExpected"] > 0 and metrics["reactorModulesExpected"] == metrics["dependencyModulesCovered"] and "DEPENDENCY_TREE_GENERATION" not in categories and "DEPENDENCY_TREE_INCOMPLETE" not in categories,
        "SOURCE_AND_REFLECTION_STRINGS": metrics["sourceFilesScanned"] > 0 and clean_scan,
        "SERVICE_LOADER_CONTENT": clean_scan,
        "CLASS_CONSTANT_POOLS": metrics["classFilesScanned"] > 0 and clean_scan,
        "PUBLISHED_ARTIFACT_ENTRIES_AND_CONTENT": metrics["artifactsScanned"] > 0 and metrics["unreadableArtifacts"] == 0 and clean_scan,
    }
    summary = {
        "task": "TASK-P1-T15",
        "iteration": "I002",
        "gate": "DECLARATION_RUNTIME_RETIREMENT",
        "result": "PASSED" if not result.bad else "FAILED",
        "violationCount": len(result.bad),
        "metrics": metrics,
        "scopeStatus": scopes,
        "scopes": [name for name, covered in scopes.items() if covered],
    }
    (report / "summary.json").write_text(json.dumps(summary, ensure_ascii=False, indent=2, sort_keys=True) + "\n", encoding="utf-8")


def scan(root, report):
    result = Result()
    prep = report / "prepare-violations.txt"
    if prep.is_file():
        for line in prep.read_text(encoding="utf-8").splitlines():
            category, _, detail = line.partition("\t")
            if line:
                result.add(category or "SCAN_FAILURE", detail or line)
    else:
        result.add("SCAN_FAILURE", "prepare-violations.txt missing")
    if (root / "dec-expand-declaration").exists():
        result.add("MODULE", "dec-expand-declaration directory exists")
    for name in (
        "dec-core-starter/src/main/java/dec/core/starter/common/ConfigUtil.java",
        "dec-core-starter/src/main/java/dec/core/starter/common/DataSourceManager.java",
    ):
        if (root / name).exists():
            result.add("STARTER_GLOBAL", name)
    scan_poms(root, report, result)
    scan_sources(root, report, result)
    scan_dependencies(report, result)
    scan_compiled(root, report, result)
    scan_artifacts(root, report, result)
    finish(report, result)
    return 0 if not result.bad else 1


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("command", choices=("prepare", "scan"))
    parser.add_argument("--root", required=True)
    parser.add_argument("--report-dir", required=True)
    args = parser.parse_args()
    root = Path(args.root).resolve()
    report = Path(args.report_dir).resolve()
    return prepare(root, report) if args.command == "prepare" else scan(root, report)


if __name__ == "__main__":
    sys.exit(main())
