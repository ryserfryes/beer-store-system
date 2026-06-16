#!/usr/bin/env bash
set -euo pipefail

PUMBA_IMAGE="ghcr.io/alexei-led/pumba:latest"
PATRONI_CFG="/etc/patroni/patroni.yml"

log() { echo "[$(date '+%H:%M:%S')] $*"; }

pumba() {
  MSYS_NO_PATHCONV=1 docker run --rm \
    -v /var/run/docker.sock:/var/run/docker.sock \
    "$PUMBA_IMAGE" "$@"
}

dexec() {
  MSYS_NO_PATHCONV=1 docker exec "$@"
}

get_primary() {
  for node in pg-node1 pg-node2 pg-node3; do
    role=$(dexec "$node" curl -sf http://localhost:8008/patroni 2>/dev/null \
      | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('role',''))" 2>/dev/null || echo "")
    if [ "$role" = "master" ] || [ "$role" = "primary" ]; then
      echo "$node"; return
    fi
  done
  echo ""
}

log "=== Scenario 1: Primary PostgreSQL Failure (Pumba kill SIGKILL) ==="

log "Pulling Pumba image..."
docker pull "$PUMBA_IMAGE" -q

PRIMARY=$(get_primary)
if [ -z "$PRIMARY" ]; then
  log "ERROR: Could not detect primary node. Is the cluster running?"
  exit 1
fi
log "Current primary: $PRIMARY"

log "Cluster state before failure:"
MSYS_NO_PATHCONV=1 docker exec "$PRIMARY" patronictl -c "$PATRONI_CFG" list 2>/dev/null || true

log ""
log "=== Injecting failure: pumba kill --signal SIGKILL $PRIMARY ==="
pumba kill --signal SIGKILL "$PRIMARY"
log "SIGKILL sent to $PRIMARY at $(date '+%H:%M:%S')"

log ""
log "Monitoring failover (polling every 3s for 60s)..."
FAILOVER_TIME=""
for i in $(seq 1 20); do
  sleep 3
  NEW_PRIMARY=$(get_primary)
  log "  t+$((i * 3))s: primary=${NEW_PRIMARY:-NONE}"
  if [ -n "$NEW_PRIMARY" ] && [ "$NEW_PRIMARY" != "$PRIMARY" ]; then
    FAILOVER_TIME=$((i * 3))
    log "  FAILOVER COMPLETE: new primary is $NEW_PRIMARY (after ~${FAILOVER_TIME}s)"
    break
  fi
done

if [ -z "${FAILOVER_TIME:-}" ]; then
  log "WARNING: No new primary detected within 60s"
fi

log ""
log "=== Testing DB availability via HAProxy (port 5000) ==="
ALIVE_NODE=""
for n in pg-node1 pg-node2 pg-node3; do
  [ "$n" = "$PRIMARY" ] && continue
  dexec "$n" curl -sf http://localhost:8008/health >/dev/null 2>&1 && ALIVE_NODE="$n" && break
done
if [ -n "$ALIVE_NODE" ]; then
  HAPROXY_CONN="postgresql://craft_beer_user:craft_beer_password@haproxy:5000/craft_beer_store?sslmode=disable"
  for i in $(seq 1 5); do
    if dexec "$ALIVE_NODE" psql "$HAPROXY_CONN" -c "SELECT 'alive' AS status, now();" 2>/dev/null; then
      log "DB reachable via HAProxy"; break
    fi
    log "  attempt $i: not yet, waiting 5s..."; sleep 5
  done
fi

log ""
log "Waiting for $PRIMARY to restart as replica (up to 90s)..."
RECOVERED=false
for i in $(seq 1 18); do
  sleep 5
  if ! docker inspect --format='{{.State.Running}}' "$PRIMARY" 2>/dev/null | grep -q true; then
    log "  t+$((i * 5))s: $PRIMARY container not running — starting it..."
    docker start "$PRIMARY" 2>/dev/null || true
  fi
  if dexec "$PRIMARY" curl -sf http://localhost:8008/health >/dev/null 2>&1; then
    log "  $PRIMARY healthy after $((i * 5))s"
    RECOVERED=true
    break
  fi
  log "  t+$((i * 5))s: $PRIMARY not yet healthy..."
done

if [ "$RECOVERED" = false ]; then
  log "WARNING: $PRIMARY did not recover within 90s"
fi

log ""
log "Final cluster state:"
for n in pg-node1 pg-node2 pg-node3; do
  MSYS_NO_PATHCONV=1 docker exec "$n" curl -sf http://localhost:8008/health >/dev/null 2>&1 && \
    MSYS_NO_PATHCONV=1 docker exec "$n" patronictl -c "$PATRONI_CFG" list && break
done 2>/dev/null || log "WARNING: could not get cluster state"

log "=== Scenario 1 complete ==="
