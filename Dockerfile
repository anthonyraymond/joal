FROM openjdk:8-jre


WORKDIR /joal/

RUN apt-get update \
        && apt-get install -y ca-certificates curl \
        && curl -s https://api.github.com/repos/anthonyraymond/joal/releases/latest \
        | grep browser_download_url \
        | grep joal.tar.gz \
        | cut -d '"' -f 4 \
        | wget -i - \
        && tar --wildcards -zxvf joal.tar.gz '*.jar' \
        && rm -rf /data/clients \
        && tar -zxvf joal.tar.gz 'clients/' \
        && mv *.jar joal.jar \
        && rm joal.tar.gz
        && apt-get remove curl \
        && rm -rf /var/lib/apt/lists/*


EXPOSE 5081 49152-65534

VOLUME /data

ENTRYPOINT ["java","-jar","/joal/joal.jar"]
CMD ["--joal-conf=/data"]
