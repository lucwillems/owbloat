package org.it4y.io;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.it4y.net.JVMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.nio.channels.SocketChannel;
import java.nio.channels.DatagramChannel;

/**
 * Created by luc on 6/15/14.
 */
public class IOUtils {

        private static final Logger LOGGER = LoggerFactory.getLogger(IOUtils.class);
        private static final Method NSC_JAVA_CHANNEL_IMPL;
        private static final Method NDC_JAVA_CHANNEL_IMPL;

        static {
            IOUtils.LOGGER.info("init IOUtils");
            try {
                /**
                 * This greatly depends on JVM implementations !!!
                 * we need access to internal fd of the socket/file. to do that we need
                 * to access private fields :-(
                 * This will fail if someone changes the little details
                 * tested with JDK1.7 and 1.6
                 */
                NSC_JAVA_CHANNEL_IMPL = NioSocketChannel.class.getDeclaredMethod("javaChannel");
                IOUtils.NSC_JAVA_CHANNEL_IMPL.setAccessible(true);
                NDC_JAVA_CHANNEL_IMPL = NioDatagramChannel.class.getDeclaredMethod("javaChannel");
                IOUtils.NDC_JAVA_CHANNEL_IMPL.setAccessible(true);
            } catch (final Throwable t) {
                //If we get here, we have a serious problem and can not continue
                //so throw a runtime exception to notify
                IOUtils.LOGGER.error("Reflection error on SocketOptions: ", t.getMessage());
                throw new JVMException(t);
            }
        }

        private static SocketChannel javaChannel(final Channel channel) {
            try {
                return (SocketChannel) IOUtils.NSC_JAVA_CHANNEL_IMPL.invoke(channel);
            } catch (final Exception ignore) {
                IOUtils.LOGGER.error("ooeps... : ", ignore);
            }
            return null;
        }

        public static SocketChannel getSocketChannel(final Channel channel) {
            try {
                if (channel instanceof NioSocketChannel) {
                    //we need to do some nasty stuff here
                    return (SocketChannel) IOUtils.NSC_JAVA_CHANNEL_IMPL.invoke(channel);
                }
                if (channel instanceof NioDatagramChannel) {
                    return (SocketChannel) IOUtils.NDC_JAVA_CHANNEL_IMPL.invoke(channel);
                }
            } catch (final Exception ignore) {
                IOUtils.LOGGER.error("Ooeps... : ",ignore);
            }
            return null;
        }

    public static DatagramChannel getDatagramChannel(final Channel channel) {
        try {
            if (channel instanceof NioDatagramChannel) {
                return (java.nio.channels.DatagramChannel) IOUtils.NDC_JAVA_CHANNEL_IMPL.invoke(channel);
            }
        } catch (final Exception ignore) {
            IOUtils.LOGGER.error("Ooeps... : ",ignore);
        }
        return null;
    }

}
