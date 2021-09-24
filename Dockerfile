FROM openjdk:11

COPY target/*.jar .
COPY target/MANIFEST .

# used to set the problem pattern
ARG APP_VERSION=1
ENV APP_VERSION=$APP_VERSION

EXPOSE 8080

CMD ["sh", "-c", "/usr/local/openjdk-11/bin/java -Xmx400m -Xms400m -jar *.jar"]
