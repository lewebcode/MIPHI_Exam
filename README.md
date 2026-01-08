# MIPHI Exam — Hotel Booking Microservices

Spring Boot 3.4.x + Spring Cloud (2024.0.1) монорепозиторий с четырьмя микросервисами:

- `eureka-server` — Service Discovery (Port 8761)
- `gateway` — Spring Cloud Gateway (Port 8080)
- `hotel-service` — управление отелями/номерами (Port 8081)
- `booking-service` — бронирования, регистрация/авторизация (Port 8082)

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

## 📍API эндпойнты

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

---

## Алгоритм бронирования

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

### HTTP 401 при доступе к API

```bash
# Получить новый токен
curl -X POST http://localhost:8080/api/bookings/user/auth \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

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