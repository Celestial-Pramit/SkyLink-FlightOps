FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/SkyLink-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /tmp/uploads
EXPOSE 8088
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
