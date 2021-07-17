# Builder image with jdk
FROM maven:3.8.1-adoptopenjdk-11 AS build

WORKDIR /build

COPY . /build/

RUN ls -la && mvn -B --quiet package -DskipTests=true \
    && mkdir /artifact && ls /build/target \
    && mv "/build/target/jack-of-all-trades-*.jar" /artifact/joal.jar


# Actual joal image with jre only
FROM adoptopenjdk:11.0.11_9-jre-hotspot

LABEL name="joal"
LABEL maintainer="joal.contact@gmail.com"
LABEL url="https://github.com/anthonyraymond/joal"
LABEL vcs-url="https://github.com/anthonyraymond/joal"

WORKDIR /joal/

COPY --from=build /artifact/joal.jar /joal/joal.jar

VOLUME /data

ENTRYPOINT ["java","-jar","/joal/joal.jar"]
CMD ["--joal-conf=/data"]
