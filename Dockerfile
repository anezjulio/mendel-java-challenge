# ---- dependencies ----
FROM maven:3.9-eclipse-temurin-21 AS deps
WORKDIR /app
COPY pom.xml .
# Download dependencies (including test deps) to warm the Maven cache
RUN mvn -q -DskipTests dependency:go-offline

# ---- run tests ----
FROM maven:3.9-eclipse-temurin-21 AS test
WORKDIR /app
# Reuse the cached Maven repository from deps stage
COPY --from=deps /root/.m2 /root/.m2
COPY pom.xml .
COPY src ./src
# Run tests. If tests fail, docker build fails here.
RUN mvn -q test

# ---- build artifact ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY --from=deps /root/.m2 /root/.m2
COPY pom.xml .
COPY src ./src
# Package the jar (tests skipped)
RUN mvn -q -DskipTests package

# ---- runtime image ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]