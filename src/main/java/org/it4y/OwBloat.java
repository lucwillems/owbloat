package org.it4y;

import io.netty.util.ResourceLeakDetector;
import org.it4y.io.UDPClient;
import org.it4y.io.UDPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by luc on 6/8/14.
 *
 */
public class OwBloat {

        static Logger logger;
        static UDPServer server=null;
        static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(10);

        public static void main(final String[] args) throws InterruptedException, IOException {
            final CliOptions cliOptions;

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    if (OwBloat.server !=null && !OwBloat.server.isFinished()) {
                        OwBloat.logger.info("Quit using shutdown hook");
                        OwBloat.server.finishNow();
                        try {
                            Thread.sleep(1000);
                        } catch (final InterruptedException ignore) {}
                        try {
                            OwBloat.server.saveSample();
                        } catch (final IOException e) {
                            logger.error("error: {}", e);
                        }
                        OwBloat.logger.info("done");
                    }
                }
            });
            cliOptions=new CliOptions();
            cliOptions.parse(args);
            if (cliOptions.isDebug()) {
                System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY,"DEBUG");
                OwBloat.logger = LoggerFactory.getLogger(OwBloat.class);
                ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
            } else {
                OwBloat.logger = LoggerFactory.getLogger(OwBloat.class);
            }
            OwBloat.logger.info("config:\n{}", cliOptions);
            if (cliOptions.getMode().equalsIgnoreCase("auto")) {
                    if (cliOptions.getArguments().size() > 0) {
                        OwBloat.logger.info("start client...");
                        final String client = cliOptions.getArguments().get(0);
                        OwBloat.logger.info("Remote server : {}", client);
                        final UDPClient udpClient = new UDPClient(new InetSocketAddress(client, cliOptions.getPort()), cliOptions);
                        if (cliOptions.getArguments().size() > 1) {
                            final long messageRate = Long.parseLong(cliOptions.getArguments().get(1));
                            udpClient.setMessageRate(messageRate);
                        }
                        OwBloat.logger.info("started owbloat");
                        udpClient.sendMessages();
                    } else {
                        OwBloat.logger.info("start server...");
                        OwBloat.server = new UDPServer(cliOptions);
                        OwBloat.EXECUTOR.execute(OwBloat.server);
                        while (!OwBloat.server.isFinished()) {
                            Thread.sleep(5000);
                        }
                        OwBloat.server.saveSample();
                    }
            }
            if (cliOptions.getMode().equalsIgnoreCase("server") || cliOptions.getMode().equalsIgnoreCase("dual")) {
                OwBloat.logger.info("start server...");
                OwBloat.server = new UDPServer(cliOptions);
                OwBloat.EXECUTOR.execute(OwBloat.server);
            }
            if (cliOptions.getMode().equalsIgnoreCase("client") || cliOptions.getMode().equalsIgnoreCase("dual")) {
                if (cliOptions.getArguments().size() > 0) {
                    OwBloat.logger.info("start client...");
                    final String client = cliOptions.getArguments().get(0);
                    OwBloat.logger.info("Remote server : {}", client);
                    final UDPClient udpClient = new UDPClient(new InetSocketAddress(client, cliOptions.getPort()),cliOptions);
                    if (cliOptions.getArguments().size() > 1) {
                        final long messageRate = Long.parseLong(cliOptions.getArguments().get(1));
                        udpClient.setMessageRate(messageRate);
                    }
                    OwBloat.logger.info("started owbloat");
                    udpClient.sendMessages();
                }
            }
            if (cliOptions.getMode().equalsIgnoreCase("server") || cliOptions.getMode().equalsIgnoreCase("dual")) {
                while (!OwBloat.server.isFinished()) {
                    Thread.sleep(5000);
                }
                OwBloat.server.saveSample();
            }
            OwBloat.logger.info("test finished");
            OwBloat.EXECUTOR.shutdownNow();
        }
}
