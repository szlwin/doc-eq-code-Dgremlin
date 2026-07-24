#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

if [ -f "$HOME/.bash_profile" ]; then
  # shellcheck disable=SC1090
  source "$HOME/.bash_profile"
fi

target="${1:-all}"

install_yaml_parser() {
  mvn -pl dec-context-config-parse-yaml -am -DskipTests install
}

install_declaration() {
  mvn -pl dec-expand-declaration -am -DskipTests install
}

run_main() {
  main_class="$1"
  mvn -f dec-demo/pom.xml -q -DskipTests compile exec:java -Dexec.mainClass="$main_class"
}

case "$target" in
  all)
    install_yaml_parser
    install_declaration
    run_main dec.demo.model.RuleTests
    run_main dec.demo.system.OrderTest
    run_main dec.demo.declaration.TestOrderBusiness
    run_main dec.demo.directory.DirectoryTest
    ;;
  rule)
    install_yaml_parser
    run_main dec.demo.model.RuleTests
    ;;
  order)
    install_yaml_parser
    run_main dec.demo.system.OrderTest
    ;;
  declaration)
    install_declaration
    run_main dec.demo.declaration.TestOrderBusiness
    ;;
  directory)
    install_yaml_parser
    run_main dec.demo.directory.DirectoryTest
    ;;
  *)
    echo "Usage: $0 [all|rule|order|declaration|directory]" >&2
    exit 2
    ;;
esac
