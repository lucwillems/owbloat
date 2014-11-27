/*
 * GNU GENERAL PUBLIC LICENSE
 * Copyright 2014 Luc Willems (T.M.M.)
 *
 * Version 2, June 1991
 *
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 *
 *   http://www.gnu.org/licenses/gpl-2.0.html
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.it4y.metric;

/**
 * Created by luc on 8/9/14.
 */
public class ExecutionTimer {
        private long hitCounter = 0;
        private long Time = 0;
        private long totalTime=0;
        private long maxTime = 0;
        private long minTime = 0;
        private long startTime;

        public ExecutionTimer() {
        }

        public void startTask() {
            this.startTime = System.nanoTime();
        }

        public void endTask() {
            //In a multicore CPU environment, we could get negative time in case we jump from 1 cpu core to am other
            //nanoTime in linux is based on clock_getTime(CLOCK_MONOTONIC,x) , more info see google and man pages
            //in that case we ignore delta as time is always moving forward according to Einstien
            final long delta = System.nanoTime() - this.startTime;
            if (delta>-1) {
                if (hitCounter==0) {
                    //startup initialization after first endTask
                    this.Time = delta;
                    this.totalTime=delta;
                    this.minTime = delta;
                    this.maxTime = delta;
                } else {
                    this.Time =delta;
                    this.totalTime=totalTime+delta;
                    if (minTime>delta) { minTime=delta;}
                    if (maxTime<delta) { maxTime=delta;}
                }
                this.hitCounter++;
            }
        }

        /**
         * reset min/max values
         */
        public void reset() {
            maxTime=0L;
            minTime= Time;
            hitCounter=0;
        }

        public long getMaximumTime() {
            return this.maxTime;
        }

        public long getMinimumTime() {
            return this.minTime;
        }

        public long getTime() {
            return this.Time;
        }

        public long getHitCounter() {
            return this.hitCounter;
        }

        @Override
        public String toString() {
            if (hitCounter>0) {
                return "Timer: avg=" + (this.totalTime / 1000) / hitCounter + " min=" + this.minTime / 1000 + " max=" + this.maxTime / 1000 + " usec, hits=" + hitCounter;
            } else {
                return "Timer: no hits";
            }
        }
}
