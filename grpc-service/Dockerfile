FROM  openjdk:17-alpine3.14 as transformer

WORKDIR /application
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar list
RUN java -Djarmode=layertools -jar application.jar extract

# Second stage
FROM openjdk:17-alpine3.14

ARG USER_ID=5050
ARG GROUP_ID=5050

#RUN addgroup -S spring && adduser -S spring -G spring
RUN apk update && apk upgrade && apk add --no-cache jq tzdata &&\
    addgroup -g ${GROUP_ID} spring &&\
    adduser -S -u ${USER_ID} -g spring spring &&\
    install -d -m 0755 -o spring -g spring /home/spring

USER spring:spring

WORKDIR /app

#Using multi stage layered builds
COPY --chown=5050:5050 --from=transformer /application/dependencies/ ./
COPY --chown=5050:5050 --from=transformer /application/snapshot-dependencies/ ./
COPY --chown=5050:5050 --from=transformer /application/spring-boot-loader/ ./
COPY --chown=5050:5050 --from=transformer /application/application/ ./

EXPOSE 9999

# Entrypoint to app
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]