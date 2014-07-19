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

import com.google.common.util.concurrent.RateLimiter;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.it4y.CliOptions;
import org.it4y.jni.libc;
import org.it4y.jni.linux.in;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Created by luc on 6/8/14.
 *
 */
public class UDPClient {
        final Logger logger = LoggerFactory.getLogger(UDPClient.class);
        long cnt;
        int prevInterval;
        long mesageIntervalNsec;
        final CliOptions options;
        DatagramChannel channel;
        final InetSocketAddress server;
        RateLimiter rateLimiter;
        private int maxMessageSize;


    public void setMessageRate(final long rateinKbs) {
        //calculate max sending speed in bytes/sec for rate limiter
        long bytesperSec=(rateinKbs*1024/8);
        rateLimiter=RateLimiter.create(bytesperSec,1, TimeUnit.MILLISECONDS);
        maxMessageSize= (int) (this.options.getMessageSize()+BloatSamples.IP_HEADERSIZE+ this.options.getOverhead());
        this.mesageIntervalNsec =1000000000L* maxMessageSize*8L/(rateinKbs*1024L);
        this.logger.info("send rate: {} kbits/sec", rateinKbs);
        this.logger.info("delay per packet: {} nsec", this.mesageIntervalNsec);
    }
    public UDPClient(final InetSocketAddress server, final CliOptions options) throws InterruptedException {
            this.server = server;
            this.options=options;
            this.setupChannel();
        }

    public void setupChannel() throws InterruptedException {
        final EventLoopGroup group = new NioEventLoopGroup();
        final Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    @Override
                    public void initChannel(final DatagramChannel ch) throws Exception {
                        UDPClient.this.logger.info("init channel : {}", ch);
                        final ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new UDPClientHandler(UDPClient.this.server));
                        pipeline.addLast(new BloatMessageCodec());
                    }
                });
        b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        b.option(ChannelOption.IP_TOS,options.getTOS());
        b.option(ChannelOption.SO_SNDBUF, options.getTxbuffer());
        b.option(ChannelOption.SO_RCVBUF, options.getRxbuffer());
        this.logger.info("bind...");
        this.channel = (DatagramChannel) b.bind(new InetSocketAddress("0.0.0.0",0)).channel();
        this.logger.info("channel: {} {}", this.channel.getClass().getSimpleName(),this.channel);
        //Linux only code here
        //PMTU handling : linux only code on ubuntu >=12.04
        if (options.isLinux() && options.isJNIEnabled()) {
            try {
                logger.info("channel PMTU setting : {}", org.it4y.jni.linuxutils.getintSockOption(IOUtils.getDatagramChannel(this.channel), in.IPPROTO_IP, in.IP_MTU_DISCOVER));
            } catch (libc.ErrnoException e) {
                logger.info("linux only: {}", e);
            }
            if (options.getPMTU() > -1) {
                try {
                    org.it4y.jni.linuxutils.setintSockOption(IOUtils.getDatagramChannel(channel), in.IPPROTO_IP, in.IP_MTU_DISCOVER, options.getPMTU());
                    logger.info("channel PMTU setting changed to {}", org.it4y.jni.linuxutils.getintSockOption(IOUtils.getDatagramChannel(this.channel), in.IPPROTO_IP, in.IP_MTU_DISCOVER));
                } catch (libc.ErrnoException e) {
                    logger.error("Could not set DF: {}", e);
                }
            }
        }
    }

    private int nextInterval(final int i) {
        int result;
        if (this.options.isFast()) {
            result= (int)((double) i * this.options.getFastFactor())+1;
            this.logger.debug("interval: {} prev: {}", result, this.prevInterval);
            if (result>99 && this.prevInterval <100) {
                result=100;
            }
            this.prevInterval =result;
        } else {
            result=i+1;
        }
        return result;
    }

    public void sendMessages() throws InterruptedException {
        this.cnt++;
        for (int i=1;i<=20;i++) {
            final BloatMessage msg=new BloatMessage(0);
            msg.setSubId(i);
            msg.setSendCreated(msg.getCreated());
            msg.setSendStart(System.nanoTime());
            this.channel.writeAndFlush(msg);
            //this is not really needed but warms up the rateLimiter
            rateLimiter.acquire(maxMessageSize);
            Thread.sleep(100);
        }
        Thread.sleep(100);
        int interval=0;
        while ((interval= this.nextInterval(interval))<=options.getSamples()) {
            final int padSize=Math.min(this.options.getMessageSize()-BloatMessageCodec.HEADERSIZE, interval * 50);
            this.logger.info("send batch {}/100 padS size: {}", interval, padSize);
            int x=0;
            final long sendStart=System.nanoTime();
            //each sample takes 500 msec default
            final long endTime=System.currentTimeMillis()+ this.options.getSampleTime();
            while(System.currentTimeMillis()<endTime) {
                //as size increases we will slowly go to full speed rate. packets are evenly spaced in time
                rateLimiter.acquire(maxMessageSize);
                x++;
                final BloatMessage msg=new BloatMessage(interval);
                msg.setSendCreated(msg.getCreated());
                msg.setSubId(x);
                msg.setSendStart(sendStart);
                msg.setPadSize(padSize);
                this.channel.writeAndFlush(msg);
                //break loop when we have max packets
                if (x> this.options.getMaxpck()) {
                    break;
                }
            }
            if (this.options.getSampleDelay() >0) {
                Thread.sleep(this.options.getSampleDelay());
            }
        }
        Thread.sleep(2000);
        //send end message
        this.logger.info("send end message for {} msec", this.options.getEndTime());
        final long startEnd=System.currentTimeMillis();
        while(System.currentTimeMillis()<startEnd+ this.options.getEndTime()) {
            final BloatMessage msg=new BloatMessage(-1);
            msg.setSubId(-1);
            this.channel.writeAndFlush(msg);
            Thread.sleep(100);//no high rates please
        }
        Thread.sleep(1000);
        this.logger.info("client done");
        this.channel.close();
        this.channel.eventLoop().shutdownGracefully();
    }
}

