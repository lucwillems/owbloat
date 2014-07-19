package org.it4y.metric;

import org.it4y.io.BloatMessage;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by luc on 6/22/14.
 */
public class SampleAnalyserTest {

    @Test
    public void testSampleAnalyserTimeWrap() {
        SampleAnalyser sampleAnalyser=new SampleAnalyser(66,0);
        BloatMessage msg1=new BloatMessage(0);
        sampleAnalyser.doAnalyse(msg1);

        BloatMessage msg2=new BloatMessage(0);
        msg2.setSendCreated(0xFFFFFFL);
        msg2.setCreated(0);
        sampleAnalyser.doAnalyse(msg2);
        Assert.assertEquals(sampleAnalyser.owd.getSamples(),1);
    }

    @Test
    public void testSampleAnalyserSubIdWrap() {
        SampleAnalyser sampleAnalyser=new SampleAnalyser(66,0);
        BloatMessage msg1=new BloatMessage(0);
        msg1.setSubId(Integer.MAX_VALUE);
        sampleAnalyser.doAnalyse(msg1);

        BloatMessage msg2=new BloatMessage(0);
        msg2.setSubId(Integer.MAX_VALUE+1);
        sampleAnalyser.doAnalyse(msg2);
        BloatMessage msg3=new BloatMessage(0);
        msg3.setSubId(Integer.MAX_VALUE+2);
        sampleAnalyser.doAnalyse(msg3);
        Assert.assertEquals(sampleAnalyser.owd.getSamples(),2);
    }

//    @Test
    public void testSampleAnalyserPktLost() {
        SampleAnalyser sampleAnalyser=new SampleAnalyser(66,0);
        for (int i=0;i<10;i++) {
            BloatMessage msg1 = new BloatMessage(0);
            msg1.setSendCreated(0);      // All packets are send on T0
            msg1.setCreated(1000+i*2);   // and recieved on T0+1000 + delta time(i*2)
            msg1.setSubId(i * 2);
            sampleAnalyser.doAnalyse(msg1);
        }
        SamplAnalyserValues result=sampleAnalyser.getMetrics();
        Assert.assertEquals(19,result.getSendPkts());
        Assert.assertEquals(10,result.getRecievedPkts());
        Assert.assertEquals(9,result.getLostPkts());
        Assert.assertEquals(0,result.getOooEvents());
        Assert.assertEquals(0,result.getOooMaxPosition());
        Assert.assertEquals(0,result.getOooPkts());
        Assert.assertEquals(1000,result.getBaseOffestTime());
        Assert.assertNotNull(result.getOwdValues());
        LongMetricValues owd=result.getOwdValues();
        Assert.assertEquals(9,owd.getSamples());
        Assert.assertEquals(18,owd.getMaxValue());
        Assert.assertEquals(2,owd.getMinValue());
        Assert.assertEquals(10,owd.getAvgValue());
        Assert.assertEquals(12,owd.getMedian());

    }
}
