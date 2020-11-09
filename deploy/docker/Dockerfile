from ubuntu:18.04

ENV S3_BUCKET=overops-storage
ENV S3_PREFIX=overops

ENV LC_ALL=C.UTF-8
ENV LANG=C.UTF-8

RUN apt-get update; apt-get install -yq python3 python3-pip openjdk-8-jdk curl unzip less

# install aws-sam-cli
RUN pip3 install aws-sam-cli

# install aws cli
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"; unzip awscliv2.zip; ./aws/install

# aws creds / config
RUN mkdir -p /root/.aws
COPY credentials /root/.aws/credentials
COPY config /root/.aws/config

RUN mkdir -p /cloudformation
COPY sam.yaml /cloudformation/sam.yaml
RUN cd /cloudformation; curl "https://dev-overops.s3.amazonaws.com/storage-s3-lambda-1.0.jar" -so "storage-s3-lambda-1.0.jar"
COPY run.sh /cloudformation/run.sh

WORKDIR /cloudformation

ENTRYPOINT ./run.sh
