#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
TEST_DIR="$ROOT/dec-core-context/src/test/java/dec/core/context/gate"
TEST_FILE="$TEST_DIR/P0IntentionalFailureTest.java"
cleanup() { rm -f "$TEST_FILE"; rmdir "$TEST_DIR" 2>/dev/null || true; }
trap cleanup EXIT HUP INT TERM
mkdir -p "$TEST_DIR"
cat > "$TEST_FILE" <<'JAVA'
package dec.core.context.gate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.fail;
class P0IntentionalFailureTest { @Test void mustFail() { fail("P0 failure-gate proof"); } }
JAVA
set +e
"$ROOT/mvnw" --batch-mode --no-transfer-progress -pl dec-core-context -Dtest=P0IntentionalFailureTest test
status=$?
set -e
if [ "$status" -eq 0 ]; then
  echo "ERROR: Maven returned success for an intentionally failing test" >&2
  exit 1
fi
echo "P0 failure gate proved: failing test returned status $status"
