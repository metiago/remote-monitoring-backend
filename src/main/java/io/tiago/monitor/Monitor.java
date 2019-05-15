package io.tiago.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
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

        Duration sec = Duration.of(node.getExpire(), ChronoUnit.SECONDS);

        LocalTime expireAt = LocalTime.now().plus(sec);

        while (isInTimeRange(node)) {

            LOGGER.debug(String.format("Checking address %s:%s", this.node.getHost(), this.node.getPort()));

            try (Socket socket = new Socket()) {

                TimeUnit.SECONDS.sleep(node.getPollFrequency());

                SocketAddress socketAddress = new InetSocketAddress(this.node.getHost(), this.node.getPort());
                socket.connect(socketAddress);
                this.node.setUp(true);
            }
            catch (Exception e) {
                node.setUp(false);
            }

            LocalTime now = LocalTime.now();
            if(now.isAfter(expireAt)) {
                LOGGER.debug(String.format("Time's up at %s", now));
                break;
            }
        }

        save(node);
    }

    private void waitExecution() {

        while (isTimeScheduled(this.node)) {

            LOGGER.debug(String.format("Waiting to check address %s:%s", this.node.getHost(), this.node.getPort()));

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

    private void save(Node node) {
        LOGGER.debug(String.format("Saving node %s", this.node));
        MemoryDb db = MemoryDb.instance();
        db.add(node);
    }
}
