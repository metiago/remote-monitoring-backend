package io.tiago.monitor;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.net.ServerSocket;

// TODO Implement unit tests
@RunWith(JUnit4.class)
public class WebSocketTest {

    @Before
    public void setUp(TestContext context) throws IOException {
        Vertx vertx = Vertx.vertx();
        ServerSocket socket = new ServerSocket(0);
        int port = socket.getLocalPort();
        socket.close();
        DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("http.port", port));
        vertx.deployVerticle(WebSocketTest.class.getName(), options, context.asyncAssertSuccess());
    }

    @Test
    public void client() {
        HttpClient client = Vertx.vertx().createHttpClient();
        client.websocket(8001, "localhost", "/", socket -> {
            socket.handler(data -> {
                System.out.println("Server message: ");
                System.out.println("Received data " + data.toString("ISO-8859-1"));
            });
            socket.writeBinaryMessage(Buffer.buffer("Hello server"));
        });
    }
}
