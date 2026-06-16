#!/usr/bin/env python3
import http.server
import os

STATE_FILE = "/tmp/backup_state"

def get_body():
    if not os.path.exists(STATE_FILE):
        return ""
    with open(STATE_FILE) as f:
        lines = f.read().strip().split("\n")
    if len(lines) < 2:
        return ""
    return (
        "# HELP backup_last_timestamp_seconds Unix timestamp of last successful backup\n"
        "# TYPE backup_last_timestamp_seconds gauge\n"
        f"backup_last_timestamp_seconds {lines[0]}\n"
        "# HELP backup_last_size_bytes Size in bytes of last backup\n"
        "# TYPE backup_last_size_bytes gauge\n"
        f"backup_last_size_bytes {lines[1]}\n"
    )

class H(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        body = get_body().encode()
        self.send_response(200)
        self.send_header("Content-Type", "text/plain; version=0.0.4")
        self.end_headers()
        self.wfile.write(body)
    def log_message(self, *args):
        pass

http.server.HTTPServer(("0.0.0.0", 9091), H).serve_forever()
