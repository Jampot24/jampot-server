FROM openjdk:17
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
ENV SPRING_PROFILES_ACTIVE cloud
ENV DOTENV_PATH=.env
EXPOSE 8080