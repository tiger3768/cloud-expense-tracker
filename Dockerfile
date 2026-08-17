FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN chmod +x mvnw

RUN ./mvnw -B dependency:go-offline

COPY src src

RUN ./mvnw -B clean package -DskipTests


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/expense-tracker-*.jar app.jar

EXPOSE 8080 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
