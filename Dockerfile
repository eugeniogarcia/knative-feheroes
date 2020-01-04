FROM openjdk:8u212-jre-alpine
VOLUME /tmp
EXPOSE 8085
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
