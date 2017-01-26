# Run me with docker run -v <host-path-to-storage>:/opt/takipi-storage/storage -p <host_port>:$STORAGE_PORT
# Logs are written to <storage>/log folder and both should be persistent outside of the running container

FROM java:8
MAINTAINER Chen harel "https://github.com/chook"

ENV VERSION 1.7.0
ENV TAR_FILENAME takipi-storage-$VERSION.tar.gz
ENV JAR_FILENAME takipi-storage-$VERSION.jar
ENV STORAGE_PORT 8080

RUN wget https://s3.amazonaws.com/app-takipi-com/deploy/takipi-storage/$TAR_FILENAME
RUN tar zxvf $TAR_FILENAME -C /tmp && \
    mkdir -p /opt/takipi-storage/lib && \
    cp /tmp/takipi-storage/lib/$JAR_FILENAME /opt/takipi-storage/lib
ADD settings.yml /opt/takipi-storage

EXPOSE $STORAGE_PORT

WORKDIR /opt/takipi-storage
CMD java -jar /opt/takipi-storage/lib/$JAR_FILENAME server settings.yml
