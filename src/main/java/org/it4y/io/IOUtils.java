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
