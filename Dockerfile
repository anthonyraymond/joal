# Builder image with jdk
FROM maven:3.8.3-eclipse-temurin-11 AS build

WORKDIR /build

COPY . /build/

RUN mvn -B --quiet package -DskipTests=true \
    && mkdir /artifact \
    && mv /build/target/jack-of-all-trades-*.jar /artifact/joal.jar


# Actual joal image with jre only
FROM eclipse-temurin:11.0.13_8-jre

LABEL name="joal"
LABEL maintainer="joal.contact@gmail.com"
LABEL url="https://github.com/anthonyraymond/joal"
LABEL vcs-url="https://github.com/anthonyraymond/joal"

WORKDIR /joal/

COPY --from=build /artifact/joal.jar /joal/joal.jar

VOLUME /data

ENTRYPOINT ["java","-jar","/joal/joal.jar"]
CMD ["--joal-conf=/data"]
