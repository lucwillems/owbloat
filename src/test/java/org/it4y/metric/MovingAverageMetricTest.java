package org.it4y.metric;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by luc on 6/22/14.
 */
public class MovingAverageMetricTest {
    private Logger logger= LoggerFactory.getLogger(MovingAverageMetricTest.class);

    @Test
    public void testMovingAverageSingleSample() {
        LongMovingStatistics movingAverageMetric = new LongMovingStatistics(100,50);
        Assert.assertNotNull(movingAverageMetric);
        movingAverageMetric.addValue(10);
        LongMetricValues result=movingAverageMetric.getMetric();
        Assert.assertEquals(1,result.getSamples());
        Assert.assertEquals(10,result.getMinValue());
        Assert.assertEquals(10,result.getMaxValue());
        Assert.assertEquals(10,result.getAvgValue());
        Assert.assertEquals(10, result.getMedian());
    }

    @Test
    public void testMovingAverageNoSample() {
        LongMovingStatistics movingAverageMetric = new LongMovingStatistics(100,50);
        Assert.assertNotNull(movingAverageMetric);
        LongMetricValues result=movingAverageMetric.getMetric();
        Assert.assertEquals(0,result.getSamples());
        Assert.assertEquals(0,result.getMinValue());
        Assert.assertEquals(0,result.getMaxValue());
        Assert.assertEquals(0,result.getAvgValue());
        Assert.assertEquals(0, result.getMedian());
    }

    @Test
    public void testMovingAverageOddSamples() {
        LongMovingStatistics movingAverageMetric = new LongMovingStatistics(100,50);
        Assert.assertNotNull(movingAverageMetric);
        movingAverageMetric.addValue(10);
        movingAverageMetric.addValue(20);
        movingAverageMetric.addValue(30);
        //check results
        LongMetricValues result=movingAverageMetric.getMetric();
        Assert.assertEquals(3,result.getSamples());
        Assert.assertEquals(10,result.getMinValue());
        Assert.assertEquals(30,result.getMaxValue());
        Assert.assertEquals(20,result.getAvgValue());
        Assert.assertEquals(20,result.getMedian());
    }

    @Test
    public void testMovingAverageEvenSamples() {
        LongMovingStatistics movingAverageMetric = new LongMovingStatistics(100,50);
        Assert.assertNotNull(movingAverageMetric);
        movingAverageMetric.addValue(10);
        movingAverageMetric.addValue(20);
        movingAverageMetric.addValue(30);
        movingAverageMetric.addValue(40);
        //check results
        LongMetricValues result=movingAverageMetric.getMetric();
        Assert.assertEquals(4,result.getSamples());
        Assert.assertEquals(10,result.getMinValue());
        Assert.assertEquals(40,result.getMaxValue());
        Assert.assertEquals(25,result.getAvgValue());
        Assert.assertEquals(25,result.getMedian());
    }

    @Test
    public void testMovingAverageMutchSamples() {
        LongMovingStatistics movingAverageMetric = new LongMovingStatistics(100,50);
        Assert.assertNotNull(movingAverageMetric);
        movingAverageMetric.addValue(10);
        movingAverageMetric.addValue(10);
        movingAverageMetric.addValue(10);
        movingAverageMetric.addValue(70);
        //check results
        LongMetricValues result=movingAverageMetric.getMetric();
        Assert.assertEquals(4,result.getSamples());
        Assert.assertEquals(10,result.getMinValue());
        Assert.assertEquals(70,result.getMaxValue());
        Assert.assertEquals(25,result.getAvgValue());
        Assert.assertEquals(10,result.getMedian());
    }

    @Test
    public void testMovingAverageMovingSamples() {
        LongMovingStatistics movingAverageMetric = new LongMovingStatistics(100,50);
        Assert.assertNotNull(movingAverageMetric);
        for (long i=0;i<1000;i++) {
            movingAverageMetric.addValue(i);
        }
        //check results
        LongMetricValues result=movingAverageMetric.getMetric();
        Assert.assertEquals(100,result.getSamples());
        Assert.assertEquals(0,result.getMinValue());
        Assert.assertEquals(999,result.getMaxValue());
        Assert.assertEquals(949,result.getAvgValue());
        Assert.assertEquals(949,result.getMedian());
    }

    @Test
    public void testMovingAverageMovingRangeSamples() {
        LongMovingStatistics movingAverageMetric = new LongMovingStatistics(100,50);
        Assert.assertNotNull(movingAverageMetric);
        long startTime=System.currentTimeMillis();
        for (long i=-50L;i<50L;i++) {
            movingAverageMetric.addValue(i);
        }
        long endTime=System.currentTimeMillis();
        //check results
        LongMetricValues result=movingAverageMetric.getMetric();
        Assert.assertEquals(100,result.getSamples());
        Assert.assertEquals(-50,result.getMinValue());
        Assert.assertEquals(49,result.getMaxValue());
        Assert.assertEquals(0,result.getAvgValue());
        Assert.assertEquals(0,result.getMedian());
    }
    @Test
    public void testMovingAverageMovingPerformanceAddValue() {
        LongMovingStatistics movingAverageMetric = new LongMovingStatistics(100,50);
        Assert.assertNotNull(movingAverageMetric);
        long startTime=System.currentTimeMillis();
        for (long i=-1000000L;i<1000001L;i++) {
            movingAverageMetric.addValue(i);
        }
        long endTime=System.currentTimeMillis();
        double usec=(double)(endTime-startTime)/2000;
        logger.info("2000000 addValue test : {} msec , {} usec per iteration",endTime-startTime,usec);
        Assert.assertTrue(usec<1); // shoud be faster than 1 usec
        //check results
        LongMetricValues result=movingAverageMetric.getMetric();
        Assert.assertEquals(100,result.getSamples());
        Assert.assertEquals(-1000000,result.getMinValue());
        Assert.assertEquals(1000000,result.getMaxValue());
        Assert.assertEquals(999950,result.getAvgValue());
        Assert.assertEquals(999950,result.getMedian());
    }

    @Test
    public void testMovingAverageMovingPerformanceMetric() {
        LongMovingStatistics movingAverageMetric = new LongMovingStatistics(100,50);
        Assert.assertNotNull(movingAverageMetric);
        for (long i=-50L;i<51L;i++) {
            movingAverageMetric.addValue(i);
        }
        long startTime=System.currentTimeMillis();
        for (long i=0;i<10000;i++){
            for (long y=-50L;y<51L;y++) {
                movingAverageMetric.addValue(y);
            }
            LongMetricValues result=movingAverageMetric.getMetric();
        }
        long endTime=System.currentTimeMillis();
        double usec=(double)(endTime-startTime)/10;
        logger.info("10000 getMetric test : {} msec , {} usec per iteration",endTime-startTime,usec);
        //getMetric will create a lock and iterate the list, so this takes some time
        Assert.assertTrue(usec<50); // shoud be faster than 50 usec
    }



}
