takipi-storage on docker
========================

To run takipi-storage for s3:
- configure bucket, access key and password in settings.yml
- run it with docker run -v <path>:/opt/takipi-storage/storage -p <port>:8080
  (If you wish to have the logs persisted to <path> and they can be found in <path>/logs folder. -p to override default 8080 port) 

