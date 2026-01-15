# Dockerfile optimizado para Railway
# Multi-stage build para optimizar el tamaño de la imagen
FROM maven:3.9.5-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copiar archivos de configuración de Maven
COPY backend/pom.xml ./backend/
COPY backend/src ./backend/src

# Construir aplicación
WORKDIR /app/backend
RUN mvn clean package -DskipTests -B

# Imagen final optimizada
FROM eclipse-temurin:21-jre-alpine

# Instalar curl para health checks
RUN apk add --no-cache curl

WORKDIR /app

# Copiar JAR desde la etapa de build
COPY --from=build /app/backend/target/*.jar app.jar

# Exponer puerto (Railway asigna dinámicamente)
EXPOSE 8080

# Comando de inicio con optimizaciones JVM para Railway
# Railway inyecta la variable PORT automáticamente
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication -Djava.security.egd=file:/dev/./urandom -Dserver.port=${PORT:-8080} -jar app.jar"]
