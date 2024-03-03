FROM openjdk:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} apiGateWay.jar
ENTRYPOINT ["java","-jar","/apiGateWay.jar"]
EXPOSE 8084