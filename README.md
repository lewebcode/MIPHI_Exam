# MIPHI Exam — Hotel Booking Microservices

Spring Boot 3.4.x + Spring Cloud (2024.0.1) монорепозиторий с четырьмя микросервисами:

- `eureka-server` — Service Discovery (Port 8761)
- `gateway` — Spring Cloud Gateway (Port 8080)
- `hotel-service` — управление отелями/номерами (Port 8081)
- `booking-service` — бронирования, регистрация/авторизация (Port 8082)

---

## 🚀 Быстрый старт

### Вариант 1: С использованием Docker Compose

```bash
cd /Users/levnikonenko/IdeaProjects/MIPHI_Exam
docker-compose up -d
docker-compose ps
```

### Вариант 2: Локальный запуск (без Docker)

```bash
mvn -q -DskipTests package
java -jar eureka-server/target/eureka-server-1.0-SNAPSHOT.jar &
java -jar hotel-service/target/hotel-service-1.0-SNAPSHOT.jar &
java -jar booking-service/target/booking-service-1.0-SNAPSHOT.jar &
java -jar gateway/target/gateway-1.0-SNAPSHOT.jar
```

---

## 🏛️ Архитектура

### Микросервисная система

```
┌─────────────────────────────────┐
│   CLIENT (Postman/Frontend)     │
└────────────┬────────────────────┘
             │ HTTP
             ▼
    ┌─────────────────────┐
    │  API GATEWAY:8080   │
    │  - JWT auth         │
    │  - Маршрутизация    │
    └──┬─────────────────┬┘
       │                 │
       ▼                 ▼
    ┌──────────┐    ┌──────────┐
    │ HOTEL:   │    │ BOOKING: │
    │ 8081     │    │ 8082     │
    │ H2 DB    │    │ H2 DB    │
    └─────┬────┘    └─────┬────┘
          │                │
          └────────┬───────┘
                   │
                   ▼
        ┌──────────────────┐
        │ EUREKA:8761      │
        │ Service Registry │
        └──────────────────┘
```

---

## �� Аутентификация

### 1. Получить токен

```bash
curl -X POST http://localhost:8080/api/bookings/user/auth \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

Ответ:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin"
}
```

### 2. Использовать токен для запросов

```bash
curl -X GET http://localhost:8080/api/hotels \
  -H "Authorization: Bearer {token}"
```

### 3. Регистрация нового пользователя

```bash
curl -X POST http://localhost:8080/api/bookings/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","password":"password123"}'
```

---

## 🗄️ Предзаполненные данные

### Учетные записи (Booking Service)
- **admin** / admin (роль: ADMIN)
- **user** / admin (роль: USER)

### Тестовые данные (Hotel Service)
- **1 отель**: "Grand Hotel"
- **3 номера**: 101, 102, 103 (все свободны)

---

## 📍 API эндпойнты

### Аутентификация
```
POST   /api/bookings/user/register
POST   /api/bookings/user/auth
GET    /api/bookings/user
PATCH  /api/bookings/user
DELETE /api/bookings/user
```

### Бронирования
```
POST   /api/bookings
GET    /api/bookings
GET    /api/bookings/{id}
DELETE /api/bookings/{id}
PUT    /api/bookings/{id}/confirm
```

### Отели
```
GET    /api/hotels
POST   /api/hotels (ADMIN)
GET    /api/hotels/{id}
PUT    /api/hotels/{id} (ADMIN)
DELETE /api/hotels/{id} (ADMIN)
```

### Номера
```
GET    /api/rooms
POST   /api/rooms (ADMIN)
GET    /api/rooms/{id}
PUT    /api/rooms/{id} (ADMIN)
DELETE /api/rooms/{id} (ADMIN)
GET    /api/rooms/recommend
```

### Управление пользователями (ADMIN только)
```
GET    /api/bookings/users
GET    /api/bookings/users/{id}
PUT    /api/bookings/users/{id}
DELETE /api/bookings/users/{id}
```

---

## 🧪 Тестирование

### Полное тестирование

```bash
./full_api_test.sh
```

### Тестирование Postman коллекции

```bash
./postman_collection_test.sh
./test_direct_api.sh
```

### Unit тесты

```bash
mvn test
```

---

## 🐳 Docker

### Структура

```
├── docker-compose.yml
├── eureka-server/Dockerfile
├── gateway/Dockerfile
├── hotel-service/Dockerfile
├── booking-service/Dockerfile
└── .dockerignore
```

### Команды

