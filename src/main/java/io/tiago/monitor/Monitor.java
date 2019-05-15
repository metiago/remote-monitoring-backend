package io.tiago.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public class Monitor implements Runnable {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(Monitor.class);

    private static final int MAX_INTERVAL_TIME_IN_SEC = 2;

    private Node node;

    public Monitor(Node node) {
        this.node = node;
    }

    @Override
    public void run() {

        waitExecution();

        long expireAt = node.getExpire();

        while (isInTimeRange(node) && expireAt > 0) {

            LOGGER.info(String.format("Checking address %s:%s", this.node.getHost(), this.node.getPort()));

            try (Socket socket = new Socket()) {

                TimeUnit.SECONDS.sleep(node.getPollFrequency());

                SocketAddress socketAddress = new InetSocketAddress(this.node.getHost(), this.node.getPort());
                socket.connect(socketAddress);
                this.node.setUp(true);
            }
            catch (Exception e) {
                node.setUp(false);
            }

            expireAt--;
        }

        LOGGER.info(String.format("Address %s:%s is %s", this.node.getHost(), this.node.getPort(), this.node.isUp()));
    }

    private void waitExecution() {

        while (isTimeScheduled(this.node)) {

            LOGGER.info(String.format("Waiting to check address %s:%s", this.node.getHost(), this.node.getPort()));

            try {
                TimeUnit.SECONDS.sleep(MAX_INTERVAL_TIME_IN_SEC);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isTimeScheduled(Node node) {
        LocalTime now = LocalTime.now();
        return now.isBefore(node.getStart());
    }

    private boolean isInTimeRange(Node node) {
        LocalTime now = LocalTime.now();
        return now.isAfter(node.getStart()) && now.isBefore(node.getEnd());
    }
}
