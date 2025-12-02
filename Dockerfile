# ---------- Build Stage ----------
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and download dependencies first (for caching)
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Copy the rest of the project
COPY . .

# Build the Spring Boot jar
RUN mvn -q package -DskipTests

# ---------- Run Stage ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port expected by Render
EXPOSE 8080

# Start Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
