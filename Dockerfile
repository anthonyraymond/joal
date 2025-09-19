FROM anthonyraymond/joal:2.1.36

WORKDIR /build

LABEL name="joal"
LABEL maintainer="skylanix@favrep.ch"
LABEL url="https://github.com/skylanix/joal"
LABEL vcs-url="https://github.com/skylanix/joal"

RUN apt-get update && apt-get install -y gosu wget && rm -rf /var/lib/apt/lists/*

WORKDIR /joal/

COPY init.sh /init.sh
RUN chmod +x /init.sh

RUN mkdir -p /data/torrents /data/clients && chmod 777 /data

VOLUME /data

ENTRYPOINT ["/init.sh"]
CMD ["--joal-conf=/data"]
