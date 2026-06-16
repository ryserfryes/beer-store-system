# Craft Beer Store Platform

Бэкенд-платформа на микросервисной архитектуре для управления магазином крафтового пива: каталог товаров, корзина, оформление заказов, складской учёт, закупки у поставщиков и отзывы покупателей.

## Технологии

- **Язык**: Kotlin, Java 25
- **Фреймворк**: Spring Boot 4, Spring Data JPA (Hibernate 7)
- **Сборка**: Gradle
- **СУБД**: PostgreSQL 16, Liquibase
- **Брокер сообщений**: Apache Kafka 3.8
- **Межсервисное взаимодействие**: gRPC + Protobuf
- **HA-кластер базы данных**: Patroni (3 ноды), etcd, HAProxy
- **CDC**: Debezium $\rightarrow$ Kafka $\rightarrow$ ClickHouse
- **Аналитика**: ClickHouse + Metabase
- **Мониторинг**: Prometheus + Grafana + postgres-exporter
- **Резервное копирование**: MinIO (S3-совместимое хранилище)

## Ключевые бизнес-правила на уровне БД

- У одного покупателя ровно одна корзина (`carts.customer_id` уникален)
- Один SKU — одна строка в корзине (`cart_items(cart_id, variant_id)` уникален)
- Один отзыв на одно пиво от одного покупателя (`reviews(customer_id, beer_id)` уникален)
- Складской учёт ведётся по партиям (лотам): срок годности и закупочная цена хранятся на уровне партии
- Каждый заказ получает уникальный код получения (`pickup_code`)
- Оставить отзыв можно только после факта получения заказа

## Доменные концепты

Система разделяет несколько уровней абстракции, которые легко спутать:

**Beer (пиво)** — карточка продукта: название, пивоварня, стиль, крепость. Не продаётся напрямую.

**ProductVariant (SKU)** — конкретный вариант фасовки пива: пиво + тип упаковки + объём. Именно SKU имеет цену и статус активности. Один и тот же `Beer` может иметь несколько `ProductVariant` (например, бутылка 0.33 и банка 0.5).

**StockBatch (партия/лот)** — физическая партия конкретного SKU на конкретном складе. Хранит `lot_code`, срок годности, закупочную стоимость и количество остатка. Один SKU может иметь несколько активных партий с разными сроками годности.

**CartItem** — позиция в корзине. Ссылается только на SKU и хранит желаемое количество. Конкретная партия на этапе корзины не определена.

**OrderItem** — позиция оформленного заказа. Ссылается на SKU, на конкретную партию (`batch_id`) и на `beer_id` (денормализовано для аналитики). Партия фиксируется в момент резервирования стока при оформлении заказа. Цена (`unit_price`) фиксируется на момент заказа.

**PurchaseOrderItem** — позиция заказа на закупку у поставщика. После приёмки создаёт новую `StockBatch` на складе.

```
Beer
 └── ProductVariant (SKU)  ←── CartItem
          └── StockBatch   ←── OrderItem (batch_id зафиксирован)
                           ←── PurchaseOrderItem (создаёт новый StockBatch)
```

## Пайплайн заказа

### 1. Просмотр каталога
Покупатель получает список пива разделенный на страницы или карточку конкретного пива с вариантами фасовки (SKU). Каждый просмотр карточки фиксируется в `product_views`.

### 2. Корзина
Покупатель добавляет варианты продукта в корзину. У каждого покупателя ровно одна корзина. Повторное добавление того же SKU увеличивает количество, не создаёт новую строку.

### 3. Оформление заказа `POST /api/orders`
При оформлении заказа выполняются следующие проверки по порядку:

