FROM eclipse-temurin:21-jre-jammy
ARG JAR_FILE='../build/libs/menify-0.0.1-SNAPSHOT.jar'
COPY ${JAR_FILE} application.jar
ENTRYPOINT ["java", "-jar", "application.jar"]