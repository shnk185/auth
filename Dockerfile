FROM amazoncorretto:17
WORKDIR /app
COPY "target/auth-0.0.1-SNAPSHOT.jar" app.jar
EXPOSE 8185
ENTRYPOINT ["java", "-jar", "app.jar"]