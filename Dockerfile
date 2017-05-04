FROM openjdk:8-jre

WORKDIR /jack-of-all-trades/

RUN apt-get update \
    && apt-get install -y ca-certificates curl \
    && GITHUB_REPO="https://github.com/anthonyraymond/joal" \
    && LATEST=$(curl -sSI $GITHUB_REPO"/releases/latest" | perl -n -e '/^Location: .*?tag\/(.*?)\r*$/ && print "$1\n"') \
    && curl -f -L $GITHUB_REPO"/releases/download/"$LATEST"/"$LATEST".jar" > /jack-of-all-trades/jack-of-all-trades.jar \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /jack-of-all-trades

EXPOSE 49152-65534
VOLUME /data

CMD ["java","-jar","/jack-of-all-trades/jack-of-all-trades.jar","/data"]
