```
cd /opt/apache-flume-1.9.0-bin/bin

./flume-ng  agent -n agent  -c  ../conf   -f  ../conf/flume.conf  -Dflume.root.logger=INFO,console
```