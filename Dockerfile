# --- этап 1: собираю jar ---
FROM eclipse-temurin:21-jdk-noble AS build
WORKDIR /app

# сначала только то, от чего зависят библиотеки: этот слой пересобирается редко
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -q dependency:go-offline

# потом исходники: они меняются часто
COPY src/ src/
RUN ./mvnw -q package -DskipTests

# --- этап 2: только JRE и готовый jar ---
FROM eclipse-temurin:21-jre-noble
WORKDIR /app

# запускаю не от root: если приложение взломают, у процесса будет минимум прав
RUN useradd --system --create-home --uid 1001 app
COPY --from=build /app/target/*.jar app.jar
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]