1. **Покупатель существует** — иначе `404 NotFoundException`
2. **Корзина существует** — иначе `404 NotFoundException`
3. **Корзина не пуста** — иначе `422 EmptyCartException`
4. **Все варианты из корзины есть в каталоге** (gRPC → catalog-service) — иначе `500` (ошибка зависимости)
5. **Достаточно товара на складе** (gRPC → warehouse-service `CheckStock`) — иначе `422 OutOfStockException` с указанием недостающих позиций
6. Заказ сохраняется со статусом `PENDING`
7. **Резервирование стока** (gRPC → warehouse-service `ReserveStock`) — партии (лоты) привязываются к позициям заказа; если резервация не прошла: `422 OutOfStockException`
8. Корзина очищается
9. В outbox записывается событие `orders.placed` → warehouse-service обновляет остатки

### 4. Обработка заказа (сотрудники)
Сотрудники продвигают заказ по статусам через `POST /api/orders/{id}/advance`:

```
PENDING -> PAID -> ASSEMBLING -> READY_FOR_PICKUP -> PICKED_UP
```

- Попытка продвинуть заказ в терминальном статусе (`PICKED_UP`, `CANCELED`, `EXPIRED`) — `422 OrderInTerminalStateException`
- Отмена через `POST /api/orders/{id}/cancel` доступна из любого нетерминального статуса

При переходе в `READY_FOR_PICKUP` фиксируется время готовности и срок хранения (по умолчанию 3 дня). При переходе в `PICKED_UP` в outbox записывается событие `orders.picked-up`.

### 5. Получение заказа и отзыв
После перехода в `PICKED_UP` событие `orders.picked-up` поступает в review-service. Покупатель получает право оставить отзыв на каждое пиво из заказа. Попытка оставить отзыв без факта получения — `403` (review-service проверяет через gRPC → order-service `HasCustomerPurchased`). Повторный отзыв на то же пиво — `409 ConflictException`.

## Архитектура

```
                        ┌─────────────┐
                        │ API Gateway │ :8080
                        └──────┬──────┘
           ┌───────────┬───────┼────────────┬────────────┐
           ▼           ▼       ▼            ▼            ▼
      catalog      warehouse  supply      order       review
      :8081         :8082     :8083       :8084        :8085
    (gRPC:9091)  (gRPC:9095)           (gRPC:9094)
```

**Синхронное взаимодействие**: REST (клиент $\rightarrow$ gateway $\rightarrow$ сервис) и gRPC (сервис-сервис)
**Асинхронное взаимодействие**: Kafka через паттерн transactional outbox
**База данных**: Единый кластер PostgreSQL 16 (Patroni, 3 ноды) с миграциями Liquibase

### Сервисы

| Сервис | REST | gRPC | Ответственность |
|---|---|---|---|
| api-gateway | 8080 | - | HTTP-прокси, маршрутизация ко всем сервисам |
| catalog-service | 8081 | 9091 | Пиво, пивоварни, стили, варианты продукта (SKU) |
| warehouse-service | 8082 | 9095 | Складские партии (лоты), точки самовывоза |
| supply-service | 8083 | - | Поставщики, заказы на закупку, приёмка товара |
| order-service | 8084 | 9094 | Покупатели, корзины, заказы, сотрудники |
| review-service | 8085 | - | Отзывы (только для покупателей с фактом получения) |

### Kafka-события

Сервисы записывают события в outbox-таблицу в рамках транзакции; планировщик публикует их в Kafka.

| Топик | Издатель | Потребитель | Когда |
|---|---|---|---|
| `orders.placed` | order-service | warehouse-service | Покупатель оформил заказ |
| `orders.picked-up` | order-service | review-service | Покупатель получил заказ |
| `supply.received` | supply-service | warehouse-service | Приёмка закупки на склад |

## Запуск

### Требования

- Docker и Docker Compose
- Свободные порты: `8080-8085`, `5000`, `9092`, `9090`, `3000`, `3001`, `8088`, `8123`, `9001`

### Старт

```bash
docker compose up -d
```

При первом запуске контейнер `migrator` выполняет все Liquibase-миграции, контейнер `seeder` загружает начальные данные каталога.

### Остановка

```bash
docker compose down
```

### Пересборка сервиса после изменений кода

```bash
docker compose build <service-name> && docker compose up -d <service-name>
```

## API

Все запросы направляются через gateway: `http://localhost:8080`.

