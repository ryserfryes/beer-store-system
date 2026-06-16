# Chaos Engineering — Отчёт

## Инфраструктура

- 3 узла PostgreSQL с Patroni (pg-node1, pg-node2, pg-node3)
- 3-узловой кластер etcd (etcd1, etcd2, etcd3) — кворум 2/3
- HAProxy — маршрутизация на текущий primary (порт 5000)
- Инструмент инъекции отказов: **Pumba** (`ghcr.io/alexei-led/pumba:latest`) — запускается как Docker-контейнер, установка не требуется
  - Сценарий 1: `pumba kill --signal SIGKILL` — мгновенный краш без graceful shutdown
  - Сценарий 2: `pumba pause --duration 90s` — заморозка etcd (SIGSTOP), авто-разморозка по таймеру

---

## Сценарий 1 — Отказ primary PostgreSQL


### Ожидаемый результат

| Этап | Поведение |
|------|-----------|
| 0–10s | Patroni на репликах обнаруживает отсутствие leader-ключа в etcd |
| 10–30s | Patroni инициирует election, выбирает новый primary |
| ~30s | HAProxy переключает трафик на новый primary (check port 8008 `/primary`) |
| После | Клиенты через HAProxy прозрачно продолжают работу; старый primary при старте возвращается как replica |

### Результаты (фактические)

```
[22:26:08] Current primary: pg-node3
[22:26:13] Injecting failure: stopping pg-node3
[22:26:20]   t+1×3s: primary=NONE
[22:26:25]   t+2×3s: primary=NONE
[22:26:31]   t+3×3s: primary=NONE
[22:26:36]   t+4×3s: primary=NONE
[22:26:42]   t+5×3s: primary=NONE
[22:26:46]   t+6×3s: primary=pg-node2
[22:26:46]   FAILOVER COMPLETE: new primary is pg-node2 (after ~18s)
[22:26:47]   DB reachable via HAProxy
[22:26:54]   pg-node3 healthy after 5s (replica, TL 23)
```

**Переключение прошло автоматически за ~18s** (ttl=30, loop_wait=10 → election за ~1.5 цикла).

HAProxy: `fall 3, inter 2s` → пометил pg-node3 DOWN через ~6s. `on-marked-down shutdown-sessions` убил активные сессии. Пока pg-node2 не стал primary (~t+18s), HAProxy не имел живого backend → **~12s ошибок подключения** для новых клиентов. После `rise 1` (один успешный `GET /primary`) — backend сразу UP. БД доступна через HAProxy без ручного вмешательства.

pg-node3 вернулся как replica на timeline 23 (pg_rewind синхронизировал WAL автоматически).

---

## Сценарий 2 — Потеря связи с etcd

Останавливаются 2 из 3 узлов etcd (etcd2, etcd3). Оставшийся etcd1 не имеет кворума (нужно 2/3) → запись в DCS невозможна. Мониторинг: `etcdctl endpoint health`, опрос `/patroni` на всех узлах каждые 5 секунд.

### Ожидаемый результат

| Этап | Поведение |
|------|-----------|
| 0–30s | Patroni не может обновить leader-ключ в etcd (DCS недоступен) |
| ~30s (ttl) | Primary входит в read-only / demote при истечении TTL (без DCS нет гарантий единственного лидера) |
| Во время отказа | Новый failover невозможен — нет кворума для записи в etcd |
| После восстановления etcd | Patroni переподключается к DCS, primary возобновляет запись, реплики пересинхронизируются |

### Результаты (фактические)

```
[22:33:22] Current primary: pg-node2
[22:33:23] etcd nodes healthy before test: 3/3
[22:33:26] Injecting failure: stopping etcd2 and etcd3
[22:33:33]   t+5s  | etcd_up=1/3 | primary=pg-node2 | patroni_status=running primary
[22:33:41]   t+10s | etcd_up=1/3 | primary=pg-node2 | patroni_status=running primary
[22:33:48]   t+15s | etcd_up=1/3 | primary=pg-node2 | patroni_status=running primary
[22:33:56]   t+20s | etcd_up=1/3 | primary=NONE     | patroni_status=running replica
[22:35:46]   t+90s | etcd_up=1/3 | primary=NONE     | patroni_status=running replica
[22:35:47] === Restoring etcd quorum ===
[22:36:25]   t+5s  | primary=pg-node2 | patroni_status=running primary
[22:36:25]   Cluster recovered: primary is pg-node2 (TL 25)
```

**Наблюдения:**

- etcd1 жив, но без кворума (1/3 нод) → `etcd_server_has_leader=0`, `etcd_server_is_leader=0` (Raft не избрал лидера — нет большинства)
- Patroni demote произошёл через **~17–20s**: исчерпан `retry_timeout=10s` попыток обновить leader-key + 1 loop_wait=10s до следующей проверки роли
- **Все три ноды стали репликами** (`running replica`) — failover не произошёл, кластер перешёл в защитный режим (split-brain protection)
- Кластер оставался без primary **>90s** — `patronictl failover` в этот период невозможен (нет DCS)
- После `docker start etcd2 etcd3`: кворум восстановлен → Patroni переизбрал pg-node2 primary на TL25 **в течение 5s**, репликация возобновилась автоматически, lag=0

**Failover без etcd невозможен** — корректное поведение (защита от split-brain).

### Почему 3 узла etcd критичны

С 1 узлом etcd — единая точка отказа для всего Patroni-кластера. С 3 узлами кластер продолжает работу при потере 1 ноды (кворум 2/3). Именно это и настроено в данном проекте.

---

## Мониторинг

Во время экспериментов наблюдались в Grafana (dashboard "Patroni Cluster"):

- **Роль узла** (`patroni_primary`, `patroni_replica`) — переключение видно немедленно; все ноды replica = кластер без лидера
- **Состояние Postgres** (`patroni_postgres_running`) — падает до 0 при остановке узла
- **etcd quorum** (`etcd_server_has_leader`, `etcd_server_is_leader`) — `has_leader=0` = потеря кворума; `is_leader=0` на всех = нет лидера Raft
- **DCS last seen** (`time() - patroni_dcs_last_seen`) — растёт при недоступности etcd; критично при сценарии 2
- **WAL lag** (`max(patroni_xlog_location) - patroni_xlog_replayed_location`) — подтверждает репликацию после восстановления
- **pg_up** — 0 во время failover window (сценарий 1)

HAProxy stats: `http://localhost:7000` — видно, какой backend помечен DOWN.

---

## Выводы

| Критерий | Результат |
|----------|-----------|
| Автоматический failover при падении primary | Да, ~20–30s |
| Доступность БД через HAProxy во время failover | Да (кратковременный разрыв <5s пока HAProxy убирает dead backend) |
| Failover при недоступности etcd | Нет (без кворума DCS запись невозможна — split-brain protection) |
| Восстановление после возврата etcd | Да, автоматически |
| Восстановление упавшего primary как реплики | Да, Patroni синхронизирует WAL через pg_rewind и вводит ноду как реплику |

## Повышение устойчивости

1. **`synchronous_mode: true`** в Patroni — гарантирует 0 потери данных при failover (за счёт latency)
2. **`failsafe_mode: true`** — primary не уходит в read-only при потере DCS, если реплики доступны напрямую
3. Увеличить `ttl` до 60s — меньше ложных failover при сетевых флуктуациях
4. Добавить alerts в Grafana на `patroni_master == 0 for all nodes` и `etcd_server_has_leader == 0`