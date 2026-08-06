#!/usr/bin/env python3
"""验证 dec-demo MySQL 业务测试报告完整且没有被空跑。"""

from __future__ import annotations

import json
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

REQUIRED_CLASSES = {
    "dec.demo.model.RuleTests",
    "dec.demo.directory.DirectoryTest",
    "dec.demo.system.OrderTest",
}


def suite_metrics(path: Path) -> tuple[str, dict[str, int]]:
    """读取一个 Surefire XML，并返回测试类名及计数。"""
    root = ET.parse(path).getroot()
    name = root.attrib.get("name", "")
    metrics = {
        key: int(float(root.attrib.get(key, "0")))
        for key in ("tests", "failures", "errors", "skipped")
    }
    return name, metrics


def main() -> int:
    """对必需业务场景执行 fail-closed 验证并写出机器摘要。"""
    report_dir = Path("dec-demo/target/surefire-reports")
    output_dir = Path("target/dec-demo-mysql-it")
    output_dir.mkdir(parents=True, exist_ok=True)

    discovered: dict[str, dict[str, int]] = {}
    if report_dir.is_dir():
        for path in sorted(report_dir.glob("TEST-*.xml")):
            name, metrics = suite_metrics(path)
            if name in REQUIRED_CLASSES:
                discovered[name] = metrics

    missing = sorted(REQUIRED_CLASSES - discovered.keys())
    totals = {
        key: sum(metrics[key] for metrics in discovered.values())
        for key in ("tests", "failures", "errors", "skipped")
    }
    status = (
        "PASSED"
        if not missing
        and totals["tests"] >= len(REQUIRED_CLASSES)
        and totals["failures"] == 0
        and totals["errors"] == 0
        and totals["skipped"] == 0
        else "FAILED"
    )
    summary = {
        "status": status,
        "requiredClasses": sorted(REQUIRED_CLASSES),
        "discovered": discovered,
        "missingClasses": missing,
        "totals": totals,
    }
    output = output_dir / "summary.json"
    output.write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(summary, ensure_ascii=False, indent=2))
    return 0 if status == "PASSED" else 1


if __name__ == "__main__":
    sys.exit(main())
