owbloat
=======

owbloat is a little test project used to measure oneway delay and bufferbloat on different links.
its bad coding style and just a playground to test and verify impact of buffers on different levels.

usage
=====

tool can be run as client or server. client will send UDP packets to server. server measures the extra delay caused by bufferbloat (NOT the propagation delay which is , in idle , = RTT/2)
also packetloss and send and recieved bandwidth is measured.

usage : 

  java -jar target/owbloat-1.0-SNAPSHOT.one-jar.jar --help
  
  

