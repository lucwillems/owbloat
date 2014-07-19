
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

package org.it4y.io;

import org.it4y.Clock;

/**
 * Created by luc on 6/8/14.
 *
 */
public class BloatMessage {
    public static final long MAGIC=0x1122334455667788L;
    private final int id;
    private int subId;
    private long send;
    private long recieved;
    private long recievedStart;
    private long sendStart;
    private long created;
    private long sendCreated;
    private Clock clock;

    public long getSendCreated() {
        return this.sendCreated;
    }

    public void setSendCreated(final long sendCreated) {
        this.sendCreated = sendCreated;
    }

    public long getCreated() {
        return this.created;
    }
    public void setCreated(long created) { this.created=created;};

    public long getRecievedStart() {
        return this.recievedStart;
    }

    public void setRecievedStart(final long recievedStart) {
        this.recievedStart = recievedStart;
    }

    public long getSendStart() {
        return this.sendStart;
    }

    public void setSendStart(final long sendStart) {
        this.sendStart = sendStart;
    }

    private int padSize;
    private int totalSize;

    public int getId() {
        return this.id;
    }

    public long getSend() {
        return this.send;
    }

    public void setSend(final long send) {
        this.send = send;
    }

    public long getRecieved() {
        return this.recieved;
    }

    public void setRecieved(final long recieved) {
        this.recieved = recieved;
    }

    public int getPadSize() {
        return this.padSize;
    }

    public void setPadSize(final int padSize) {
        this.padSize = padSize;
    }

    public int getTotalSize() {
        return this.totalSize;
    }
    public void setTotalSize(final int size) {
        this.totalSize=size;
    }
    public int getSubId() {
        return this.subId;
    }

    public void setSubId(final int subId) {
        this.subId = subId &0xffffff;
    } //only 24bits

    public long getOwd() {
        return this.recieved - this.send;
    }
    public long getSampleSendDelay() {
        return this.send - this.sendStart;
    }
    public long getSampleRecieveDelay() {
        return this.recieved - this.recievedStart;
    }

    public BloatMessage(final int id) {
        this.id=id;
        this.created=Clock.DEFAULT.get24BitCurrentTimesMillis();
    }

    @Override
    public String toString() {
        return "id: " + this.id + " subId: " + this.subId + " pad: " + this.padSize + " owd: " + this.getOwd() + " send delay: " + this.getSampleSendDelay() + " recieve delay: " + this.getSampleRecieveDelay();
    }
}
