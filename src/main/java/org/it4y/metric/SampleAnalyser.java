package org.it4y.metric;

import org.it4y.io.BloatMessage;
import org.it4y.io.BloatSamples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by luc on 6/20/14.
 */
public class SampleAnalyser {

    private Logger logger= LoggerFactory.getLogger(SampleAnalyser.class);
    private final Lock lock=new ReentrantLock();
    private BloatSamples samples;
    private BloatMessage lastMessage=null;
    private long prevdpid;
    private int ooodpid;
    private long OooCounter;
    private long OooEvents;
    private long OooMaxPosition;
    private long lostCounter;
    private long sendpktCounter;
    private long recievedPktCounter;
    private Random random=new Random();
    private long measureTime;
    private long baseOffestTime;
    protected LongMovingStatistics owd;
    private long bandwidthSend;
    private long bandwidthRecieved;
    private int size;

    public long getBandwidthRecieved() {
        return bandwidthRecieved;
    }

    public long getBandwidthSend() {
        return bandwidthSend;
    }

    private long avgOWD;

    public SampleAnalyser(BloatSamples samples, long minBaseLine) {
        this.samples=samples;
        this.baseOffestTime=minBaseLine;
        this.owd=new LongMovingStatistics(samples.getNumberOfMessages(),66);
    }

    public SampleAnalyser(int size, long minBaseLine) {
        this.baseOffestTime=minBaseLine;
        this.owd=new LongMovingStatistics(size,66);
    }

    public void analyse() {
        //Calculate packetLoss / OOO
        ArrayList<BloatMessage> list = (ArrayList<BloatMessage>) samples.getList();
        long startTime=list.get(0).getCreated();
        long endTime=list.get(list.size()-1).getCreated();
        for(BloatMessage msg:list) {
                doAnalyse(msg);
        }
       size=samples.getSizeOfMessages();
       long messageSize=size/recievedPktCounter;
       long deltatime=endTime-startTime;
       //calculate bw estimation in kbits/sec (note deltatime = msec)
       if (deltatime>0) {
            bandwidthSend = messageSize * sendpktCounter * 8 / deltatime;
            bandwidthRecieved=messageSize*recievedPktCounter*8/deltatime;
        }
    }

    protected void doAnalyse(BloatMessage msg) {
        this.lock.lock();
        try {
            //skip first message
            if (lastMessage == null) {
                lastMessage = msg;
                sendpktCounter += 1;
                recievedPktCounter += 1;
                return;
            }

            //Measure packet delay
            //We use a positive 24bit time based on CurrentTimesMillis ,
            //values will wrap around 16 Miljoen, but by using the 0xffffffL mask
            //we can eliminated the side effects
            long rawOwd = (msg.getCreated() - msg.getSendCreated()) & 0xffffffL;
            //Idle Raw Offset between sender/reciever, should be positief but can be negative if clock is running behind
            if (rawOwd < baseOffestTime) {
                baseOffestTime = rawOwd;
            }
            //calculate owd
            owd.addValue((rawOwd - baseOffestTime)&0xffffffL);

            //check for packet lost and OOO
            //subId is 24Bits so we mask to fix sing wrapping
            int dpid = (msg.getSubId() - lastMessage.getSubId()) & 0xffffff;
            recievedPktCounter += 1;
            sendpktCounter = sendpktCounter + dpid;
            if (dpid < 0) {
                //Out of order detected, note the gap size
                this.ooodpid = dpid;
                this.OooEvents += 1;
                //get the Max Ooo position
                if (Math.abs(dpid) > OooMaxPosition) {
                    OooMaxPosition = Math.abs(dpid);
                }
            } else if (dpid > 1) {
                if (ooodpid < 0) {
                    //seems like out of order so count this als out of order
                    int nrOfOoo = Math.abs(ooodpid + dpid) + 2;
                    OooCounter = OooCounter + nrOfOoo;
                    lostCounter = lostCounter - nrOfOoo;
                } else {
                    //seems like packet loss
                    lostCounter = lostCounter + dpid - 1;
                }
                ooodpid = 0;
            }
            lastMessage = msg;
        } finally {
            this.lock.unlock();
        }
    }

    public SamplAnalyserValues getMetrics() {
        lock.lock();
        try {

            SamplAnalyserValues result=new SamplAnalyserValues(OooCounter,OooEvents,OooMaxPosition,lostCounter,sendpktCounter,recievedPktCounter,baseOffestTime,size,bandwidthSend,bandwidthRecieved,owd.getMetric());
            OooCounter=0;
            OooEvents=0;
            OooMaxPosition=0;
            lostCounter=0;
            sendpktCounter=0;
            recievedPktCounter=0;
            return result;
        } finally {
            lock.unlock();
        }
    }
}
