#!/bin/sh
set -eu
BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROPS="$BASE_DIR/.mvn/wrapper/maven-wrapper.properties"
DIST_URL=$(sed -n 's/^distributionUrl=//p' "$PROPS" | tail -1)
MAVEN_VERSION=$(printf '%s' "$DIST_URL" | sed -n 's#.*apache-maven-\([^/]*\)-bin\.zip#\1#p')
[ -n "$MAVEN_VERSION" ] || { echo "Cannot determine Maven version from $PROPS" >&2; exit 1; }
MAVEN_HOME="${MAVEN_USER_HOME:-$HOME/.m2}/wrapper/dists/apache-maven-$MAVEN_VERSION"
MVN="$MAVEN_HOME/bin/mvn"
if [ ! -x "$MVN" ]; then
  command -v unzip >/dev/null 2>&1 || { echo "unzip is required by Maven Wrapper" >&2; exit 1; }
  TMP_DIR=$(mktemp -d 2>/dev/null || mktemp -d -t mvnw)
  trap 'rm -rf "$TMP_DIR"' EXIT HUP INT TERM
  ARCHIVE="$TMP_DIR/maven.zip"
  if command -v curl >/dev/null 2>&1; then curl -fsSL "$DIST_URL" -o "$ARCHIVE"
  elif command -v wget >/dev/null 2>&1; then wget -q "$DIST_URL" -O "$ARCHIVE"
  else echo "curl or wget is required by Maven Wrapper" >&2; exit 1; fi
  mkdir -p "$(dirname "$MAVEN_HOME")"
  unzip -q "$ARCHIVE" -d "$TMP_DIR/unpack"
  rm -rf "$MAVEN_HOME"
  mv "$TMP_DIR/unpack/apache-maven-$MAVEN_VERSION" "$MAVEN_HOME"
fi
exec "$MVN" -f "$BASE_DIR/pom.xml" "$@"
