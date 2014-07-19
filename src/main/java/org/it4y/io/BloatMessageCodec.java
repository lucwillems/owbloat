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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by luc on 6/8/14.
 *
 */
public class BloatMessageCodec extends ByteToMessageCodec<BloatMessage> {
    public static final int HEADERSIZE=8+4+4+8+8+8+4; //see fixed fields in encode !!!
    public static final int PADSIZE_POSITION= BloatMessageCodec.HEADERSIZE -4;
    private final Logger logger= LoggerFactory.getLogger(BloatMessage.class);

    public BloatMessageCodec() {
        this.logger.info("init: header size{}", BloatMessageCodec.HEADERSIZE);
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final BloatMessage msg, final ByteBuf out) throws Exception {
        msg.setSend(System.nanoTime());
        out.writeLong(BloatMessage.MAGIC);
        out.writeInt(msg.getId());
        out.writeInt(msg.getSubId());
        out.writeLong(msg.getSendCreated());
        out.writeLong(msg.getSend());
        out.writeLong(msg.getSendStart());
        //should be last field in header
        out.writeInt(msg.getPadSize());
        //we pad on int size
        for (int x=0;x<msg.getPadSize()/4;x++) {
            out.writeInt(x);
        }
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        //When we are triggerd , it is not sure we will recieve enough bytes or correct message.
        //so we must be carefull in seeing we have enough bytes and correct message format before continueing

        //we need a minimum of size before we start
        final int size=in.readableBytes();
        if (size < BloatMessageCodec.HEADERSIZE) {
            this.logger.debug("message to small: {}", size);
            return;
        }

        //Search padding size and MAGIC to check for valid message and chekc total size before decoding
        final long x=in.getLong(0);
        final int y=in.getInt(BloatMessageCodec.PADSIZE_POSITION);

        //check if magic is there if not, skip 1 byt and try again
        //this probebly means we are out of sync or somebody is sending bad messages...
        if ( x!= BloatMessage.MAGIC) {
            this.logger.error("invalid message found 0x{}!=0x{}", Long.toHexString(x), Long.toHexString(BloatMessage.MAGIC));
            in.readByte();
            return;
        }

        //check for complete sized messages
        if (in.readableBytes()< BloatMessageCodec.HEADERSIZE +y) {
            this.logger.debug("not enough bytes to read {} message, skipping", BloatMessageCodec.HEADERSIZE + y);
            return;
        }

        //Message must start with MAGIC
        if (in.readLong()==BloatMessage.MAGIC) {
                final long recieved = System.nanoTime();
                final int id = in.readInt();
                final BloatMessage msg = new BloatMessage(id);
                msg.setTotalSize(BloatMessageCodec.HEADERSIZE +y);
                msg.setSubId(in.readInt());
                msg.setSendCreated(in.readLong());
                msg.setSend(in.readLong());
                msg.setSendStart(in.readLong());
                msg.setRecieved(recieved);
                msg.setPadSize(in.readInt());
                //in case or message is truncated
                final int padsize=msg.getPadSize();
                //we pad on int size
                for (int i = 0; i < (padsize/4); i++) {
                    in.readInt();
                }
                out.add(msg);
            this.logger.debug("message: {}", msg);
        } else {
            this.logger.error("invalid message, should not happen");
        }
    }
}