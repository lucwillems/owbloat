package org.it4y.io;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

/**
 * Created by luc on 6/8/14.
 *
 */
public class UDPClientHandler extends ChannelHandlerAdapter {

    private final InetSocketAddress remote;
    public UDPClientHandler(final InetSocketAddress remote) {
        this.remote=remote;
    }

    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) throws Exception {
        ctx.write(new DatagramPacket((ByteBuf) msg, this.remote));
    }
}
