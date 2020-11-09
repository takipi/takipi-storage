#!/bin/bash

cd docker
docker build -t s3-storage-lambda .

docker run --rm -it s3-storage-lambda
