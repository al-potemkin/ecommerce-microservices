FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -f common-lib/pom.xml install -DskipTests

COPY user-service/pom.xml user-service/pom.xml
COPY user-service/src user-service/src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -f user-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system spring && useradd --system --gid spring spring

COPY --from=build /build/user-service/target/*.jar app.jar
USER spring
EXPOSE 8083

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]