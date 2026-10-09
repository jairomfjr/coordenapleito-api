FROM maven:3.9-eclipse-temurin-21

COPY . /app
WORKDIR /app

RUN mvn clean install -DskipTests
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/target/coordenapleito-api-0.0.1.jar"]
