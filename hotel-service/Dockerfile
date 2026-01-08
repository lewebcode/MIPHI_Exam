# Многоэтапная сборка для Hotel Management Service
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Копируем root pom.xml и все модули
COPY pom.xml .
COPY eureka-server eureka-server/
COPY gateway gateway/
COPY hotel-service hotel-service/
COPY booking-service booking-service/

# Собираем только этот модуль
RUN mvn clean package -pl hotel-service -am -DskipTests

# Финальный образ
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Копируем JAR из сборочного образа
COPY --from=builder /build/hotel-service/target/hotel-service-*.jar hotel-service.jar

# Переменные окружения для оптимизации JVM для контейнера
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=25.0"

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "hotel-service.jar"]
