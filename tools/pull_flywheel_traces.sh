#!/bin/sh
# Pulls Flywheel Trace takes off the robot over adb and builds a viewer page with them inside it.
# Usage: tools/pull_flywheel_traces.sh            (all sessions)
#        tools/pull_flywheel_traces.sh 1021_1530  (only takes whose name starts with this)
set -e
cd "$(dirname "$0")"
OUT=flywheel_traces
mkdir -p "$OUT"
adb pull /sdcard/FIRST/flywheel_trace/. "$OUT/" >/dev/null
python3 - "$OUT" "${1:-}" <<'EOF'
import json, os, sys
out, prefix = sys.argv[1], sys.argv[2]
data = {}
for f in sorted(os.listdir(out)):
    if f.endswith('.csv') and f.startswith(prefix):
        with open(os.path.join(out, f)) as fh:
            data[f[:-4]] = fh.read()
html = open('flywheel-trace-viewer.html').read()
marker = '<script id="embedded-traces" type="application/json">{}</script>'
blob = json.dumps(data).replace('</', '<\\/')
page = html.replace(marker, '<script id="embedded-traces" type="application/json">' + blob + '</script>')
with open(os.path.join(out, 'report.html'), 'w') as fh:
    fh.write(page)
print(f'{len(data)} takes -> {out}/report.html')
EOF
