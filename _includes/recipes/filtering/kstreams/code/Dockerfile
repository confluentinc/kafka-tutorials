FROM openjdk:8-jre-alpine

RUN apk add --no-cache bash
RUN mkdir -p /opt/kstreams

COPY build/libs/kstreams-filter-0.0.1.jar /opt/kstreams/app.jar

CMD ["/usr/bin/java", "-jar", "/opt/kstreams/app.jar"]
