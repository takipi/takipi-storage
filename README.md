Takipi Storage
==============

With thanks to moovel for developting this version supporting s3: https://github.com/moovel/takipi-storage/tree/s3-storage

Build and run:
- clone the repo
- `cd takipi-storage`
- `mvn compile package`
- edit settings.yml to contain <bucket>, <key>, <password> to access your s3 bucket
- `java -jar target/takipi-storage-1.7.0.jar server settings.yml`

Deploy:
- `wget https://s3.amazonaws.com/app-takipi-com/deploy/takipi-storage/takipi-storage-1.7.0.tar.gz`
- **Now with sudo**:
- `cd /opt`
- `tar zxvf <path-to-download>/takipi-storage-1.7.0.tar.gz` 
- `cp /opt/takipi-storage/etc/takipi-storage /etc/init.d`
- Ubuntu: `/usr/sbin/update-rc.d takipi-storage defaults`
- RHEL: `/sbin/chkconfig takipi-storage on`
- Edit `/etc/init.d/takipi-storage` and point it to a valid Java installation
- Run the service: `service takipi-storage start`

Docker:
- Follow the Dockerfile instructions in the docker subfolder
