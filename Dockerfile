FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY settings.xml /root/.m2/settings.xml
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app
COPY --from=build /app/target/rag-1.0.0.jar app.jar

ENV JAVA_OPTS="-Xms512m -Xmx1024m"
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
