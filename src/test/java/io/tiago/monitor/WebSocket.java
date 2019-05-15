package io.tiago.monitor;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;

public class WebSocket {

    public static void main(String[] args) {
        HttpClient client = Vertx.vertx().createHttpClient();
        client.websocket(8001, "localhost", "/", socket -> socket.handler(data -> System.out.println("Received data " + data)));
    }
}
