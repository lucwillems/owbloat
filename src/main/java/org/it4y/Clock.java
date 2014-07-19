package org.it4y;

/**
 * Created by luc on 6/21/14.
 */
public class Clock {
    public static Clock DEFAULT=new Clock();

    Clock() {}

    public long get24BitCurrentTimesMillis() {
        return System.currentTimeMillis() & 0xffffffL;
    }

    public long get24BitnanoTime() {
        return System.nanoTime() & 0xffffffL;
    }

}
