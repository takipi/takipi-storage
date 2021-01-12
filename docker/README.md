# Deploy the Storage Server - S3 (hybrid installations, AWS S3)

For hybrid installations, the Storage Server can be installed in your cluster.

This Storage Server [Dockerfile](Dockerfile) is based on the [Installing the Storage Server on AWS S3](https://doc.overops.com/docs/installing-the-storage-server-on-aws-s3) guide, with some minor modifications.

For complete instructions on performing a hybrid installation, refer to the [Hybrid Installation on Linux](https://doc.overops.com/docs/linux-hybrid-installation) guide.

The file `settings.yaml` must be mounted into the `/opt/takipi-storage/private` directory to run this container. An example [settings.yaml](private/settings.yaml) can be found in this repo.

## Quick Start

This image is on Docker Hub: [overops/storage-server-s3](https://hub.docker.com/r/overops/storage-server-s3)

### Docker Quick Start

```console
docker run -d -p 8080:8080 --mount type=bind,source="$(pwd)"/private,target=/opt/takipi-storage/private overops/storage-server-s3
```