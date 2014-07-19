package org.it4y.metric;

import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by luc on 5/24/14.
 */
public class LongMovingStatistics {

    private final Lock lock=new ReentrantLock();
    protected final long[] buffer;
    protected int cnt;
    protected long avg;
    protected long median;
    protected int medianPoint;
    protected long maxValue;
    protected long minValue;

    public LongMovingStatistics(final int size, int medianPoint) {
        if (size < 2) {
            throw new IllegalArgumentException("Buffer size "+size+" must be 2 or greater");
        }
        this.buffer = new long[size];
        setMedianPoint(medianPoint);
    }

    /*
     * @param : add value to internal buffer
     *          oldest value will be removed
     */
    public void addValue(final long x) {
        // shift buffer + add
        if (this.lock.tryLock()) {
            this.cnt++;
            if (this.cnt ==1) {
                this.maxValue =x;
                this.minValue =x;
            } else {
                if (x > this.maxValue) {
                    this.maxValue = x;
                }
                if (x < this.minValue) {
                    this.minValue = x;
                }
            }
            // shift buffer + add
            System.arraycopy(this.buffer, 0, this.buffer, 1, this.buffer.length - 1);
            this.buffer[0] = x;
            this.lock.unlock();
        }
    }

    public int getMedianPoint() {
        return medianPoint;
    }

    public void setMedianPoint(int medianPoint) {
        this.medianPoint=Math.max(10,Math.min(90,medianPoint));//medianPoint must be limited between 10..90
    }

    public int getSamples()  {
        this.lock.lock();
        try {
            return Math.min(this.buffer.length, cnt);
        } finally {
            this.lock.unlock();
        }
    }

    public synchronized LongMetricValues getMetric() {
        final LongMetricValues result;
        this.lock.lock();
        try {
            //Calculate Mean value every length samples
            int samples = Math.min(this.buffer.length, cnt);
            int middle = ((samples * medianPoint) / 100);
            Arrays.sort(this.buffer, 0, samples);
            if (samples % 2 == 0 && samples > 1) {
                this.median = (this.buffer[middle] + this.buffer[middle-1])/2;
            } else {
                this.median = this.buffer[middle];
            }
            avg=0;
            if (samples>0) {
                for (int i = 0; i < samples; i++) {
                    avg = avg + this.buffer[i];
                }
                avg = avg / samples;
            }
            result = new LongMetricValues(samples,middle, this.avg, this.minValue, this.maxValue, this.median);
            //reset
            this.cnt=0;
            this.maxValue=0;
            this.minValue=0;
            return result;
        } finally {
            this.lock.unlock();
        }
    }
}
