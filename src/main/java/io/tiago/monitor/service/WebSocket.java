package io.tiago.monitor.service;

import io.tiago.monitor.domain.Node;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WebSocket implements Runnable {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(WebSocket.class);

    private ServerWebSocket handler;

    public WebSocket(ServerWebSocket handler) {
        this.handler = handler;
    }

    @Override
    public void run() {

        MemoryDB db = MemoryDB.instance();

        while (true) {

            Map<String, Node> data = db.all();

            data.forEach((k, v) -> {
                LOGGER.info(String.format("Sending event %s", k));
                this.handler.writeBinaryMessage(Buffer.buffer(k));
            });

            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
