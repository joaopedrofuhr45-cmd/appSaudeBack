# Etapa 1: build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: execução
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Em container o padrão seria "dev" (H2 em memória); aqui o padrão é produção.
ENV SPRING_PROFILES_ACTIVE=prod
# Fuso do Brasil: o @Future e as datas das consultas usam o relógio do servidor.
ENTRYPOINT ["java", "-Duser.timezone=America/Sao_Paulo", "-jar", "app.jar"]