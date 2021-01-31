FROM openjdk:8-jre-slim
LABEL maintainer="support@overops.com"

ARG APP_VERSION=latest

# install curl
RUN apt-get update; apt-get install -y curl

# rootless
RUN groupadd --gid 1000 overops
RUN adduser --home /opt/takipi-storage --uid 1000 --gid 1000 overops
USER 1000:1000

# install into the /opt directory
WORKDIR /opt

# download and install the storage server
RUN curl -sL https://app-takipi-com.s3.amazonaws.com/deploy/takipi-storage/takipi-storage-${APP_VERSION}.tar.gz | tar -xvzf -

# use a volume to store data
VOLUME ["/opt/takipi-storage/storage"]

RUN mkdir /opt/takipi-storage/private
COPY --chown=1000:1000  "./private/settings.yaml" "/opt/takipi-storage/private/settings.yaml"

# use mount to make settings.yaml available
VOLUME ["/opt/takipi-storage/private"]

WORKDIR /opt/takipi-storage

# copy the run script
COPY --chown=1000:1000 "./scripts/run.sh" "./run.sh"
RUN chmod +x run.sh

EXPOSE 8080 8081

# run the service, printing logs to stdout
CMD ["./run.sh"]
