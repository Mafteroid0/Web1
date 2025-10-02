# Собираем наш сервер
FROM gradle:8.10-jdk17 AS build
# Копируем из текущей директории, а не родительской
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle jar --no-daemon

FROM eclipse-temurin:17-jre
EXPOSE 1337
RUN mkdir /app
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/app.jar
# Уберите аргумент из ENTRYPOINT, он должен быть в CMD
ENTRYPOINT ["java", "-jar", "app.jar"]