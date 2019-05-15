package io.tiago.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.time.LocalTime;

public class Monitor {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(Monitor.class);

    public Node check(Node node) throws IOException {

        LOGGER.info(String.format("Checking address %s:%s...", node.getHost(), node.getPort()));

        try (Socket socket = new Socket()) {
            SocketAddress socketAddress = new InetSocketAddress(node.getHost(), node.getPort());
            socket.connect(socketAddress);
            node.setUp(true);
        } catch (Exception e) {
            node.setUp(false);
            throw e;
        }

        LOGGER.info(String.format("Address %s:%s is %s", node.getHost(), node.getPort(), node.isUp()));
        return node;
    }

    private boolean isScheduledRangeTime(Node node) {
        LocalTime now = LocalTime.now();
        return now.isAfter(node.getStart()) && now.isBefore(node.getEnd());
    }
}
