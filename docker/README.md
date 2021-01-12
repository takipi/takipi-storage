takipi-storage on docker
========================

The Storage Server [Dockerfile](Dockerfile) is based on the [Installing the Storage Server on a Local Server](https://doc.overops.com/docs/installing-the-storage-server-on-a-local-server) guide, with some minor modifications.

For complete instructions on performing a hybrid installation, refer to the [Hybrid Installation on Linux](https://doc.overops.com/docs/linux-hybrid-installation) guide.

The file `settings.yaml` must be mounted into the `/opt/takipi-storage/private` directory to run this container. An example [settings.yaml](private/settings.yaml) can be found in this repo.

### Docker Quick Start

```console
docker run -d -p 8080:8080 -p 8081:8081 --mount type=bind,source="$(pwd)"/storage,target=/opt/takipi-storage/storage  --mount type=bind,source="$(pwd)"/private,target=/opt/takipi-storage/private overops/storage-server
```
