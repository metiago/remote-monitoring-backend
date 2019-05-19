package io.tiago.monitor.service;

import io.tiago.monitor.domain.Node;
import io.tiago.monitor.helper.JsonHelper;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class WebSocket implements Runnable {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(WebSocket.class);

    private volatile boolean running = true;

    private ServerWebSocket handler;

    public WebSocket(ServerWebSocket handler) {
        this.handler = handler;
    }

    @Override
    public void run() {

        MemoryDB db = MemoryDB.instance();

        while (running) {

            try {

                List<Node> data = db.all();

                for (Node n : data) {
                    TimeUnit.SECONDS.sleep(10);
                    LOGGER.info("Sending status for: {}", n);
                    this.handler.write(Buffer.buffer(JsonHelper.encodePrettily(n)));
                }

                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException | IllegalStateException e) {
                LOGGER.error(e.getMessage(), e);
                // When web socket close its connection we stop this thread
                running = false;
            }
        }
    }
}
