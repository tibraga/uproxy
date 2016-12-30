**This project is just to simulate problem with undertow.**

_Servers necessaries:_

1 server nginx
1 server with this project, java and maven installed
1 server with wrk installed

_On the server nginx:_

Restart nginx each 4 seconds.

`while :; do nginx -s stop && nginx ; sleep 4;done`

_On the server with this project:_

`mvn clean install`

Use the script 'start.sh' to start jvm. Change the script with params if necessaries. 
Change the param client.backend with the IP of server nginx.

_On the server with wrk installed:_

`wrk --latency --timeout 5 -c 10 -t 10 -d 360000000 http://[HOST_UNDERTOW]:[PORT_UNDERTOW]`

**_TESTING AND CAPTURE THE PROBLEM:_**

On the server with this project, run tcpdump for watch the packages on the port configured:

`tcpdump -i bond0 -n "port [PORT_UNDERTOW]"`

After some minutes running, the log of undertow will show in the console:

`java.io.IOException: UT001000: Connection closed
       	at io.undertow.client.http.HttpClientConnection$ClientReadListener.handleEvent(HttpClientConnection.java:531)
       	at io.undertow.client.http.HttpClientConnection$ClientReadListener.handleEvent(HttpClientConnection.java:473)
       	at org.xnio.ChannelListeners.invokeChannelListener(ChannelListeners.java:92)
       	at org.xnio.conduits.ReadReadyHandler$ChannelListenerHandler.readReady(ReadReadyHandler.java:66)
       	at org.xnio.nio.NioSocketConduit.handleReady(NioSocketConduit.java:88)
       	at org.xnio.nio.WorkerThread.run(WorkerThread.java:559)
`

This exception will show some times in the console before stop receive packages in the tcpdump.
