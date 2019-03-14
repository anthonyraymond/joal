# Builder image with jdk
FROM --platform=$BUILDPLATFORM maven:3.6-jdk-8 AS build


RUN apt-get update \
    && apt-get install -y git \
    && JOAL_VERSION="2.1.14" \
    && git clone https://github.com/anthonyraymond/joal.git --branch "$JOAL_VERSION" --depth=1 \
    && cd joal \
    && mvn --batch-mode --quiet package -DskipTests=true \
    && mkdir /artifact \
    && mv "/joal/target/jack-of-all-trades-$JOAL_VERSION.jar" /artifact/joal.jar \
    && apt-get remove -y git \
    && rm -rf /var/lib/apt/lists/*


# Actual joal image with jre only
FROM openjdk:8u181-jre

WORKDIR /joal/

COPY --from=build /artifact/joal.jar /joal/joal.jar

VOLUME /data

ENTRYPOINT ["java","-jar","/joal/joal.jar"]
CMD ["--joal-conf=/data"]