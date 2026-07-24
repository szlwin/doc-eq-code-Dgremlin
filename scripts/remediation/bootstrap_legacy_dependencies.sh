#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
install_jar() {
  group=$1; artifact=$2; version=$3; jar=$4
  [ -f "$ROOT/lib/$jar" ] || { echo "Missing legacy jar: lib/$jar" >&2; exit 1; }
  "$ROOT/mvnw" --batch-mode --no-transfer-progress -N \
    org.apache.maven.plugins:maven-install-plugin:3.1.3:install-file \
    -Dfile="$ROOT/lib/$jar" -DgroupId="$group" -DartifactId="$artifact" \
    -Dversion="$version" -Dpackaging=jar -DgeneratePom=true
}
install_jar smarter express-check 1.0 express-check-1.0.jar
install_jar smarter easy-check 1.0 easy-check-1.0.jar
install_jar smarter santr 2.0.3 santr-2.0.3.jar
install_jar javolution javolution 6.1.0 javolution-6.1.0.jar
