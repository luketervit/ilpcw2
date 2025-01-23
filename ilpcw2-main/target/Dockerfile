FROM --platform=linux/amd64 openjdk:21

LABEL authors="luket"

EXPOSE 8080

WORKDIR /app

COPY pizzadronz-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar" , "app.jar"]