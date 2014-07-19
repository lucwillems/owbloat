package org.it4y.io;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

/**
 * Created by luc on 6/8/14.
 *
 */
public class UDPSeverHandler extends ChannelHandlerAdapter {


    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        ctx.fireChannelRead(((DatagramPacket) msg).content());
    }

}
