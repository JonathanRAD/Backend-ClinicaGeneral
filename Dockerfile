# ETAPA 1: Construcción con Maven
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ETAPA 2: Creación de la imagen final
FROM openjdk:17-jdk-slim
WORKDIR /app

# --- AQUÍ ESTÁ EL CAMBIO ---
# Copiamos el JAR que se genera con el nombre "app.jar"
COPY --from=build /app/target/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]