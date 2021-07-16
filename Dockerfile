# Builder image with jdk
FROM maven:3.8.1-adoptopenjdk-11 AS build


RUN apt-get update \
    && apt-get install -y git \
    && JOAL_VERSION="2.1.25" \
    && git clone https://github.com/anthonyraymond/joal.git --branch "$JOAL_VERSION" --depth=1 \
    && cd joal \
    && mvn --batch-mode --quiet package -DskipTests=true \
    && mkdir /artifact \
    && mv "/joal/target/jack-of-all-trades-$JOAL_VERSION.jar" /artifact/joal.jar \
    && apt-get remove -y git \
    && rm -rf /var/lib/apt/lists/*


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
