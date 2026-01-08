# MIPHI Exam — Hotel Booking Microservices

Spring Boot 3.5.x + Spring Cloud (2024.0.2) монорепозиторий с четырьмя сервисами:

- `eureka-server` — Service Discovery
- `gateway` — Spring Cloud Gateway (JWT passthrough)
- `hotel-service` — управление отелями/номерами, рекомендации по times_booked, подтверждение/отмена слотов
- `booking-service` — бронирования, регистрация/авторизация (JWT), саги с компенсацией

## Быстрый старт

```bash
mvn -q -DskipTests package
# в отдельных терминалах
java -jar eureka-server/target/eureka-server-1.0-SNAPSHOT.jar
java -jar hotel-service/target/hotel-service-1.0-SNAPSHOT.jar
java -jar booking-service/target/booking-service-1.0-SNAPSHOT.jar
java -jar gateway/target/gateway-1.0-SNAPSHOT.jar
```

Порты по умолчанию:
- Eureka: `8761`
- Gateway: `8080` (все внешние запросы)
- Сервисы регистрируются с random port (Spring Cloud LoadBalancer + Eureka).

## Предзаполненные данные

- Booking Service (H2): admin/user с паролем `admin`
  - admin / admin
  - user / admin
- Hotel Service (H2): один отель и три номера (`101`, `102`, `103`).

## Аутентификация

1. Получить токен:
   - POST `/api/bookings/user/auth` (через Gateway) с `{ "username":"admin", "password":"admin" }`
   - либо зарегистрироваться: POST `/api/bookings/user/register` (роль USER)
2. Использовать `Authorization: Bearer <jwt>` для всех защищённых эндпойнтов.

## Основные эндпойнты (через Gateway)

Booking (префикс `/api/bookings`):
- `POST /user/register` — регистрация (JWT в ответе)
- `POST /user/auth` — получение JWT
- `POST /user` / `PATCH /user` / `DELETE /user` — управление пользователями (ADMIN)
- `POST /booking` — создать бронирование; `autoSelect=true` выбирает номер по min(times_booked)
- `GET /bookings` — история пользователя
- `GET /booking/{id}` — детали (USER видит только свои; ADMIN — все)
- `DELETE /booking/{id}` — отмена бронирования (компенсация release)

Hotel (префикс `/api`):
- `POST /hotels` — создать отель (ADMIN)
- `GET /hotels` — список (USER/ADMIN)
- `POST /rooms` — добавить номер (ADMIN)
- `GET /rooms` — свободные номера
- `GET /rooms/recommend` — свободные номера, отсортированные по `times_booked`, затем `id`
- `POST /rooms/{id}/confirm-availability` — удержание слота (internal, вызывается Booking)
- `POST /rooms/{id}/release` — компенсирующее действие (internal)

## Алгоритм бронирования и согласованность

1. Booking создает запись со статусом `PENDING` (локальная транзакция) и фиксирует `requestId` для идемпотентности.
2. Вызывает Hotel `/confirm-availability` с тайм-аутом и backoff retry (3 попытки, до 2с).
3. При успехе — статус `CONFIRMED`; при ошибке/тайм-ауте — `CANCELLED` и вызов `/release`.
4. Автоподбор комнат сортирует свободные по возрастанию `times_booked`, далее `id`.
5. Пересечения дат проверяются в Hotel; `requestId` предотвращает дубликаты.

## Тесты

Юнит/сервисные тесты (Mockito):
- `BookingWorkflowServiceTest` — успех, сбой с компенсацией, идемпотентность, автоподбор, валидация дат
- `AvailabilityServiceTest` — удержание, идемпотентность, конфликт дат, release

Запуск:
```bash
mvn test
```

## Документация OpenAPI

- Booking: `http://localhost:8080/api/bookings/swagger-ui/index.html`
- Hotel:   `http://localhost:8080/api/hotels/swagger-ui/index.html`

## Примечания

- Все сервисы — H2 in-memory; данные обнуляются при перезапуске.
- Тайм-ауты и ретраи заданы в `booking-service` (`HotelClient`).
- Роли проверяются аннотациями `@PreAuthorize`; каждый сервис валидирует JWT сам.
