#!/usr/bin/env bash
set -euo pipefail

python3 - <<'PY'
from pathlib import Path
src=Path('.common-develop-dev01-r03-raw-runner.sh').read_text()
start="skel=r'''# Add the fresh R02 architecture skeleton by replacing only the reviewed concrete seam."
end="\n'''\ns=s[:a]+skel+s[b:]"
if start not in src:
    raise SystemExit('skel opening quote anchor missing')
start_pos=src.index(start)
end_pos=src.index(end,start_pos)
src=src[:start_pos]+src[start_pos:].replace("skel=r'''",'skel=r"""',1)
# Recompute end after opening replacement.
end_pos=src.index(end,start_pos)
src=src[:end_pos]+'\n"""\ns=s[:a]+skel+s[b:]'+src[end_pos+len(end):]
Path('/tmp/dev01-r03-raw-fixed.sh').write_text(src)
PY
chmod +x /tmp/dev01-r03-raw-fixed.sh
exec /tmp/dev01-r03-raw-fixed.sh
