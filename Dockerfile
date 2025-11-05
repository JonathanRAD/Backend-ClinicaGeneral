# ETAPA 1: Construcción con Maven
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ETAPA 2: Creación de la imagen final
FROM openjdk:17-jre-slim
WORKDIR /app

# Copiamos el JAR manteniendo su nombre original
COPY --from=build /app/target/api-0.0.1-SNAPSHOT.jar .

# Ejecutamos el JAR usando su nombre original
ENTRYPOINT ["java","-jar","./api-0.0.1-SNAPSHOT.jar"]