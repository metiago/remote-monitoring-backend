package io.tiago.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.time.LocalTime;

public class Monitor implements Runnable {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(Monitor.class);

    private Node node;

    public Monitor(Node node) {
        this.node = node;
    }

    @Override
    public void run() {

        while (!isScheduledRangeTime(node)) {

            LOGGER.info(String.format("Checking address %s:%s...", this.node.getHost(), this.node.getPort()));

            try (Socket socket = new Socket()) {
                Thread.sleep(2000);
                SocketAddress socketAddress = new InetSocketAddress(this.node.getHost(), this.node.getPort());
                socket.connect(socketAddress);
                this.node.setUp(true);
            } catch (Exception e) {
                node.setUp(false);
            }

            LOGGER.info(String.format("Address %s:%s is %s", this.node.getHost(), this.node.getPort(), this.node.isUp()));
        }
    }

    private boolean isScheduledRangeTime(Node node) {
        LocalTime now = LocalTime.now();
        return now.isAfter(node.getStart()) && now.isBefore(node.getEnd());
    }
}
