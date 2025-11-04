FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/Order-service-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
