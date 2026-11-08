FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw -B dependency:go-offline
COPY src ./src
RUN ./mvnw -B verify

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system app \
    && useradd --system --gid app app
COPY --from=builder /app/target/*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
