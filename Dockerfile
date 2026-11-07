# ---- build stage: jar বানায় ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# dependency আগে নামাই, যাতে শুধু code বদলালে এই layer cache থেকে আসে
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q -DskipTests package

# ---- run stage: শুধু JRE আর jar, ছোট image ----
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --uid 1001 nhls
COPY --from=build /workspace/target/*.jar app.jar
USER nhls

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