### Каталог

```
GET  /api/catalog                          Список пива с пагинацией (?page=0&size=20)
GET  /api/catalog/beers/{id}               Карточка пива со всеми вариантами
GET  /api/catalog/variants/{id}            Один вариант продукта
GET  /api/catalog/styles                   Все стили пива
GET  /api/catalog/breweries                Все пивоварни
POST /api/catalog/variants/{id}/view       Зафиксировать просмотр
```

### Покупатели

```
POST /api/customers                        Регистрация покупателя
GET  /api/customers/{id}                   Профиль покупателя
```

### Корзина

```
GET    /api/customers/{id}/cart                        Получить корзину
POST   /api/customers/{id}/cart/items                  Добавить товар  { variantId, quantity }
PUT    /api/customers/{id}/cart/items/{variantId}      Изменить количество  { quantity }
DELETE /api/customers/{id}/cart/items/{variantId}      Удалить позицию
DELETE /api/customers/{id}/cart                        Очистить корзину
```

### Заказы

```
POST /api/orders                   Оформить заказ из корзины  { customerId, pickupPointId }
GET  /api/orders/{id}              Получить заказ
GET  /api/customers/{id}/orders    История заказов покупателя
POST /api/orders/{id}/advance      Перевести на следующий статус  { "employeeId": 1 }
POST /api/orders/{id}/cancel       Отменить заказ  { "employeeId": 1 }
```

**Жизненный цикл заказа:**
```
PENDING -> PAID -> ASSEMBLING -> READY_FOR_PICKUP -> PICKED_UP
    └─────────────────────────────────────┘
                    CANCELED
```

Терминальные статусы: `PICKED_UP`, `CANCELED`, `EXPIRED`

### Склад

```
GET /api/pickup-points             Список точек самовывоза
GET /api/warehouses                Список складов
```

### Закупки

```
GET  /api/suppliers                        Список поставщиков
POST /api/suppliers                        Создать поставщика
GET  /api/purchase-orders                  Список заказов на закупку
POST /api/purchase-orders                  Создать заказ на закупку
GET  /api/purchase-orders/{id}             Заказ на закупку с позициями
POST /api/purchase-orders/{id}/items       Добавить позицию в заказ
PUT  /api/purchase-orders/{id}/status      Обновить статус заказа
```

### Отзывы

```
GET  /api/reviews?beerId={id}      Отзывы о конкретном пиве
POST /api/reviews                  Оставить отзыв (только после получения заказа)
```

### Сотрудники

```
GET  /api/employees                Список сотрудников
POST /api/employees                Создать сотрудника
```

## Коды ошибок

| HTTP-статус | Исключение | Причина |
|---|---|---|
| 400 Bad Request | `IllegalArgumentException` | Некорректные входные данные (неизвестная роль и т.п.) |
| 404 Not Found | `NotFoundException` | Ресурс не существует |
| 409 Conflict | `ConflictException` | Дубликат (email уже зарегистрирован) |
| 422 Unprocessable | `EmptyCartException` | Оформление заказа с пустой корзиной |
| 422 Unprocessable | `OutOfStockException` | Недостаточно товара на складе |
| 422 Unprocessable | `OrderInTerminalStateException` | Попытка изменить заказ в терминальном статусе |


## Вспомогательные интерфейсы

| Инструмент | URL | Назначение |
|---|---|---|
| Kafka UI | http://localhost:8088 | Управление топиками и consumer groups |
| Grafana | http://localhost:3000 | Метрики PostgreSQL и сервисов |
| Metabase | http://localhost:3001 | Бизнес-аналитика над ClickHouse |
| MinIO Console | http://localhost:9001 | Просмотр бэкапов |
| Prometheus | http://localhost:9090 | Сырые метрики |

## CDC-пайплайн

Debezium читает PostgreSQL WAL для таблиц `customer_orders`, `order_items`, `beers` и `product_variants`, отправляет изменения в Kafka. ClickHouse потребляет этот поток для аналитических запросов, результаты отображаются в Metabase.
