Takipi Storage
==============

Build and run:
- clone the repo
- `cd takipi-storage`
- `mvn compile package`
- `java -jar target/takipi-storage-1.1.0.jar server settings.yml`

Deploy:
- `wget https://s3.amazonaws.com/app-takipi-com/deploy/takipi-storage/takipi-storage-1.2.0.tar.gz`
- **Now with sudo**:
- `cd /opt`
- `tar zxvf https://s3.amazonaws.com/app-takipi-com/deploy/takipi-storage/takipi-storage-1.2.0.tar.gz` 
- `cp /opt/takipi-storage/etc/takipi-storage /etc/init.d`
- Ubuntu: `/usr/sbin/update-rc.d takipi-storage defaults`
- RHEL: `/sbin/chkconfig takipi-storage on`
- Edit `/etc/init.d/takipi-storage` and point it to a valid Java installation
- Run the service: `service takipi-storage start`
