FROM eclipse-temurin:17-jre

RUN mkdir /opt/app

COPY ./api/target/quarkus-app/ /opt/app
COPY ./engine /opt/app/engine

EXPOSE 8080

CMD ["java", "-jar", "/opt/app/quarkus-run.jar"]
