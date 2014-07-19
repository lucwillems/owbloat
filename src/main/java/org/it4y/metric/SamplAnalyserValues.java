package org.it4y.metric;

/**
 * Created by luc on 6/23/14.
 */
public class SamplAnalyserValues {

    private long OooPkts;
    private long OooEvents;
    private long OooMaxPosition;
    private long lostPkts;
    private long sendPkts;
    private long recievedPkts;
    private long timestamp;
    private long baseOffestTime;
    private long bandwidthSend;
    private long bandwidthRecieved;
    private int size;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getBandwidthSend() {
        return bandwidthSend;
    }

    public long getBandwidthRecieved() {
        return bandwidthRecieved;
    }

    protected LongMetricValues owdValues;

    public long getOooPkts() {
        return OooPkts;
    }

    public long getOooEvents() {
        return OooEvents;
    }

    public long getOooMaxPosition() {
        return OooMaxPosition;
    }

    public long getLostPkts() {
        return lostPkts;
    }

    public long getSendPkts() {
        return sendPkts;
    }

    public long getRecievedPkts() {
        return recievedPkts;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getBaseOffestTime() {
        return baseOffestTime;
    }

    public LongMetricValues getOwdValues() {
        return owdValues;
    }

    public SamplAnalyserValues(long OooPkts,long OooEvents, long OoomMaxPosition,long LostPkts, long SendPckts, long recievedPkts,long baseOffestTime,int size,long send,long recieved,LongMetricValues owd) {
        this.OooPkts=OooPkts;
        this.OooEvents=OooEvents;
        this.OooMaxPosition=OoomMaxPosition;
        this.lostPkts=LostPkts;
        this.sendPkts=SendPckts;
        this.recievedPkts=recievedPkts;
        this.timestamp=System.currentTimeMillis();
        this.baseOffestTime=baseOffestTime;
        this.bandwidthSend=send;
        this.bandwidthRecieved=recieved;
        this.owdValues=owd;
        this.size=size;
    }
}
