FROM alpine/java:21-jre
LABEL authors="locky003"
ARG JAR_FILE
WORKDIR /app
COPY config config
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-Duser.language=es","-Duser.region=BO","-Duser.country=BO","-Duser.timezone=America/La_Paz", "-jar","app.jar"]