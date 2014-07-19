package org.it4y.metric;

/**
 * Created by luc on 5/24/14.
 */
public class LongMetricValues {
    private final long avgValue;
    private final long minValue;
    private final long maxValue;
    private final long median;
    private final int samples;
    private final int medianPos;
    private final long timeStamp;

    public LongMetricValues(final int samples, final int medianPos, final long avgValue, final long minValue, final long maxValue, final long median) {

        this.samples = samples;
        this.medianPos = medianPos;
        this.avgValue = avgValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.median = median;
        this.timeStamp = System.currentTimeMillis();
    }

    public long getAvgValue() {
        return this.avgValue;
    }

    public long getMinValue() {
        return this.minValue;
    }

    public long getMaxValue() {
        return this.maxValue;
    }
    public long getMedian() {
        return this.median;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public int getSamples() {
        return this.samples;
    }

    public int getMedianPos() {return this.medianPos;}


}