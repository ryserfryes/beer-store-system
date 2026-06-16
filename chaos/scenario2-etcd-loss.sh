#!/usr/bin/env bash
set -euo pipefail

PUMBA_IMAGE="ghcr.io/alexei-led/pumba:latest"
PAUSE_DURATION="${PAUSE_DURATION:-90s}"

log() { echo "[$(date '+%H:%M:%S')] $*"; }

pumba() {
  MSYS_NO_PATHCONV=1 docker run --rm \
    -v /var/run/docker.sock:/var/run/docker.sock \
    "$PUMBA_IMAGE" "$@"
}

get_primary() {
  for node in pg-node1 pg-node2 pg-node3; do
    role=$(docker exec "$node" curl -sf http://localhost:8008/patroni 2>/dev/null \
      | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('role',''))" 2>/dev/null || echo "")
    if [ "$role" = "master" ] || [ "$role" = "primary" ]; then
      echo "$node"; return
    fi
  done
  echo ""
}

check_etcd_quorum() {
  local up=0
  for node in etcd1 etcd2 etcd3; do
    state=$(docker inspect --format='{{.State.Status}}' "$node" 2>/dev/null || echo "missing")
    [ "$state" = "running" ] && up=$((up + 1))
  done
  echo "$up"
}

log "=== Scenario 2: etcd Quorum Loss (Pumba pause ${PAUSE_DURATION}) ==="

log "Pulling Pumba image..."
docker pull "$PUMBA_IMAGE" -q

PRIMARY=$(get_primary)
log "Current primary: ${PRIMARY:-NONE}"
log "etcd nodes running before test: $(check_etcd_quorum)/3"

log "Cluster state before:"
MSYS_NO_PATHCONV=1 docker exec "${PRIMARY:-pg-node1}" patronictl -c /etc/patroni/patroni.yml list 2>/dev/null || true

log ""
log "=== Injecting failure: pumba pause --duration ${PAUSE_DURATION} etcd2 etcd3 ==="
pumba pause --duration "$PAUSE_DURATION" etcd2 etcd3 &
PUMBA_PID=$!
log "Pumba pause started (PID $PUMBA_PID), auto-resumes in ${PAUSE_DURATION}"

log ""
log "Monitoring Patroni behaviour (every 5s)..."
ELAPSED=0
while kill -0 "$PUMBA_PID" 2>/dev/null; do
  sleep 5
  ELAPSED=$((ELAPSED + 5))
  NEW_PRIMARY=$(get_primary)
  PATRONI_STATUS=$(docker exec "${PRIMARY:-pg-node1}" curl -sf http://localhost:8008/patroni 2>/dev/null \
    | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('state','?'), d.get('role','?'))" 2>/dev/null || echo "unreachable")
  ETCD_UP=$(check_etcd_quorum)
  log "  t+${ELAPSED}s | etcd_up=${ETCD_UP}/3 | primary=${NEW_PRIMARY:-NONE} | patroni=${PATRONI_STATUS}"
done

log ""
log "Pumba finished — etcd2, etcd3 auto-unpaused"
log ""
log "Monitoring Patroni recovery (every 5s for 60s)..."
for i in $(seq 1 12); do
  sleep 5
  NEW_PRIMARY=$(get_primary)
  ETCD_UP=$(check_etcd_quorum)
  log "  t+$((i * 5))s | etcd_up=${ETCD_UP}/3 | primary=${NEW_PRIMARY:-NONE}"
  if [ -n "$NEW_PRIMARY" ]; then
    log "  Cluster recovered: primary is $NEW_PRIMARY"
    break
  fi
done

log ""
log "Final cluster state:"
for n in pg-node1 pg-node2 pg-node3; do
  docker exec "$n" curl -sf http://localhost:8008/health >/dev/null 2>&1 && \
    MSYS_NO_PATHCONV=1 docker exec "$n" patronictl -c /etc/patroni/patroni.yml list && break
done

log "=== Scenario 2 complete ==="
