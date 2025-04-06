FROM eclipse-temurin:8-jre-alpine-3.21
ADD target/RA-CMP.jar /app/RA-CMP.jar
ADD src/test/resources/config.yml /app/config.yml
ADD src/main/resources/server-example.p12 /app/server-example.p12
CMD java -jar /app/RA-CMP.jar server /app/config.yml
EXPOSE 9080