
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

package org.it4y;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luc on 6/13/14.
 *
 */
public class CliOptions {
    @Option(name="-bind", usage = "bind to ip" , required = false)
    private  String bindIp="0.0.0.0"; //default to any
    @Option(name="-port", usage = "set UDP port" , required = false)
    private  int port=9999;
    @Option(name="-pmtu", usage = "set PMTU & DF handling (-1=no change,0=DONT,1=ROUTE hints,2=DO,3=PROBE", required = false)
    private int pmtu=-1;
    @Option(name="-tos", usage = "Set tos value (0..255 or 0x00 ... 0xff)" , required = false)
    private String tos="0";
    @Option(name="-time", usage = "sample time in msec" , required = false)
    private  long sampleTime=500;
    @Option(name="-delay", usage = "delay between samples" , required = false)
    private  long sampleDelay=0;
    @Option(name="-endtime", usage = "time ot send end message" , required = false)
    private  long endTime=5000;
    @Option(name="-size", usage = "set UDP maximum message size" , required = false)
    private  int messageSize =1400;
    @Option(name="-mode",usage = "mode (client/server/dual) default=auto", required = false)
    private  String mode="auto";
    @Option(name="-fast",usage = "fast incremental mode", required = false)
    private  boolean fast=false;
    @Option(name="-fastFactor",usage = "fast increment factor", required = false)
    private  double fastFactor=1.1;
    @Option(name="-d",usage = "debug", required = false)
    private  boolean debug=false;
    @Option(name="-overhead",usage = "bytes overhead off tunneling per packet",required = false)
    private int overhead=0;
    @Option(name="-maxpackets",usage = "max packets per sample",required = false)
    private int maxpck=Integer.MAX_VALUE;
    @Option(name="-jni", usage = "enable advanced jni, requires ubuntu12.04 or higher", required = false)
    private boolean jni=false;
    @Option(name="-txbuffer" , usage= "tx buffersize in bytes")
    private int txbuffer=32768;
    @Option(name="-rxbuffer" , usage= "rx buffersize in bytes")
    private int rxbuffer=32768;
    @Option(name="-samples" , usage= "number of samples (default=100)")
    private int samples=100;

    @Argument(usage = "<remote host> <speed in kb/sec>")


    private final List<String> arguments = new ArrayList<String>();
    //my os ?
    private final String os=System.getProperty("os.name");

    public int getPort() {
        return this.port;
    }
    public String getBindIp() { return  this.bindIp; }
    public int getMessageSize() {
        return this.messageSize;
    }
    public String getMode() {
        return this.mode;
    }
    public boolean isFast() {
        return this.fast;
    }
    public double getFastFactor() {
        return this.fastFactor;
    }
    public boolean isDebug() {
        return this.debug;
    }
    public List<String> getArguments() {
        return this.arguments;
    }
    public long getSampleTime() {
        return this.sampleTime;
    }
    public long getEndTime() {
        return this.endTime;
    }
    public int getOverhead() {
        return this.overhead;
    }
    public int getMaxpck() {
        return this.maxpck;
    }
    public int getSamples() {
        return samples;
    }

    public long getSampleDelay() { return this.sampleDelay; }
    public int getPMTU() { return pmtu;}
    public int getTOS() {
        //parse hex or normal integer tos values
        if (tos.startsWith("0x")) {
            return Integer.parseInt(tos.substring(2),16);
        }
        return Integer.parseInt(tos);
    }
    public String getOs() {
        return os;
    }
    public boolean isLinux() {
        return os.equalsIgnoreCase("linux");
    }
    public boolean isJNIEnabled() {
        return jni;
    }

    public int getTxbuffer() {
        return txbuffer;
    }

    public int getRxbuffer() {
        return rxbuffer;
    }

    public CliOptions() { }
    public void parse(final String[] args) {
       final CmdLineParser parser=new CmdLineParser(this);
       try {
           parser.parseArgument(args);
       } catch (final CmdLineException e) {
           System.err.println(e.getMessage());
           parser.printUsage(System.err);
           System.exit(1);
       }
    }

    public String toString() {
        final StringBuilder stringBuilder=new StringBuilder(1024);
        stringBuilder.append(" OS: ").append(os).append("\n");
        stringBuilder.append(" mode: ").append(this.mode).append("\n");
        stringBuilder.append(" samples: ").append(samples).append("\n");
        stringBuilder.append(" bind: ").append(this.bindIp).append("\n");
        stringBuilder.append(" port: ").append(this.port).append("\n");
        stringBuilder.append(" PMTU setting: ").append(this.pmtu).append("\n");
        stringBuilder.append(" tos setting: ").append(this.tos).append("\n");
        stringBuilder.append(" tx buffer: ").append(this.txbuffer).append("\n");
        stringBuilder.append(" rx buffer: ").append(this.rxbuffer).append("\n");
        stringBuilder.append(" sample time: ").append(this.sampleTime).append(" msec\n");
        stringBuilder.append(" end time: ").append(this.endTime).append(" msec\n");
        stringBuilder.append(" max packets per sample: ").append(this.maxpck).append(" msec\n");
        stringBuilder.append(" overhead: ").append(this.overhead).append(" bytes\n");
        stringBuilder.append(" debug: ").append(this.debug).append("\n");
        stringBuilder.append(" fast increment: ").append(this.fast).append("\n");
        if (this.fast) {
            stringBuilder.append(" Fast factor: ").append(this.fastFactor).append("\n");
        }
        stringBuilder.append(" message size: ").append(this.messageSize).append("\n");
        if (arguments.size()>0) {
            stringBuilder.append("Remote server: ").append(arguments.get(0)).append("\n");
            if (arguments.size()>1) {
                stringBuilder.append("Bandwidth: ").append(arguments.get(1)).append(" kbits/sec\n");
            }
        }
        return stringBuilder.toString();
    }
}
