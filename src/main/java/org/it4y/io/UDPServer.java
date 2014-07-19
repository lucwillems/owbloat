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

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.it4y.CliOptions;
import org.it4y.Clock;
import org.it4y.metric.SamplAnalyserValues;
import org.it4y.metric.SampleAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Created by luc on 6/8/14.
 *
 */
public class UDPServer extends SimpleChannelInboundHandler<BloatMessage>implements Runnable {
        final Logger logger = LoggerFactory.getLogger(UDPServer.class);
        final int port;
        CliOptions options;
        private final ArrayList<BloatSamples> sampless=new ArrayList<BloatSamples>(100);
        private BloatSamples currentSample;
        long recieveStart=-1;
        private boolean finished=false;
        private long minBaseLine=Long.MAX_VALUE;

    public boolean isFinished() {
        return this.finished;
    }

    public UDPServer(CliOptions options) {
            this.port = options.getPort();
            this.options=options;
        }

        public void startServer() {
            this.logger.info("starting server on port {}", this.port);
        final EventLoopGroup group = new NioEventLoopGroup();
        try {
            final Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, false)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        public void initChannel(final DatagramChannel ch) throws Exception {
                            UDPServer.this.logger.info("init channel");
                            final ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new UDPSeverHandler());
                            pipeline.addLast(new BloatMessageCodec());
                            pipeline.addLast(UDPServer.this);
                        }
                    });
            b.option(ChannelOption.SO_SNDBUF, options.getTxbuffer());
            b.option(ChannelOption.SO_RCVBUF, options.getTxbuffer());
            b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            b.localAddress(this.port);
            final Channel channel = b.bind().syncUninterruptibly().channel();
            this.logger.info("channel: {}", channel);
            channel.closeFuture().await();
            this.logger.info("server finished.");
        } catch (final InterruptedException ignore) {
        } finally {
            group.shutdownGracefully();
        }
    }

    public void run() {
        this.startServer();
        this.logger.info("# of samples: {}", this.sampless.size());
        int msgcnt=0;
        int totalSize=0;
        for (final BloatSamples sample: this.sampless) {
            msgcnt=msgcnt+sample.getNumberOfMessages();
            totalSize=totalSize+sample.getSizeOfMessages();
        }
        this.logger.info("# of message recieved: {}", msgcnt);
        this.logger.info("Total size recieved: {}", totalSize);
        this.logger.info("done");
    }

    public void finishNow() {
        this.finished =true;
    }

    @Override
    protected void messageReceived(final ChannelHandlerContext ctx, final BloatMessage msg) throws Exception {
        if (msg.getId()==-1 && msg.getSubId()==(-1 & 0xffffff)) {
            this.logger.info("recieved end message");
            ctx.channel().close();
            this.finished =true;
            return;
        }

        if (this.currentSample ==null || this.currentSample.getId() != msg.getId()) {
            this.currentSample =new BloatSamples(msg.getId());
            this.sampless.add(this.currentSample);
            this.recieveStart =System.currentTimeMillis();
            msg.setRecievedStart(this.recieveStart);
            this.logger.info("new sample id {} {}", msg.getId(), Long.toHexString(Clock.DEFAULT.get24BitCurrentTimesMillis()));
        }
        if (msg.getPadSize()==0) {
            msg.setRecievedStart(msg.getRecieved());
        } else {
            msg.setRecievedStart(this.recieveStart);
        }
        this.currentSample.addSample(msg);
    }

    public void calculateSamples() {
        for (final  BloatSamples sample:this.sampless) {
            sample.calculateResults();
            if (sample.getMinimum()<minBaseLine) {
                minBaseLine=sample.getMinimum();
            }
        }
        this.logger.info("Min base line offset : {} msec",minBaseLine);
    }

    public void saveResultHeaderToFile(File file) throws IOException {
        final Writer writer=new FileWriter(file, true);
        String Header="id;size;send_pkt;rec_pkt;lost_pkt;ooo_pkts;ooo_events;ooo_max;owd_base;owd_median;owd_max;owd_min;owd_avg;bw_send;bw_rec\n";
        writer.write(Header);
        writer.close();
    }

    public void saveResultToFile(final File file,BloatSamples sample,SamplAnalyserValues metrics) throws IOException {
        final Writer writer=new FileWriter(file, true);
        //calculate send bandwidth , % packet loss
        final StringBuilder sampleInfo=new StringBuilder(1024);
        sampleInfo.append(sample.getId()).append(";");
        sampleInfo.append(metrics.getSize()).append(";");
        sampleInfo.append(metrics.getSendPkts()).append(";");
        sampleInfo.append(metrics.getRecievedPkts()).append(";");
        sampleInfo.append(metrics.getLostPkts()).append(";");
        sampleInfo.append(metrics.getOooPkts()).append(";");
        sampleInfo.append(metrics.getOooEvents()).append(";");
        sampleInfo.append(metrics.getOooMaxPosition()).append(";");
        sampleInfo.append(metrics.getBaseOffestTime()).append(";");
        sampleInfo.append(metrics.getOwdValues().getMedian()).append(";");
        sampleInfo.append(metrics.getOwdValues().getMaxValue()).append(";");
        sampleInfo.append(metrics.getOwdValues().getMinValue()).append(";");
        sampleInfo.append(metrics.getOwdValues().getAvgValue()).append(";");
        sampleInfo.append(metrics.getBandwidthSend()).append(";");
        sampleInfo.append(metrics.getBandwidthRecieved()).append(";");
        sampleInfo.append("\n");
        writer.write(sampleInfo.toString());
        writer.flush();
        writer.close();
    }

    public void saveSample() throws IOException {

        final File sendOutput= new File("/tmp/owbloat-send-result.csv");
        final File recieveOutput= new File("/tmp/owbloat-recieved-result.csv");
        final File resultOutput= new File("/tmp/owbloat-result.csv");
        calculateSamples();
        if (sendOutput.exists()) {
            sendOutput.delete();
        }
        if (recieveOutput.exists()){
            recieveOutput.delete();
        }
        if (resultOutput.exists()) {
            resultOutput.delete();
        }
        this.logger.info("save to {} & {} & {}", sendOutput, recieveOutput,resultOutput);
        saveResultHeaderToFile(resultOutput);
        for (final BloatSamples sample : this.sampless) {
                sample.saveSendToFile(sendOutput);
                sample.saveRecievedToFile(recieveOutput);
                SampleAnalyser analyser=new SampleAnalyser(sample,minBaseLine);
                analyser.analyse();
                SamplAnalyserValues metrics=analyser.getMetrics();
                logger.info("id: {} size: {} pkt send: {} pkt recieved: {} lost: {} Ooo Pkts: {} Ooo events: {} Max Ooo: {} Offet: {} owd: {} owdMax: {} owdMin: {}, up: {}, down: {}",
                        sample.getId(),
                        metrics.getSize(),
                        metrics.getSendPkts(),
                        metrics.getRecievedPkts(),
                        metrics.getLostPkts(),
                        metrics.getOooPkts(),
                        metrics.getOooEvents(),
                        metrics.getOooMaxPosition(),
                        metrics.getBaseOffestTime(),
                        metrics.getOwdValues().getMedian(),
                        metrics.getOwdValues().getMaxValue(),
                        metrics.getOwdValues().getMinValue(),
                        metrics.getBandwidthSend(),
                        metrics.getBandwidthRecieved());
               if (sample.getId()>0) {
                   saveResultToFile(resultOutput, sample, metrics);
               }
        }
    }
}

