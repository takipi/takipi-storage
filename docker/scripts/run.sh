#!/bin/bash
java -jar /opt/takipi-storage/lib/takipi-storage.jar server /opt/takipi-storage/private/settings.yaml &> /opt/takipi-storage/log/takipi-storage.log &
/usr/bin/tail -f /opt/takipi-storage/log/takipi-storage.log