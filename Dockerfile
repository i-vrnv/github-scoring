# Step 1: Build a jar
FROM gradle:8.5-jdk21 AS builder
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN git config --global --add safe.directory /home/gradle/src
RUN gradle bootJar --no-daemon
  
# Step 2: Create an image
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache bash curl

WORKDIR /opt/app
COPY --from=builder /home/gradle/src/build/libs/app.jar /opt/app/app.jar

RUN adduser \
--disabled-password \
--shell "/sbin/nologin" \
--no-create-home \
appuser

RUN chown -R appuser:appuser /opt/app

USER appuser

EXPOSE 8080

CMD ["java", "-jar", "/opt/app/app.jar"]
