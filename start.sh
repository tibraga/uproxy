#!/bin/bash

java -Dclient.backend=http://127.0.0.1:8087 -Dserver.port=8011 -Dclient.maxRequestTime=0 -Dclient.connectionsPerThread=85 -Dserver.ioThread=4 -Dserver.workerThreads=$[4*24] -Dserver.backlog=65535 -Dserver.rewriteHostHeader=false -Dserver.reuseXForwarded=true -Dclient.virtualhost=domain.local.com -Dcom.sun.management.jmxremote.port=19998 -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -jar uproxy-1.0-SNAPSHOT-uber.jar