```bash
# Сборка образов
docker-compose build --no-cache

# Запуск контейнеров
docker-compose up -d

# Проверка статуса
docker-compose ps

# Просмотр логов
docker-compose logs -f [service-name]

# Остановка
docker-compose down
```

### Docker образы

| Образ | Размер | Базовый образ |
|-------|--------|---------------|
| miphi_exam-eureka | 469 MB | eclipse-temurin:17-jdk-jammy |
| miphi_exam-gateway | 463 MB | eclipse-temurin:17-jdk-jammy |
| miphi_exam-hotel | 499 MB | eclipse-temurin:17-jdk-jammy |
| miphi_exam-booking | 506 MB | eclipse-temurin:17-jdk-jammy |

---

## 🗄️ Базы данных

### H2 в памяти

**Hotel Service** (jdbc:h2:mem:hoteldb)
```
Таблицы:
- hotels (id, name, address)
- rooms (id, hotel_id, number, available, times_booked)
```

**Booking Service** (jdbc:h2:mem:bookingdb)
```
Таблицы:
- users (id, username, password, role, created_at)
- bookings (id, user_id, room_id, check_in_date, check_out_date, status, created_at)
- booking_confirmations (id, booking_id, confirmation_token, confirmed)
```

### H2 консоль

- **Hotel**: http://localhost:8081/h2-console (jdbc:h2:mem:hoteldb, user: sa)
- **Booking**: http://localhost:8082/h2-console (jdbc:h2:mem:bookingdb, user: sa)

---

## �� Алгоритм бронирования

1. Создание: запись со статусом `PENDING`
2. Фиксация: вызов `/confirm-availability` с retry логикой
3. Подтверждение: статус `CONFIRMED` при успехе, иначе `CANCELLED`
4. Компенсация: вызов `/release` при ошибке
5. Автоподбор: комнаты сортируются по `times_booked` (по возрастанию)

---

## 🔧 Стек технологий

### Spring Boot
- **Spring Boot**: 3.4.3
- **Spring Cloud**: 2024.0.1
- **Java**: 17 OpenJDK
- **Spring Cloud Eureka**: Service Discovery
- **Spring Cloud Gateway**: API Gateway
- **Spring Security + JWT**: Аутентификация
- **Spring Data JPA**: ORM
- **H2**: In-memory DB
- **Lombok**: Boilerplate reduction

### Build & DevOps
- **Maven**: 3.9
- **Docker**: Container platform
- **Docker Compose**: Orchestration

---

## 🐛 Решение проблем

### Сервис не регистрируется в Eureka

```bash
docker-compose logs [service-name] | grep eureka
curl http://localhost:8761/eureka/apps
```

### HTTP 401 при доступе к API

```bash
# Получить новый токен
curl -X POST http://localhost:8080/api/bookings/user/auth \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

### H2 консоль возвращает 404

Это нормально. Данные загружаются при запуске через schema.sql и data.sql.

---

## �� Дополнительно

### Postman коллекция

Используйте `MIPHI_Exam_API_Collection.json` с 20+ готовыми запросами.

### Интерактивное управление

```bash
./docker-manage.sh start
./docker-manage.sh stop
./docker-manage.sh restart
./docker-manage.sh logs
./docker-manage.sh urls
```

### Сценарии использования

**Сценарий 1: Создать отель и забронировать номер**
1. Авторизоваться: admin/admin
2. POST /api/hotels (создать отель)
3. POST /api/rooms (добавить номер)
4. POST /api/bookings (создать бронирование)
5. PUT /api/bookings/{id}/confirm (подтвердить)

**Сценарий 2: Пользователь бронирует номер**
1. POST /api/bookings/user/register (регистрация)
2. POST /api/bookings/user/auth (получить токен)
3. GET /api/rooms (просмотреть номера)
4. POST /api/bookings (забронировать)
5. DELETE /api/bookings/{id} (отменить)

---

## ✅ Проверка готовности

Система готова если:

- ✅ `docker-compose ps` показывает все 4 сервиса как `Up`
- ✅ Eureka доступен: http://localhost:8761
- ✅ API Gateway отвечает: http://localhost:8080
- ✅ `./full_api_test.sh` проходит все тесты
- ✅ Postman коллекция работает

---

## 🎯 Итог

Полностью функциональная микросервисная система бронирования отелей:
- ✅ Spring Boot 3.4.3 + Spring Cloud
- ✅ Docker контейнеризация
- ✅ JWT аутентификация
- ✅ Service Discovery (Eureka)
- ✅ API Gateway маршрутизация
- ✅ H2 базы данных
- ✅ Comprehensive тестирование

**PRODUCTION READY! 🚀**
