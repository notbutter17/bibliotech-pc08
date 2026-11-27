# syntax=docker/dockerfile:1
# ---------- Etapa 1: compilación (JDK + Maven wrapper) ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Dependencias primero: esta capa se reutiliza mientras pom.xml no cambie.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src/ src/
# Las pruebas ya corrieron en el job de CI; aquí solo se empaqueta.
RUN ./mvnw -B -q -DskipTests package && cp target/*.jar app.jar

# ---------- Etapa 2: ejecución (solo JRE, usuario sin privilegios) ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system biblio && useradd --system --gid biblio biblio
COPY --from=build /app/app.jar app.jar
USER biblio

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
