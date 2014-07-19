package org.it4y.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by luc on 6/8/14.
 *
 */
public class BloatSamples {
    public final static long IP_HEADERSIZE=28; //default IP + UDP header size
    private final int id;
    private final ArrayList<BloatMessage> samples=new ArrayList<BloatMessage>(100);
    private long min=Long.MAX_VALUE;
    private long max=Long.MIN_VALUE;
    private long median=0;
    private double packetloss;
    private long bandwidth=0;

    public BloatSamples(final int id) {
        this.id=id;
    }
    public void addSample(final BloatMessage msg) {
        this.samples.add(msg);
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder=new StringBuilder(128);
        stringBuilder.append("id=").append(this.id).append('[').append("samples: ").append(this.samples.size()).append(']');
        //calculate size
        int size=0;
        long sendStartTime=0;
        long recieveStartTime=0;
        long sendEndTime=0;
        long recieveEndTime=0;

        for (final BloatMessage msg: this.samples) {
            recieveStartTime=msg.getRecievedStart();
            sendStartTime=msg.getSendStart();
            recieveEndTime=Math.max(recieveEndTime,msg.getRecieved());
            sendEndTime=Math.max(recieveEndTime,msg.getSend());
            size=size+msg.getTotalSize();
        }
        stringBuilder.append("[size: ").append(size);
        //stringBuilder.append(" send start: ").append(sendStartTime).append(" send end: ").append(sendEndTime);
        stringBuilder.append(" delta send: ").append(sendEndTime-sendStartTime).append(" delta recieve: ").append(recieveEndTime-recieveStartTime).append(" ]");
        return stringBuilder.toString();
    }

    public void saveSendToFile(final File file) throws IOException {
        final Writer writer=new FileWriter(file, true);
        //calculate send bandwidth , % packet loss
        final StringBuilder sampleInfo=new StringBuilder(1024);
        sampleInfo.append(this.samples.size()).append(";");
        //find delta time to calculate bandwidh , note : time is in nanosecodns
        final BloatMessage lastSample= this.samples.get(this.samples.size() - 1);//TODO : sort op subId to handle OOO
        final BloatMessage firstSample= this.samples.get(0);
        final long nrofsamples=lastSample.getSubId();
        final long deltaTime=lastSample.getSendCreated()-firstSample.getSendCreated();
        final long size=(lastSample.getTotalSize()+ BloatSamples.IP_HEADERSIZE)*nrofsamples;
        final double bandwidth=((double)size)*8/((double)deltaTime/1000.0);
        sampleInfo.append(deltaTime).append(";").append(size).append(";").append(nrofsamples).append(";").append(Math.round(bandwidth)).append(";");
        for (final BloatMessage msg: this.samples) {
            writer.write("s" + ";" + msg.getSendCreated() + ";" + msg.getId() + ";" + msg.getSubId() + ";" + sampleInfo + msg.getSend() + ";" + msg.getSendStart() + "\n");
        }
        writer.flush();
        writer.close();
    }

    public void saveRecievedToFile(final File file) throws IOException {
        final Writer writer=new FileWriter(file, true);
        final StringBuilder sampleInfo=new StringBuilder(1024);
        sampleInfo.append(this.samples.size()).append(";");
        //find delta time to calculate bandwidh , note : time is in nanosecodns
        final BloatMessage lastSample= this.samples.get(this.samples.size() - 1);//TODO : sort op subId to handle OOO
        final BloatMessage firstSample= this.samples.get(0);
        final long nrofsamples= this.samples.size();
        final long deltaTime=lastSample.getCreated()-firstSample.getCreated();
        final long size=(lastSample.getTotalSize()+ BloatSamples.IP_HEADERSIZE)*nrofsamples;
        final double bandwidth=((double)size)*8/((double)deltaTime/1000.0);
        sampleInfo.append(deltaTime).append(";").append(size).append(";").append(nrofsamples).append(";").append(Math.round(bandwidth)).append(";");

        for (final BloatMessage msg: this.samples) {
            writer.write("r" + ";" + msg.getCreated() + ";" + msg.getId() + ";" + msg.getSubId() + ";" + sampleInfo + msg.getRecieved() + ";" + msg.getRecievedStart() + "\n");
        }
        writer.flush();
        writer.close();
    }

    public int getId() {
        return this.id;
    }

    public void calculateResults() {
        //Find minimum delta time between send/reciece timestamps
        //find median (66%) value
        long[] deltas=new long[samples.size()];
        int i=0;
        int maxPacketId=0;
        for(BloatMessage msg: samples) {
            long delta=msg.getCreated()-msg.getSendCreated();
            if (delta<min) { min=delta; }
            if (delta>max) { max=delta; }
            deltas[i]=delta;
            if (msg.getSubId()>maxPacketId) { maxPacketId=msg.getSubId();}
            i++;
        }
        Arrays.sort(deltas);
        median = deltas[(66*samples.size()/100)];
        packetloss=(1.0-(samples.size()/maxPacketId))*100;
    }

    public void dumpResult(final File file, long  baseling) throws IOException {
        final BloatMessage lastSample= this.samples.get(this.samples.size() - 1);//TODO : sort op subId to handle OOO
        final BloatMessage firstSample= this.samples.get(0);
        final long nrofsamples=lastSample.getSubId();
        final long size=(lastSample.getTotalSize()+ BloatSamples.IP_HEADERSIZE)*nrofsamples;
        final long sendDeltaTime=lastSample.getSendCreated()-firstSample.getSendCreated();
        final long recievedeltaTime=lastSample.getCreated()-firstSample.getCreated();
        final double sendBandwidth=((double)size)*8/((double)sendDeltaTime/1000.0);
        final double recieveBandwidth=((double)size)*8/((double)recievedeltaTime/1000.0);

        //calculate send bandwidth , % packet loss
        final StringBuilder sampleInfo=new StringBuilder(1024);
        sampleInfo.append(this.id).append(";");
        sampleInfo.append(this.samples.size()).append(";");
        sampleInfo.append(this.min-baseling).append(";");
        sampleInfo.append(this.max-baseling).append(";");
        sampleInfo.append(this.median-baseling).append(";");
        sampleInfo.append(String.format("%.2f",this.packetloss)).append(";");
        sampleInfo.append(String.format("%.0f",sendBandwidth)).append(";");
        sampleInfo.append(String.format("%.0f",recieveBandwidth)).append(";");

        final Writer writer=new FileWriter(file, true);
        writer.write(sampleInfo.toString());
        System.out.println(sampleInfo.toString());
        writer.flush();
        writer.close();
    }

    public int getNumberOfMessages() {
        return this.samples.size();
    }

    public int getSizeOfMessages() {
        int size=0;
        for (final BloatMessage m: this.samples) {
            size=size+m.getTotalSize();
        }
        return size;
    }

    public long getMinimum() {
        return min;
    }
    public long getMaximum() {
        return max;
    }
    public long getMedian() {
        return median;
    }
    public double packetLos() {
        return packetloss;
    }

    public List<BloatMessage> getList() {
        return samples;
    }

    public BloatMessage get(int i) {
        return samples.get(i);
    }
}
