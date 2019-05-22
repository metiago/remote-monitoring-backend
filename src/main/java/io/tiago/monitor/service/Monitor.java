package io.tiago.monitor.service;

import io.tiago.monitor.domain.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class Monitor implements Runnable {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(Monitor.class);

    // TODO Make it configurable
    private static final int MAX_INTERVAL_TIME_IN_SEC = 2;

    private Node node;

    private ZoneId zoneId;

    public Monitor(Node node) {
        this.node = node;
        if(node.getTimeZone() == null) {
            this.zoneId = ZoneId.of(ZoneId.systemDefault().toString());
        } else {
            this.zoneId = ZoneId.of(node.getTimeZone());
        }
    }

    @Override
    public void run() {

        waitExecution();

        Duration sec = Duration.of(node.getExpire(), ChronoUnit.SECONDS);

        LocalTime expireAt = LocalTime.now(this.zoneId).plus(sec);

        while (isInTimeRange(node)) {

            if(hasNoData()) {
                LOGGER.debug("Exiting method run");
                break;
            }

            LOGGER.info("Checking address {}:{}", this.node.getHost(), this.node.getPort());

            try (Socket socket = new Socket()) {

                TimeUnit.SECONDS.sleep(node.getPollFrequency());

                SocketAddress socketAddress = new InetSocketAddress(this.node.getHost(), this.node.getPort());
                socket.connect(socketAddress);
                this.node.setUp(true);

            } catch (Exception e) {
                LOGGER.debug(e.getMessage() + " {}:{} ", this.node.getHost(), this.node.getPort());
                node.setUp(false);
            }

            if (LocalTime.now(this.zoneId).isAfter(expireAt)) {
                break;
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LOGGER.info("Ended at {} with node: {}", formatter.format(LocalTime.now(this.zoneId)), node);
    }

    private void waitExecution() {

        while (!isInTimeRange(this.node)) {

            if(hasNoData()) {
                LOGGER.debug("Exiting method wait execution");
                break;
            }

            LOGGER.debug("Waiting to check node: {}", this.node);

            try {
                TimeUnit.SECONDS.sleep(MAX_INTERVAL_TIME_IN_SEC);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private boolean isInTimeRange(Node node) {
        LocalTime now = LocalTime.now(this.zoneId);
        return now.isAfter(node.getStart()) && now.isBefore(node.getEnd());
    }

    private boolean hasNoData() {
        MemoryDB db = MemoryDB.instance();
        return db.size() == 0;
    }
}
