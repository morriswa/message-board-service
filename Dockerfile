FROM --platform=x86-64 amazoncorretto:17-alpine-jdk

ENV APPCONFIG_APP_ID appid
ENV APPCONFIG_PROFILE_ID profileid
ENV APPCONFIG_ENV_ID envid
ENV AWS_ACCESS_KEY_ID awsid
ENV AWS_SECRET_ACCESS_KEY secretid

WORKDIR /app

COPY target/message-board-service-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir /files

ENTRYPOINT ["java","-jar","/app/app.jar"]

EXPOSE 8080