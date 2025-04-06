FROM maven:3.9-eclipse-temurin-8-focal as build
COPY ./src /buildDir/src
COPY ./pom.xml /buildDir
WORKDIR /buildDir
RUN mvn package

FROM eclipse-temurin:8-jre-alpine-3.21
COPY --from=build /buildDir/target/RA-CMP.jar /app/RA-CMP.jar 
CMD java -jar /app/RA-CMP.jar server /app/config.yml
EXPOSE 9080
